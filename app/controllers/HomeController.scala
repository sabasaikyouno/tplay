package controllers

import java.io.File
import java.util.UUID

import javax.inject._
import play.api._
import play.api.mvc._
import models.form.TextForm._
import domain.repository.{PostedDataRepository, RoomDataRepository, UserDataRepository}
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import akka.stream.scaladsl._
import models.form.LoginForm.loginForm
import models.form.RoomForm.roomForm
import models.form.SignupForm.signupForm
import models.post.{PostImage, PostText}
import play.api.cache.SyncCacheApi
import utils.RoomUtils.makeOrder
import utils.UserUtils.passwordHash

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  val postedDataRepository: PostedDataRepository,
  roomDataRepository: RoomDataRepository,
  val userDataRepository: UserDataRepository,
  val cache: SyncCacheApi
) extends UserLoginController(cc, roomDataRepository) with I18nSupport {

  def index(pageOpt: Option[Int], tagOpt: Option[String], orderOpt: Option[String]) = UserAction.async { implicit request =>
    val order = makeOrder(orderOpt.filter(_ != ""))
    val limit = 3
    val page = pageOpt.filter(_ >= 0).getOrElse(0) * limit
    val roomDataList = tagOpt.filter(_ != "").fold(roomDataRepository.getLatestRoom(page, limit, order))(
      roomDataRepository.getRoomTagFilter(page, _, limit, order)
    )

    roomDataList.map( list =>
      Ok(views.html.index(list, page, tagOpt.getOrElse(""), orderOpt.getOrElse("")))
    )
  }

  def loginFormView = Action { implicit request =>
    Ok(views.html.login())
  }

  def postText(roomId: String) = PostAction(roomId, "text") { implicit request =>
    textForm.bindFromRequest.fold(
      errors => {
        Redirect("/")
      },
      sendText => {
        postedDataRepository.create(PostText(roomId, request.user, sendText.text))
        Redirect(s"/room/$roomId")
      }
    )
  }

  def postImage(roomId: String) = PostAction(roomId, "image")(parse.multipartFormData) { implicit request =>
    request.body.file("image").filter(_.filename.endsWith("jpg")).map { image =>
      val imgPath = s"tmp/img/${UUID.randomUUID() + image.filename}"
      image.ref.moveTo(new File(imgPath))
      postedDataRepository.create(PostImage(roomId, request.user, imgPath))
      Redirect(s"/room/$roomId")
    }.getOrElse(Redirect("/"))
  }

  def getImage(fileName: String, roomId: String) = RoomAction(roomId) { implicit request =>
    val file = new File(s"tmp/img/$fileName")
    val source = FileIO.fromPath(file.toPath)

    Result(
      header = ResponseHeader(200, Map.empty),
      body = HttpEntity.Streamed(source, None, Some("image/png"))
    )
  }

  def room(roomId: String) = RoomAction(roomId).async { implicit request =>
    for {
      postedList <- postedDataRepository.getLatestPosted(3, roomId)
      roomData <- roomDataRepository.getOneRoom(roomId)
      tags <- roomDataRepository.getTags(roomId)
      _ <- roomDataRepository.roomViewCount(roomId)
    } yield Ok(views.html.room(roomData, postedList, tags))
  }

  def createRoom = UserAction { implicit request =>
    roomForm.bindFromRequest.fold(
      errors => {
        NotFound(errors.toString)
      },
      roomForm => {
        val roomId = UUID.randomUUID().toString
        roomDataRepository.create(roomId, request.user, roomForm.title.getOrElse("noTitle"), roomForm.contentType.getOrElse("text/image"))
        roomDataRepository.createTag(roomId, roomForm.tag.map(_.split(" ")).getOrElse(Array("noTag")))
        roomForm.authUser.foreach(user =>
          roomDataRepository.createAuthUser(roomId, user.split(" "))
        )
        Redirect("/")
      }
    )
  }

  def signup = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      errors => {
        Redirect("/")
      },
      signup => {
        userDataRepository.signup(
          signup.userId,
          passwordHash(signup.password)
        )
        Redirect("/")
      }
    )
  }

  def login = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      errors => {
        Future(Redirect("/login_form"))
      },
      login => {
        userDataRepository.login(login.userId, passwordHash(login.password)).map(_.fold(Redirect("/login_form")){ user =>
          val idCookie = request.cookies.get("id").getOrElse(Cookie("id", UUID.randomUUID().toString))
          cache.set(idCookie.value, user.name)
          Redirect("/").withCookies(idCookie)
        })
      }
    )
  }
}

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
import models.form.Login.loginForm
import models.form.Room.roomForm
import models.form.Signup.signupForm
import models.post.{PostImage, PostText}
import models.room.RoomData
import play.api.cache.SyncCacheApi
import utils.ResultUtils._
import utils.UserUtils.passwordHash

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  implicit val postedDataRepository: PostedDataRepository,
  implicit override val roomDataRepository: RoomDataRepository,
  val userDataRepository: UserDataRepository,
  implicit val cache: SyncCacheApi
) extends UserLoginController(cc, roomDataRepository) with I18nSupport {

  def index(pageOpt: Option[Int], tagOpt: Option[String], orderOpt: Option[String]) = UserAction.async { implicit request =>
    indexResult(pageOpt, tagOpt, orderOpt) { (roomDataList, page) =>
      Ok(views.html.index(roomDataList, page, tagOpt.getOrElse(""), orderOpt.getOrElse("")))
    }
  }

  def loginFormView = Action { implicit request =>
    Ok(views.html.login())
  }

  def postText(roomId: String) = PostAction(roomId, "text") { implicit request =>
    textForm.bindFromRequest.fold(
      errors => Redirect("/"),
      sendText => {
        postedDataRepository.create(PostText(roomId, request.user, sendText.text))
        Redirect(s"/room/$roomId")
      }
    )
  }

  def postImage(roomId: String) = PostAction(roomId, "image")(parse.multipartFormData) { implicit request =>
    request.body.file("image").filter(_.filename.endsWith("jpg")).map { image =>
      val imgPath = s"tmp/img/${UUID.randomUUID().toString + image.filename}"
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

  def room(roomId: String, pageOpt: Option[Int]) = RoomAction(roomId).async { implicit request =>
    roomResult(roomId, pageOpt){ (roomData, postedList, tags, page) =>
      Ok(views.html.room(roomData, postedList, tags, page))
    }
  }

  def createRoom = UserAction { implicit request =>
    roomForm.bindFromRequest.fold(
      errors => NotFound(errors.toString),
      room => {
        val roomId = UUID.randomUUID().toString
        roomDataRepository.create(roomId, room)
        Redirect("/")
      }
    )
  }

  def signup = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      errors => Redirect("/login_form"),
      signup => {
        userDataRepository.signup(
          signup.userId,
          passwordHash(signup.password)
        )
        Redirect("/login_form")
      }
    )
  }

  def login = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      errors => Future(Redirect("/login_form")),
      login => {
        userDataRepository.login(login.userId, passwordHash(login.password)).map(_.fold(Redirect("/login_form")){ user =>
          val idCookie = request.cookies.get("id").getOrElse(Cookie("id", UUID.randomUUID().toString))
          cache.set(idCookie.value, user.name)
          Redirect("/").withCookies(idCookie)
        })
      }
    )
  }

  def roomEdit(roomId: String) = RoomEditAction(roomId).async { implicit request =>
    for {
      roomData <- Future(cache.get[RoomData](roomId).get)
      tags <- roomDataRepository.getTags(roomId).map(_.mkString(" "))
      authUsers <- roomDataRepository.getAuthUsers(roomId).map(_.mkString(" "))
    } yield Ok(views.html.roomEdit(roomId, roomData, tags, authUsers))
  }

  def roomUpdate(roomId: String) = RoomEditAction(roomId) { implicit request =>
    roomForm.bindFromRequest.fold(
      errors => Redirect("/"),
      roomForm => {
        roomDataRepository.updateRoom(roomId, roomForm.title.getOrElse("noTitle"), roomForm.contentType.getOrElse("text/image"))
        roomDataRepository.updateTag(roomId, roomForm.tag.map(_.split(" ")).getOrElse(Array("noTag")))
        roomForm.authUser.foreach( users =>
          roomDataRepository.updateAuthUser(roomId, users.split(" "))
        )
        Redirect("/")
      }
    )
  }
}

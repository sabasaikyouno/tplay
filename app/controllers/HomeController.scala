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
import utils.UserUtils.passwordHash

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
  cc: ControllerComponents,
  val postedDataRepository: PostedDataRepository,
  val roomDataRepository: RoomDataRepository,
  val userDataRepository: UserDataRepository,
  val cache: SyncCacheApi
) extends UserLoginController(cc) with I18nSupport {

  def index = Action.async { implicit request =>
    roomDataRepository.getLatestRoom(3).map( list =>
      Ok(views.html.index(list))
    )
  }

  def postText(roomId: String) = UserAction { implicit request =>
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

  def postImage(roomId: String) = UserAction(parse.multipartFormData) { implicit request =>
    request.body.file("image").map { image =>
      val imgPath = s"tmp/img/${UUID.randomUUID() + image.filename}"
      image.ref.moveTo(new File(imgPath))
      postedDataRepository.create(PostImage(roomId, request.user, imgPath))
      Redirect(s"/room/$roomId")
    }.getOrElse {
      Redirect("/")
    }
  }

  def getImage(fileName: String) = UserAction { implicit request =>
    val file = new File(s"tmp/img/$fileName")
    val source = FileIO.fromPath(file.toPath)

    Result(
      header = ResponseHeader(200, Map.empty),
      body = HttpEntity.Streamed(source, None, Some("image/png"))
    )
  }

  def room(roomId: String) = UserAction.async { implicit request =>
    for {
      postedList <- postedDataRepository.getLatestPosted(3, roomId)
      tags <- roomDataRepository.getTags(roomId)
    } yield Ok(views.html.room(roomId, postedList, tags))
  }

  def createRoom = UserAction { implicit request =>
    roomForm.bindFromRequest.fold(
      errors => {
        Redirect("/")
      },
      tagForm => {
        val roomId = UUID.randomUUID().toString
        roomDataRepository.create(roomId, request.user)
        roomDataRepository.createTag(roomId, tagForm.tag.map(_.split(" ")).getOrElse(Array("noTag")))
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
        Future(Redirect("/"))
      },
      login => {
        userDataRepository.login(login.userId, passwordHash(login.password)).map {
          case Some(user) =>
            val idCookie = request.cookies.get("id").getOrElse(Cookie("id", UUID.randomUUID().toString))
            cache.set(idCookie.value, user.name)
            Redirect("/").withCookies(idCookie)
          case _ => Redirect("/")
        }
      }
    )
  }
}

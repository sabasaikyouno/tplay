package controllers

import java.io.File
import java.util.UUID

import javax.inject._
import play.api._
import play.api.mvc._
import models.SendText._
import domain.repository.{PostedDataRepository, RoomDataRepository}
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import akka.stream.scaladsl._
import models.post.{PostImage, PostText}
import models.room.RoomData

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(
  val controllerComponents: ControllerComponents,
  val postedDataRepository: PostedDataRepository,
  val roomDataRepository: RoomDataRepository
) extends BaseController with I18nSupport {

  def index = Action.async { implicit request =>
    roomDataRepository.getLatestRoom(3).map( list =>
      Ok(views.html.index(list))
    )
  }

  def postText(roomId: String) = Action { implicit request =>
    sendTextForm.bindFromRequest.fold(
      errors => {
        Redirect("/")
      },
      sendText => {
        postedDataRepository.create(PostText(roomId, sendText.text))
        Redirect(s"/room/$roomId")
      }
    )
  }

  def postImage(roomId: String) = Action(parse.multipartFormData) { implicit request =>
    request.body.file("image").map { image =>
      val imgPath = s"tmp/img/${UUID.randomUUID() + image.filename}"
      image.ref.moveTo(new File(imgPath))
      postedDataRepository.create(PostImage(roomId, imgPath))
      Redirect(s"/room/$roomId")
    }.getOrElse {
      Redirect("/")
    }
  }

  def getImage(fileName: String) = Action { implicit request =>
    val file = new File(s"tmp/img/$fileName")
    val source = FileIO.fromPath(file.toPath)

    Result(
      header = ResponseHeader(200, Map.empty),
      body = HttpEntity.Streamed(source, None, Some("image/png"))
    )
  }

  def room(roomId: String) = Action.async { implicit request =>
    postedDataRepository.getLatestPosted(3, roomId).map( list =>
      Ok(views.html.room(roomId, list))
    )
  }

  def createRoom = Action { implicit request =>
    roomDataRepository.create
    Redirect("/")
  }
}

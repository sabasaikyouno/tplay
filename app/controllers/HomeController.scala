package controllers

import java.io.File
import java.util.UUID

import javax.inject._
import play.api._
import play.api.mvc._
import models.SendText._
import domain.repository.PostedDataRepository
import models.{PostImage, PostText}
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(
  val controllerComponents: ControllerComponents,
  val postedDataRepository: PostedDataRepository
) extends BaseController with I18nSupport {

  def index = Action.async { implicit request =>
    postedDataRepository.getLatestPosted(3).map( list =>
      Ok(views.html.index(list))
    )
  }

  def postText = Action { implicit request =>
    sendTextForm.bindFromRequest.fold(
      errors => {
        Redirect("/")
      },
      sendText => {
        postedDataRepository.create(PostText(sendText.text))
        Redirect("/")
      }
    )
  }

  def postImage = Action(parse.multipartFormData) { implicit request =>
    request.body.file("image").map { image =>
      val imgPath = s"tmp/img/${UUID.randomUUID() + image.filename}"
      image.ref.moveTo(new File(imgPath))
      postedDataRepository.create(PostImage(imgPath))
      Redirect("/")
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
}

package controllers

import java.io.File
import java.util.UUID

import javax.inject._
import play.api._
import play.api.mvc._
import models.SendText._
import domain.repository.{ImageDataRepository, TextDataRepository}
import models.{ImageData, PostedData, TextData}
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  val controllerComponents: ControllerComponents,
  val textDataRepository: TextDataRepository,
  val imageDataRepository: ImageDataRepository
) extends BaseController with I18nSupport {

  def index = Action.async { implicit request =>
    def postedWithId(list: List[PostedData], id: Long): List[PostedData] = list match {
      case TextData(_, text, createdTime) :: t => TextData(id, text, createdTime) :: postedWithId(t, id + 1)
      case ImageData(_, img, createdTime) :: t => ImageData(id, img, createdTime) :: postedWithId(t, id + 1)
      case _ => Nil
    }

    val textList = textDataRepository.getLatestText(3)
    val imgList = imageDataRepository.getLatestImage(3)
    val textImageList = textList.zipWith(imgList)(_ ::: _)

    textImageList.map { list =>
      val sortedList = list.sortBy(_.createdTime).takeRight(3)

      Ok(views.html.index(postedWithId(sortedList, sortedList.head.id)))
    }
  }

  def postText = Action { implicit request =>
    sendTextForm.bindFromRequest.fold(
      errors => {
        Redirect("/")
      },
      sendText => {
        textDataRepository.create(sendText)
        Redirect("/")
      }
    )
  }

  def postImage = Action(parse.multipartFormData) { implicit request =>
    request.body.file("image").map { image =>
      val imgPath = s"tmp/img/${UUID.randomUUID() + image.filename}"
      image.ref.moveTo(new File(imgPath))
      imageDataRepository.create(imgPath)
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

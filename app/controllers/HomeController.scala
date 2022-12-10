package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models.SendText._
import domain.repository.TextDataRepository
import play.api.i18n.I18nSupport

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, val textDataRepository: TextDataRepository) extends BaseController with I18nSupport {
  def index() = Action.async { implicit request =>
    textDataRepository.getLatestText(3).map(textDataList => Ok(views.html.index(textDataList)))
  }

  def postText() = Action { implicit request =>
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
}

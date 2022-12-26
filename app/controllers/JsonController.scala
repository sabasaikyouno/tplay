package controllers

import java.util.UUID

import domain.repository.{PostedDataRepository, RoomDataRepository, UserDataRepository}
import javax.inject._
import models.form.{Login, Signup}
import play.api._
import play.api.cache.SyncCacheApi
import play.api.libs.json._
import play.api.mvc._
import utils.ResultUtils._
import utils.UserUtils.passwordHash

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class JsonController @Inject()(
  cc: ControllerComponents,
  implicit override val roomDataRepository: RoomDataRepository,
  implicit val postedDataRepository: PostedDataRepository,
  val userDataRepository: UserDataRepository,
  implicit val cache: SyncCacheApi
) extends JsonUserController(cc, roomDataRepository) {

  def index(pageOpt: Option[Int], tagOpt: Option[String], orderOpt: Option[String]) = UserAction.async { implicit request =>
    indexResult(pageOpt, tagOpt, orderOpt) { (roomDataList, page) =>
      Ok(Json.obj(
        "status" -> "OK",
        "roomDataList" -> roomDataList,
        "page" -> page,
        "tag" -> tagOpt.getOrElse[String](""),
        "order" -> orderOpt.getOrElse[String]("")))
    }
  }

  def room(roomId: String, pageOpt: Option[Int]) = RoomAction(roomId).async { implicit request =>
    roomResult(roomId, pageOpt){ (roomData, postedList, tags, _) =>
      Ok(Json.obj(
        "status" -> "OK",
        "roomData" -> roomData,
        "postedList" -> postedList,
        "tags" -> tags))
    }
  }

  def login = Action(parse.json).async { implicit request =>
    request.body.validate[Login].fold(
      errors => Future(BadRequest(Json.obj("message" -> "miss parameter"))),
      login => {
        userDataRepository.login(login.userId, passwordHash(login.password)).map(_.fold(Ok(Json.obj("message" -> "no user"))){ user =>
          val id = UUID.randomUUID().toString
          cache.set(id, user.name)
          Ok(Json.obj("id" -> id))})
      }
    )
  }

  def signup = Action(parse.json) { implicit request =>
    request.body.validate[Signup].fold(
      errors => BadRequest(Json.obj("message" -> "miss parameter")),
      signup => {
        userDataRepository.signup(signup.userId, passwordHash(signup.password))
        Ok(Json.obj("status" -> "OK"))
      }
    )
  }
}

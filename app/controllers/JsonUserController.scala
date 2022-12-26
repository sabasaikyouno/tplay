package controllers

import domain.repository.RoomDataRepository
import models.room.RoomData
import models.user.UserData
import play.api.mvc._
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

abstract class JsonUserController(cc: ControllerComponents, roomDataRepository: RoomDataRepository) extends FilterController(cc, roomDataRepository) {
  implicit val cache: SyncCacheApi

  val roomBadResult: Result = BadRequest(Json.obj("message" -> "no authUser"))
  def postBadResult(roomData: RoomData): Result = BadRequest(Json.obj("message" -> "bad content type"))
  def roomEditBadResult(roomData: RoomData): Result = BadRequest(Json.obj("message" -> "no user"))

  def UserActionRefiner = new ActionRefiner[Request, UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    override def refine[A](request: Request[A]) = Future(
      request.headers.get("id").flatMap(id =>
        cache.get[String](id).map(v =>
          new UserRequest(UserData(v), request))
      ).toRight(BadRequest(Json.obj("message" -> "no user")))
    )
  }
}

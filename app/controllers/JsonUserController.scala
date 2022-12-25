package controllers

import domain.repository.RoomDataRepository
import models.user.UserData
import play.api.mvc._
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import utils.CacheUtils.getAuthUsers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

abstract class JsonUserController(protected val cc: ControllerComponents, implicit val roomDataRepository: RoomDataRepository) extends AbstractController(cc) {
  implicit val cache: SyncCacheApi

  def UserAction =
    Action andThen UserActionRefiner

  def RoomAction(roomId: String) =
    UserAction andThen RoomFilter(roomId: String)

  def UserActionRefiner = new ActionRefiner[Request, UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    override def refine[A](request: Request[A]) = Future(
      request.headers.get("id").flatMap( id =>
        cache.get[String](id).map( v =>
          new UserRequest(UserData(v), request))
      ).toRight(BadRequest(Json.obj("message" -> "no user")))
    )
  }

  def RoomFilter(roomId: String) = new ActionFilter[UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    def filter[A](request: UserRequest[A]) = getAuthUsers(roomId).map { authUsers =>
      cache.set(roomId, authUsers)
      authUsers match {
        case authUsers if authUsers.nonEmpty && !authUsers.contains(request.user.name) =>
          Some(BadRequest(Json.obj("message" -> "no authUser")))
        case _ => None
      }
    }
  }
}

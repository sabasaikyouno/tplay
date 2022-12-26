package controllers

import domain.repository.RoomDataRepository
import models.room.RoomData
import play.api.cache.SyncCacheApi
import play.api.mvc._
import utils.CacheUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

abstract class FilterController(protected val cc: ControllerComponents, implicit val roomDataRepository: RoomDataRepository) extends AbstractController(cc) {
  implicit val cache: SyncCacheApi

  protected val roomBadResult: Result
  protected def postBadResult(roomData: RoomData): Result
  protected def roomEditBadResult(roomData: RoomData): Result

  def UserAction =
    Action andThen UserActionRefiner

  def RoomAction(roomId: String): ActionBuilder[UserRequest, AnyContent] =
    UserAction andThen RoomFilter(roomId)

  def PostAction(roomId: String, postContentType: String) =
    RoomAction(roomId) andThen PostFilter(roomId, postContentType)

  def RoomEditAction(roomId: String) =
    RoomAction(roomId) andThen RoomEditorFilter(roomId)

  def UserActionRefiner: ActionRefiner[Request, UserRequest]

  def RoomFilter(roomId: String) = new ActionFilter[UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    def filter[A](request: UserRequest[A]) = getAuthUsers(roomId).map { authUsers =>
      cache.set(roomId, authUsers)
      authUsers match {
        case authUsers if authUsers.nonEmpty && !authUsers.contains(request.user.name) =>
          Some(roomBadResult)
        case _ => None
      }
    }
  }

  def PostFilter(roomId: String, postContentType: String) = new ActionFilter[UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    def filter[A](request: UserRequest[A]) = getRoom(roomId).map { roomData =>
      setOptCache(roomId, roomData)
      roomData
        .filterNot(_.contentType.contains(postContentType))
        .map(postBadResult)
    }
  }

  def RoomEditorFilter(roomId: String) = new ActionFilter[UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    def filter[A](request: UserRequest[A]) = getRoom(roomId).map { roomData =>
      setOptCache(roomId, roomData)
      roomData
        .filterNot(request.user.name == _.userId)
        .map(roomEditBadResult)
    }
  }
}

package controllers

import domain.repository.RoomDataRepository
import models.room.RoomData
import models.user.UserData
import play.api.cache.SyncCacheApi
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val user: UserData, request: Request[A]) extends WrappedRequest[A](request)

abstract class UserLoginController(protected val cc: ControllerComponents, val roomDataRepository: RoomDataRepository) extends AbstractController(cc) {
  val cache: SyncCacheApi

  def UserAction =
    Action andThen UserActionRefiner

  def RoomAction(roomId: String) =
    UserAction andThen RoomFilter(roomId)

  def PostAction(roomId: String, postContentType: String) =
    RoomAction(roomId) andThen PostFilter(roomId, postContentType)

  def RoomEditAction(roomId: String) =
    RoomAction(roomId) andThen RoomEditorFilter(roomId)

  def UserActionRefiner = new ActionRefiner[Request, UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    override def refine[A](request: Request[A]) = {
      Future(
        request.cookies.get("id").flatMap(c =>
          cache.get[String](c.value).map( v =>
            new UserRequest(UserData(v), request)
          )
        ).toRight(Redirect("/login_form"))
      )
    }
  }

  def RoomFilter(roomId: String) = new ActionFilter[UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    def filter[A](request: UserRequest[A]) = getAuthUsers(roomId).map { authUsers =>
      cache.set(roomId, authUsers)
      authUsers match {
        case authUsers if authUsers.nonEmpty && !authUsers.contains(request.user.name) =>
          Some(Redirect("/"))
        case _ => None
      }
    }
  }

  def PostFilter(roomId: String, postContentType: String) = new ActionFilter[UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    def filter[A](request: UserRequest[A]) = getRoom(roomId).map { roomData =>
      setCache(roomId, roomData)
      roomData
        .filterNot(_.contentType.contains(postContentType))
        .map(_ => Redirect(s"/room$roomId"))
    }
  }

  def RoomEditorFilter(roomId: String) = new ActionFilter[UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    def filter[A](request: UserRequest[A]) = getRoom(roomId).map { roomData =>
      setCache(roomId, roomData)
      roomData
        .filterNot(request.user.name == _.userId)
        .map(_ => Redirect(s"/room/$roomId"))
    }
  }

  private def getRoom(roomId: String) =
    cache.get[RoomData](roomId).fold(roomDataRepository.getOneRoom(roomId))(v => Future(Some(v)))

  private def getAuthUsers(roomId: String) =
    cache.get[List[String]](roomId+"authUsers").fold(roomDataRepository.getAuthUsers(roomId))(Future(_))

  // 値がNoneの場合セットしない。
  private def setCache[A](key: String, valueOpt: Option[A]) =
    valueOpt.foreach(cache.set(key, _))
}

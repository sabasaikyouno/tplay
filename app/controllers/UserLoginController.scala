package controllers

import domain.repository.RoomDataRepository
import models.room.RoomData
import models.user.UserData
import play.api.cache.SyncCacheApi
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val user: UserData, request: Request[A]) extends WrappedRequest[A](request)

abstract class UserLoginController(cc: ControllerComponents, roomDataRepository: RoomDataRepository) extends FilterController(cc, roomDataRepository) {
  implicit val cache: SyncCacheApi

  val roomBadResult: Result = Redirect("/")
  def postBadResult(roomData: RoomData): Result = Redirect(s"/room/${roomData.roomId}")
  def roomEditBadResult(roomData: RoomData): Result = Redirect(s"/room/${roomData.roomId}")

  def UserActionRefiner = new ActionRefiner[Request, UserRequest] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    override def refine[A](request: Request[A]) = Future(
        request.cookies.get("id").flatMap(c =>
          cache.get[String](c.value).map( v =>
            new UserRequest(UserData(v), request)
          )
        ).toRight(Redirect("/login_form"))
    )
  }
}

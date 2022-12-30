package domain.repository

import controllers.UserRequest
import models.form.Room
import models.room.RoomData
import models.user.UserData
import scalikejdbc.interpolation.SQLSyntax

import scala.concurrent.Future

trait RoomDataRepository {
  def create(roomId: String, room: Room)(implicit request: UserRequest[_]): Future[_]

  def createRoom(roomId: String, userData: UserData, room: Room): Future[_]

  def createTag(roomId: String, tag: Array[String]): Future[_]

  def createAuthUser(roomId: String, users: Array[String]): Future[_]

  def roomViewCount(roomId: String): Future[_]

  def getTags(roomId: String): Future[List[String]]

  def getAuthUsers(roomId: String): Future[List[String]]

  def getLatestRoom(page: Int, limit: Int, order: SQLSyntax): Future[List[RoomData]]

  def getRoomTagFilter(page: Int, tag: String, limit: Int, order: SQLSyntax): Future[List[RoomData]]

  def getRoomContentType(roomId: String): Future[String]

  def getOneRoom(roomId: String): Future[Option[RoomData]]

  def updateRoom(roomId: String, title: String, contentType: String): Future[_]

  def updateTag(roomId: String, tag: Array[String]): Future[_]

  def updateAuthUser(roomId: String, users: Array[String]): Future[_]

  def delete(roomId: String): Future[_]

  def deleteRoom(roomId: String): Future[_]

  def deleteTag(roomId: String): Future[_]

  def deleteAuthUser(roomId: String): Future[_]
}

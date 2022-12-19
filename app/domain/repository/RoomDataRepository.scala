package domain.repository

import models.room.RoomData
import models.user.UserData
import scalikejdbc.interpolation.SQLSyntax

import scala.concurrent.Future

trait RoomDataRepository {
  def create(roomId: String, user: UserData, title: String, contentType: String): Future[_]

  def createTag(roomId: String, tag: Array[String]): Future[_]

  def createAuthUser(roomId: String, users: Array[String]): Future[_]

  def roomViewCount(roomId: String): Future[_]

  def getTags(roomId: String): Future[List[String]]

  def getAuthUsers(roomId: String): Future[List[String]]

  def getLatestRoom(limit: Int, order: SQLSyntax): Future[List[RoomData]]

  def getRoomTagFilter(tag: String, limit: Int, order: SQLSyntax): Future[List[RoomData]]

  def getRoomContentType(roomId: String): Future[String]

  def getOneRoom(roomId: String): Future[RoomData]
}

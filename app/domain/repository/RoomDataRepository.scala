package domain.repository

import models.room.RoomData
import models.user.UserData

import scala.concurrent.Future

trait RoomDataRepository {
  def create(roomId: String, user: UserData): Future[_]

  def createTag(roomId: String, tag: Array[String]): Future[_]

  def createAuthUser(roomId: String, users: Array[String]): Future[_]

  def getTags(roomId: String): Future[List[String]]

  def getAuthUsers(roomId: String): Future[List[String]]

  def getLatestRoom(count: Int): Future[List[RoomData]]
}

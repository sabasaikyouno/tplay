package domain.repository

import models.room.RoomData
import models.user.UserData

import scala.concurrent.Future

trait RoomDataRepository {
  def create(user: UserData): Future[_]

  def getLatestRoom(count: Int): Future[List[RoomData]]
}

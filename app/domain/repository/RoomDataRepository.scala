package domain.repository

import models.room.RoomData

import scala.concurrent.Future

trait RoomDataRepository {
  def create: Future[_]

  def getLatestRoom(count: Int): Future[List[RoomData]]
}

package infrastructure.repository

import java.util.UUID

import domain.repository.RoomDataRepository
import models.room.RoomData

import scala.concurrent.Future
import scala.util.Try
import scalikejdbc._

class RoomDataRepositoryImpl extends RoomDataRepository {

  def create: Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO room_properties (
                 | room_id
                 | ) VALUES (
                 | ${UUID.randomUUID().toString}
                 | )
               """.stripMargin
          sql.update().apply()
        }
      }
    })

  def getLatestRoom(count: Int): Future[List[RoomData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | id,
                 | room_id
                 | FROM room_properties
                 | ORDER BY id DESC
                 | LIMIT $count
               """.stripMargin
          sql.map(resultSetToRoomData).list().apply()
        }
      }
    })

  def resultSetToRoomData(rs: WrappedResultSet): RoomData =
    RoomData(
      id = rs.long("id"),
      roomId = rs.string("room_id")
    )
}

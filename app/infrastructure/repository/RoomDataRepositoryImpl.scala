package infrastructure.repository

import domain.repository.RoomDataRepository
import models.room.RoomData
import models.user.UserData

import scala.concurrent.Future
import scala.util.Try
import scalikejdbc._

class RoomDataRepositoryImpl extends RoomDataRepository {

  def create(roomId: String, user: UserData): Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO room_properties (
                 | room_id,
                 | user_id
                 | ) VALUES (
                 | $roomId,
                 | ${user.name}
                 | )
               """.stripMargin
          sql.update().apply()
        }
      }
    })

  def createTag(roomId: String, tag: Array[String]): Future[_] =
    Future.fromTry(Try{
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          tag.map { tag =>
            sql"""INSERT INTO tag_properties (
                 | room_id,
                 | tag
                 | ) VALUES (
                 | $roomId,
                 | $tag
                 | )
               """.stripMargin
          }.foreach(_.update().apply())
        }
      }
    })

  def createAuthUser(roomId: String, users: Array[String]): Future[_] =
    Future.fromTry(Try{
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          users.map { user =>
            sql"""INSERT INTO room_auth_properties (
                 | room_id,
                 | user_id
                 | ) VALUES (
                 | $roomId,
                 | $user
                 | )
               """.stripMargin
          }.foreach(_.update().apply())
        }
      }
    })

  def getTags(roomId: String): Future[List[String]] =
    Future.fromTry(Try{
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | tag
                 | FROM tag_properties
                 | WHERE room_id = $roomId
               """.stripMargin
          sql.map(_.string("tag")).list().apply()
        }
      }
    })

  def getAuthUsers(roomId: String): Future[List[String]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | user_id
                 | FROM room_auth_properties
                 | WHERE room_id = $roomId
               """.stripMargin
          sql.map(_.string("user_id")).list().apply()
        }
      }
    })

  def getLatestRoom(limit: Int): Future[List[RoomData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | id,
                 | room_id
                 | FROM room_properties
                 | ORDER BY id DESC
                 | LIMIT $limit
               """.stripMargin
          sql.map(resultSetToRoomData).list().apply()
        }
      }
    })

  def getRoomTagFilter(tag: String, limit: Int): Future[List[RoomData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | room_properties.id,
                 | room_properties.room_id
                 | FROM room_properties
                 | JOIN tag_properties
                 | ON room_properties.room_id = tag_properties.room_id
                 | AND tag_properties.tag = $tag
                 | ORDER BY room_properties.id DESC
                 | LIMIT $limit
               """.stripMargin
          sql.map(resultSetToRoomData).list().apply()
        }
      }
    })

  private[this] def resultSetToRoomData(rs: WrappedResultSet): RoomData =
    RoomData(
      id = rs.long("id"),
      roomId = rs.string("room_id")
    )
}

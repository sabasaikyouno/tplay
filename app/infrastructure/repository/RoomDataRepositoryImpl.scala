package infrastructure.repository

import domain.repository.RoomDataRepository
import models.room.RoomData
import models.user.UserData

import scala.concurrent.Future
import scala.util.Try
import scalikejdbc._

class RoomDataRepositoryImpl extends RoomDataRepository {

  def create(roomId: String, user: UserData, title: String, contentType: String): Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO room_properties (
                 | room_id,
                 | user_id,
                 | title,
                 | view_count,
                 | content_type
                 | ) VALUES (
                 | $roomId,
                 | ${user.name},
                 | $title,
                 | 0,
                 | $contentType
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

  def roomViewCount(roomId: String): Future[_] =
    Future.fromTry(Try{
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""UPDATE room_properties
                 | SET view_count = view_count + 1
                 | WHERE room_id = $roomId
               """.stripMargin
          sql.update().apply()
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

  def getLatestRoom(page: Int, limit: Int, order: SQLSyntax): Future[List[RoomData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | id,
                 | room_id,
                 | user_id,
                 | title,
                 | view_count,
                 | content_type
                 | FROM room_properties
                 | ORDER BY $order DESC
                 | LIMIT $page, $limit
               """.stripMargin
          sql.map(resultSetToRoomData).list().apply()
        }
      }
    })

  def getRoomTagFilter(page: Int, tag: String, limit: Int, order: SQLSyntax): Future[List[RoomData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | room_properties.id,
                 | room_properties.room_id,
                 | room_properties.user_id,
                 | room_properties.title,
                 | room_properties.view_count,
                 | room_properties.content_type
                 | FROM room_properties
                 | JOIN tag_properties
                 | ON room_properties.room_id = tag_properties.room_id
                 | AND tag_properties.tag = $tag
                 | ORDER BY room_properties.$order DESC
                 | LIMIT $page, $limit
               """.stripMargin
          sql.map(resultSetToRoomData).list().apply()
        }
      }
    })

  def getRoomContentType(roomId: String): Future[String] =
    Future.fromTry(Try{
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | content_type
                 | FROM room_properties
                 | WHERE room_id = $roomId
               """.stripMargin
          sql.map(_.string("content_type")).single().apply().get
        }
      }
    })

  def getOneRoom(roomId: String): Future[RoomData] =
    Future.fromTry(Try{
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | id,
                 | room_id,
                 | user_id,
                 | title,
                 | view_count,
                 | content_type
                 | FROM room_properties
                 | WHERE room_id = $roomId
               """.stripMargin
          sql.map(resultSetToRoomData).single().apply().get
        }
      }
    })

  private[this] def resultSetToRoomData(rs: WrappedResultSet): RoomData =
    RoomData(
      id = rs.long("id"),
      roomId = rs.string("room_id"),
      userId = rs.string("user_id"),
      title = rs.string("title"),
      viewCount = rs.int("view_count"),
      contentType = rs.string("content_type")
    )
}

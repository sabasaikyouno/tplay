package infrastructure.repository

import controllers.UserRequest
import domain.repository.RoomDataRepository
import models.form.Room
import models.room.RoomData
import models.user.UserData

import scala.concurrent.Future
import scalikejdbc._
import utils.DBUtils._

class RoomDataRepositoryImpl extends RoomDataRepository {

  def create(roomId: String, room: Room)(implicit request: UserRequest[_]): Future[_] = {
    createRoom(roomId, request.user, room)
    room.authUser.foreach(users =>
      createAuthUser(roomId, users.split(" "))
    )
    createTag(roomId, room.tag.map(_.split(" ")).getOrElse(Array("noTag")))
  }

  def createRoom(roomId: String, userData: UserData, room: Room) =
    localTx { implicit session =>
      val sql =
        sql"""INSERT INTO room_properties (
             | room_id,
             | user_id,
             | title,
             | view_count,
             | content_type
             | ) VALUES (
             | $roomId,
             | ${userData.name},
             | ${room.title},
             | 0,
             | ${room.contentType}
             | )
               """.stripMargin
      sql.update().apply()
    }

  def createTag(roomId: String, tag: Array[String]): Future[_] =
    localTx { implicit session =>
      tag.map { tag =>
        sql"""INSERT INTO tag_properties(
             | room_id,
             | tag
             | ) VALUES (
             | $roomId,
             | $tag
             | )
           """.stripMargin
      }.foreach(_.update().apply())
    }

  def createAuthUser(roomId: String, users: Array[String]): Future[_] =
    localTx { implicit session =>
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

  def roomViewCount(roomId: String): Future[_] =
    localTx { implicit session =>
      val sql =
        sql"""UPDATE room_properties
             | SET view_count = view_count + 1
             | WHERE room_id = $roomId
               """.stripMargin
      sql.update().apply()
    }

  def getTags(roomId: String): Future[List[String]] =
    readOnly { implicit session =>
      val sql =
        sql"""SELECT
             | tag
             | FROM tag_properties
             | WHERE room_id = $roomId
               """.stripMargin
      sql.map(_.string("tag")).list().apply()
    }

  def getAuthUsers(roomId: String): Future[List[String]] =
    readOnly { implicit session =>
      val sql =
        sql"""SELECT
             | user_id
             | FROM room_auth_properties
             | WHERE room_id = $roomId
               """.stripMargin
      sql.map(_.string("user_id")).list().apply()
    }

  def getLatestRoom(page: Int, limit: Int, order: SQLSyntax): Future[List[RoomData]] =
    readOnly { implicit session =>
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

  def getRoomTagFilter(page: Int, tag: String, limit: Int, order: SQLSyntax): Future[List[RoomData]] =
    readOnly { implicit session =>
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

  def getRoomContentType(roomId: String): Future[String] =
    readOnly { implicit session =>
      val sql =
        sql"""SELECT
             | content_type
             | FROM room_properties
             | WHERE room_id = $roomId
               """.stripMargin
      sql.map(_.string("content_type")).single().apply().get
    }

  def getOneRoom(roomId: String): Future[Option[RoomData]] =
    readOnly { implicit session =>
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
      sql.map(resultSetToRoomData).single().apply()
    }

  def updateRoom(roomId: String, title: String, contentType: String): Future[_] =
    localTx { implicit session =>
      val sql =
        sql"""UPDATE
             | room_properties
             | SET
             | title = $title,
             | content_type = $contentType
             | WHERE room_id = $roomId
               """.stripMargin
      sql.update().apply()
    }

  def updateTag(roomId: String, tag: Array[String]): Future[_] = {
    deleteTag(roomId)
    createTag(roomId, tag)
  }

  def updateAuthUser(roomId: String, users: Array[String]): Future[_] = {
    deleteAuthUser(roomId)
    createAuthUser(roomId, users)
  }

  def deleteTag(roomId: String): Future[_] =
    localTx { implicit session =>
      val sql =
        sql"""DELETE
             | FROM tag_properties
             | WHERE room_id = $roomId
               """.stripMargin
      sql.update().apply()
    }

  def deleteAuthUser(roomId: String): Future[_] =
    localTx { implicit session =>
      val sql =
        sql"""DELETE
             | FROM
             | user_properties
             | WHERE room_id = $roomId
               """.stripMargin
      sql.update().apply()
    }

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

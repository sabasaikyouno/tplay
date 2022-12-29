package infrastructure.repository

import java.time.LocalDateTime

import domain.repository.PostedDataRepository
import models.post.PostData
import models.posted
import models.posted.{PostedData, PostedImage, PostedText}
import models.user.UserData

import scala.concurrent.Future
import scalikejdbc._
import utils.DBUtils._

class PostedDataRepositoryImpl extends PostedDataRepository {

  def create(postData: PostData): Future[_] =
    localTx { implicit session =>
      val sql =
        sql"""INSERT INTO posted_properties (
             | content_id,
             | content_type,
             | room_id,
             | user_id,
             | content,
             | created_time
             | ) VALUES (
             | (SELECT IFNULL(MAX(tmp.content_id), 0) FROM (
             | SELECT content_id FROM posted_properties
             | WHERE room_id = ${postData.roomId}) AS tmp) + 1,
             | ${postData.contentType},
             | ${postData.roomId},
             | ${postData.user.name},
             | ${postData.content},
             | ${LocalDateTime.now()}
             | )
               """.stripMargin
      sql.update().apply()
    }

  def getLatestPosted(roomId: String, limit: Int, page: Int): Future[List[PostedData]] =
    readOnly { implicit session =>
      val sql =
        sql"""SELECT
             | content_id,
             | user_id,
             | content_type,
             | content,
             | created_time
             | FROM posted_properties
             | WHERE room_id = $roomId
             | ORDER BY created_time DESC
             | LIMIT $page, $limit
               """.stripMargin
      sql.map(resultSetToPostedData).list().apply()
    }

  def deletePosted(roomId: String, contentId: Long, userId: String): Future[_] =
    localTx { implicit session =>
      val sql =
        sql"""DELETE
             | FROM
             | posted_properties
             | WHERE room_id = $roomId
             | AND content_id = $contentId
             | AND user_id = $userId
           """.stripMargin
      sql.update().apply()
    }

  private[this] def resultSetToPostedData(rs: WrappedResultSet): PostedData =
    rs.string("content_type") match {
      case "text" =>
        PostedText(
          contentId = rs.long("content_id"),
          user = UserData(rs.string("user_id")),
          text = rs.string("content"),
          createdTime = rs.localDateTime("created_time")
        )
      case "image" =>
        PostedImage(
          contentId = rs.long("content_id"),
          user = UserData(rs.string("user_id")),
          img = rs.string("content"),
          createdTime = rs.localDateTime("created_time")
        )
    }
}

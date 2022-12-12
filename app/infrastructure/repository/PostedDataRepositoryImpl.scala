package infrastructure.repository

import java.time.LocalDateTime

import domain.repository.PostedDataRepository
import models.post.PostData
import models.posted
import models.posted.{PostedData, PostedImage}

import scala.concurrent.Future
import scala.util.Try
import scalikejdbc._

class PostedDataRepositoryImpl extends PostedDataRepository {

  def create(postData: PostData): Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO posted_properties (
                 | content_id,
                 | content_type,
                 | room_id,
                 | content,
                 | created_time
                 | ) VALUES (
                 | (SELECT IFNULL(MAX(tmp.content_id), 0) FROM (
                 | SELECT content_id FROM posted_properties
                 | WHERE room_id = ${postData.roomId}) AS tmp) + 1,
                 | ${postData.contentType},
                 | ${postData.roomId},
                 | ${postData.content},
                 | ${LocalDateTime.now()}
                 | )
               """.stripMargin
          sql.update().apply()
        }
      }
    })

  def getLatestPosted(count: Int, roomId: String): Future[List[PostedData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | content_id,
                 | content_type,
                 | content,
                 | created_time
                 | FROM posted_properties
                 | WHERE room_id = $roomId
                 | ORDER BY created_time DESC
                 | LIMIT $count
               """.stripMargin
          sql.map(resultSetToPostedData).list().apply()
        }
      }
    })

  private[this] def resultSetToPostedData(rs: WrappedResultSet): PostedData =
    rs.string("content_type") match {
      case "text" =>
        posted.PostedText(
          id = rs.long("content_id"),
          text = rs.string("content"),
          createdTime = rs.localDateTime("created_time")
        )
      case "image" =>
        PostedImage(
          id = rs.long("content_id"),
          img = rs.string("content"),
          createdTime = rs.localDateTime("created_time")
        )
    }
}
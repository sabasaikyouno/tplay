package infrastructure.repository

import java.time.LocalDateTime

import domain.repository.ImageDataRepository
import models.ImageData

import scala.util.Try
import scalikejdbc._

import scala.concurrent.Future

class ImageDataRepositoryImpl extends ImageDataRepository {
  def create(imgPath: String): Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO image_properties (
                 | image,
                 | created_time
                 | ) VALUES (
                 | $imgPath,
                 | ${LocalDateTime.now()}
                 | )
               """.stripMargin
          sql.update().apply()
        }
      }
    })

  def getLatestImage(count: Int): Future[List[ImageData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | image_id,
                 | image,
                 | created_time
                 | FROM image_properties
                 | ORDER BY created_time DESC
                 | LIMIT $count
               """.stripMargin
          sql.map(resultSetToImageData).list().apply()
        }
      }
    })

  private[this] def resultSetToImageData(rs: WrappedResultSet): ImageData =
    ImageData(
      id = rs.long("image_id"),
      img = rs.string("image"),
      createdTime = rs.localDateTime("created_time")
    )
}

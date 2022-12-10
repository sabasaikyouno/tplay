package infrastructure.repository

import java.time.LocalDateTime

import models.{SendText, TextData}
import domain.repository.TextDataRepository
import scalikejdbc._

import scala.concurrent.Future
import scala.util.Try

class TextDataRepositoryImpl extends TextDataRepository {

  def create(sendText: SendText): Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO text_properties (
                 | text,
                 | created_time
                 | ) VALUES (
                 | ${sendText.text},
                 | ${LocalDateTime.now()}
                 | )
               """.stripMargin
          sql.update().apply()
        }
      }
    })

  def getLatestText(count: Int): Future[List[TextData]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | text_id,
                 | text,
                 | created_time
                 | FROM text_properties
                 | ORDER BY created_time DESC
                 | LIMIT $count
               """.stripMargin
          sql.map(resultSetToTextData).list().apply()
        }
      }
    })

  private[this] def resultSetToTextData(rs: WrappedResultSet): TextData =
    TextData(
      id = rs.long("text_id"),
      text = rs.string("text"),
      createdTime = rs.localDateTime("created_time")
    )
}

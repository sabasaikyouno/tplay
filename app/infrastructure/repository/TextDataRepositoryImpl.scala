package infrastructure.repository

import java.time.LocalDateTime

import models.TextData
import domain.repository.TextDataRepository
import scalikejdbc._

import scala.concurrent.Future
import scala.util.Try

class TextDataRepositoryImpl extends TextDataRepository {

  def create(textData: TextData): Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO text_properties (
                 | text,
                 | created_time
                 | ) VALUES (
                 | ${textData.text},
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
                 | text
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
      text = rs.string("text")
    )
}

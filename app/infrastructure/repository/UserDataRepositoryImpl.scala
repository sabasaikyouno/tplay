package infrastructure.repository

import domain.repository.UserDataRepository
import models.user.UserData

import scala.concurrent.Future
import scala.util.Try
import scalikejdbc._

class UserDataRepositoryImpl extends UserDataRepository {
  def signup(userId: String, password: String): Future[_] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO user_properties (
                 | user_id,
                 | password
                 | ) VALUES (
                 | $userId,
                 | $password
                 | )
               """.stripMargin
          sql.update().apply()
        }
      }
    })

  def login(userId: String, password: String): Future[Option[UserData]] =
    Future.fromTry( Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | user_id
                 | FROM user_properties
                 | WHERE user_id = $userId AND password = $password
               """.stripMargin
          sql.map(resultSetToUserData).single().apply()
        }
      }
    })

  private[this] def resultSetToUserData(rs: WrappedResultSet): UserData =
    UserData(
      name = rs.string("user_id")
    )
}
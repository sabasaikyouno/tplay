package utils

import scalikejdbc.{ConnectionPool, DB, DBSession, using}

import scala.concurrent.Future
import scala.util.Try

object DBUtils {
  def localTx[A](execution: DBSession => A) =
    db(_.localTx(execution))

  def readOnly[A](execution: DBSession => A) =
    db(_.readOnly(execution))

  private def db[A](f: DB => A) =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow()))(f)
    })
}

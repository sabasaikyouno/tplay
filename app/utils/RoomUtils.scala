package utils

import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

object RoomUtils {
  def makeOrder: Option[String] => SQLSyntax = {
    case Some("popular") => sqls"view_count"
    case _ => sqls"id"
  }
}

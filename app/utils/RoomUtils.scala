package utils

import domain.repository.{PostedDataRepository, RoomDataRepository}
import models.posted.PostedData
import models.room.RoomData
import play.api.cache.SyncCacheApi
import play.api.mvc.Result
import scalikejdbc.interpolation.SQLSyntax
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import scala.concurrent.ExecutionContext.Implicits.global

object RoomUtils {
  def makeOrder: Option[String] => SQLSyntax = {
    case Some("popular") => sqls"view_count"
    case _ => sqls"id"
  }

  def roomResult(roomId: String, pageOpt: Option[Int])(f: (RoomData, List[PostedData], List[String], Int) => Result)(implicit cache: SyncCacheApi, roomDataRepository: RoomDataRepository, postedDataRepository: PostedDataRepository) = {
    val limit = 3
    val page = pageOpt.filter(_ >= 0).getOrElse(0) * limit

    for {
      roomData <- roomDataRepository.getOneRoom(roomId)
      if roomData.isDefined
      postedList <- postedDataRepository.getLatestPosted(roomId, limit, page)
      tags <- roomDataRepository.getTags(roomId)
      _ <- roomDataRepository.roomViewCount(roomId)
    } yield f(roomData.get, postedList, tags, page)
  }

}

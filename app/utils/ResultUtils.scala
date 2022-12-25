package utils

import domain.repository.{PostedDataRepository, RoomDataRepository}
import models.posted.PostedData
import models.room.RoomData
import play.api.cache.SyncCacheApi
import play.api.mvc.Result
import utils.RoomUtils.makeOrder

import scala.concurrent.ExecutionContext.Implicits.global

object ResultUtils {

  def indexResult(pageOpt: Option[Int], tagOpt: Option[String], orderOpt: Option[String])
    (f: (List[RoomData], Int) => Result)
    (implicit roomDataRepository: RoomDataRepository)= {
    val order = makeOrder(orderOpt.filter(_ != ""))
    val limit = 3
    val page = pageOpt.filter(_ >= 0).getOrElse(0) * limit
    val roomDataList = tagOpt.filter(_ != "").fold(roomDataRepository.getLatestRoom(page, limit, order))(
      roomDataRepository.getRoomTagFilter(page, _, limit, order)
    )

    roomDataList.map( list =>
      f(list, page)
    )
  }

  def roomResult(roomId: String, pageOpt: Option[Int])
    (f: (RoomData, List[PostedData], List[String], Int) => Result)
    (implicit cache: SyncCacheApi, roomDataRepository: RoomDataRepository, postedDataRepository: PostedDataRepository) = {
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

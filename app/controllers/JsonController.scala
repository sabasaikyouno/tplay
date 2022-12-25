package controllers

import domain.repository.{PostedDataRepository, RoomDataRepository}
import javax.inject._
import play.api._
import play.api.cache.SyncCacheApi
import play.api.libs.json._
import play.api.mvc._
import utils.RoomUtils.makeOrder

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class JsonController @Inject()(
  cc: ControllerComponents,
  roomDataRepository: RoomDataRepository,
  val postedDataRepository: PostedDataRepository,
  val cache: SyncCacheApi
) extends JsonUserController(cc, roomDataRepository) {

  def index(pageOpt: Option[Int], tagOpt: Option[String], orderOpt: Option[String]) = UserAction.async { implicit request =>
    val order = makeOrder(orderOpt.filter(_ != ""))
    val limit = 3
    val page = pageOpt.filter(_ >= 0).getOrElse(0) * limit
    val roomDataList = tagOpt.filter(_ != "").fold(roomDataRepository.getLatestRoom(page, limit, order))(
      roomDataRepository.getRoomTagFilter(page, _, limit, order)
    )

    roomDataList.map( list =>
      Ok(Json.obj(
        "status" -> "OK",
        "roomData" -> list
      ))
    )
  }

  def room(roomId: String, pageOpt: Option[Int]) = RoomAction(roomId).async { implicit request =>
    val limit = 3
    val page = pageOpt.filter(_ >= 0).getOrElse(0) * limit

    for {
      roomData <- roomDataRepository.getOneRoom(roomId)
      if roomData.isDefined
      postedList <- postedDataRepository.getLatestPosted(roomId, limit, page)
      tags <- roomDataRepository.getTags(roomId)
      _ <- roomDataRepository.roomViewCount(roomId)
    } yield Ok(Json.obj(
      "status" -> "OK",
      "roomData" -> roomData.get,
      "postedList" -> postedList,
      "tags" -> tags
    ))
  }
}

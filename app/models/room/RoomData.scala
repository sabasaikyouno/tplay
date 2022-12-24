package models.room

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

case class RoomData(
  id: Long,
  roomId: String,
  userId: String,
  title: String,
  viewCount: Int,
  contentType: String
)

object RoomData {
  implicit val roomDataWrites: Writes[RoomData] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "roomId").write[String] and
      (JsPath \ "userId").write[String] and
      (JsPath \ "title").write[String] and
      (JsPath \ "viewCount").write[Int] and
      (JsPath \ "contentType").write[String]
    )(unlift(RoomData.unapply))
}

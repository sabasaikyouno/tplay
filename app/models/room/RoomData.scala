package models.room

import play.api.libs.json.{Json, Reads, Writes}

case class RoomData(
  id: Long,
  roomId: String,
  userId: String,
  title: String,
  viewCount: Int,
  contentType: String
)

object RoomData {
  implicit val roomDataWrites: Writes[RoomData] = Json.writes[RoomData]
  implicit val roomDataReads: Reads[RoomData] = Json.reads[RoomData]
}

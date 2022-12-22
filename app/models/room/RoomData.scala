package models.room

case class RoomData(
  id: Long,
  roomId: String,
  userId: String,
  title: String,
  viewCount: Int,
  contentType: String
)

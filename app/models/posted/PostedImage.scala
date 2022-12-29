package models.posted

import java.time.LocalDateTime

import models.user.UserData
import play.api.libs.json.{Json, Writes}

case class PostedImage(contentId: Long, user: UserData, img: String, createdTime: LocalDateTime) extends PostedData

object PostedImage {
  implicit val postedImageWrites: Writes[PostedImage] = Json.writes[PostedImage]
}

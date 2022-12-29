package models.posted

import java.time.LocalDateTime

import models.user.UserData
import play.api.libs.json.{Json, Writes}

case class PostedText(contentId: Long, user: UserData, text: String, createdTime: LocalDateTime) extends PostedData

object PostedText {
  implicit val postedTextWrites: Writes[PostedText] = Json.writes[PostedText]
}

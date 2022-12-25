package models.posted

import java.time.LocalDateTime

import play.api.libs.json._

import models.user.UserData

trait PostedData {
  val user: UserData
  val createdTime: LocalDateTime
}

object PostedData {
  implicit val postedDataWrites: Writes[PostedData] = Writes[PostedData] {
    case x: PostedText => PostedText.postedTextWrites.writes(x)
    case x: PostedImage => PostedImage.postedImageWrites.writes(x)
  }
}

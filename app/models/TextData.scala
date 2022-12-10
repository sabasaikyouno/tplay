package models

import java.time.LocalDateTime

import play.api.data._
import play.api.data.Forms._

case class TextData(text: String)

object TextData {
  val textForm = Form(
    mapping(
      "text" -> nonEmptyText
    )(TextData.apply)(TextData.unapply)
  )
}

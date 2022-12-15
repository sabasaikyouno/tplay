package models.form

import play.api.data.Forms._
import play.api.data._

case class RoomForm(tag: Option[String])

object RoomForm {
  val roomForm = Form(
    mapping(
      "tag" -> optional(text)
    )(RoomForm.apply)(RoomForm.unapply)
  )
}

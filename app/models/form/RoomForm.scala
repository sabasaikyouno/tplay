package models.form

import play.api.data.Forms._
import play.api.data._

case class RoomForm(tag: Option[String], authUser: Option[String], contentType: Option[String])

object RoomForm {
  val roomForm = Form(
    mapping(
      "tag" -> optional(text),
      "authUser" -> optional(text),
      "contentType" -> optional(text)
    )(RoomForm.apply)(RoomForm.unapply).verifying("error contentType", {
      case RoomForm(_, _, contentType) => contentType.exists(_.split("/").forall(s => s == "text" || s == "image"))
    })
  )
}

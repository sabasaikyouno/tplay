package models.form

import play.api.data.Forms._
import play.api.data._

case class RoomForm(title: Option[String], tag: Option[String], authUser: Option[String], contentType: Option[String])

object RoomForm {
  val roomForm = Form(
    mapping(
      "title" -> optional(text),
      "tag" -> optional(text),
      "authUser" -> optional(text),
      "contentType" -> optional(text)
    )(RoomForm.apply)(RoomForm.unapply).verifying("error contentType", {
      case RoomForm(_, _, _, contentType) => contentType.isEmpty || contentType.exists(_.split("/").forall(s => s == "text" || s == "image"))
    })
  )
}

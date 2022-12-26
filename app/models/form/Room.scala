package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, Reads}

case class Room(title: Option[String], tag: Option[String], authUser: Option[String], contentType: Option[String])

object Room {
  val roomForm: Form[Room] = Form(
    mapping(
      "title" -> optional(text),
      "tag" -> optional(text),
      "authUser" -> optional(text),
      "contentType" -> optional(text)
    )(Room.apply)(Room.unapply).verifying("error contentType", {
      case Room(_, _, _, contentType) => contentType.isEmpty || contentType.exists(_.split("/").forall(s => s == "text" || s == "image"))
    })
  )

  implicit val roomReads: Reads[Room] = Json.reads[Room]
}

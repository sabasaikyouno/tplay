package models

import play.api.data._
import play.api.data.Forms._

case class SendText(text: String)

object SendText {
  val sendTextForm = Form(
    mapping(
      "text" -> nonEmptyText
    )(SendText.apply)(SendText.unapply)
  )
}

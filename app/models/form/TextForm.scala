package models.form

import play.api.data.Forms._
import play.api.data._

case class TextForm(text: String)

object TextForm {
  val textForm = Form(
    mapping(
      "text" -> nonEmptyText
    )(TextForm.apply)(TextForm.unapply)
  )
}

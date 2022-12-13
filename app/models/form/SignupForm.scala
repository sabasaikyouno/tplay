package models.form

import play.api.data.Forms._
import play.api.data._

case class SignupForm(userId: String, password: String)

object SignupForm {
  val signupForm = Form(
    mapping(
      "userId" -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignupForm.apply)(SignupForm.unapply)
  )
}

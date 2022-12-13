package models

import play.api.data._
import play.api.data.Forms._

case class SignupForm(userId: String, password: String)

object SignupForm {
  val signupForm = Form(
    mapping(
      "userId" -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignupForm.apply)(SignupForm.unapply)
  )
}

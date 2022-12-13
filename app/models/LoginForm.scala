package models

import play.api.data._
import play.api.data.Forms._

case class LoginForm(userId: String, password: String)

object LoginForm {
  val loginForm = Form(
    mapping(
      "userId" -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignupForm.apply)(SignupForm.unapply)
  )
}


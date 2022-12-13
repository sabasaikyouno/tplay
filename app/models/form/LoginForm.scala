package models.form

import play.api.data.Forms._
import play.api.data._

case class LoginForm(userId: String, password: String)

object LoginForm {
  val loginForm = Form(
    mapping(
      "userId" -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignupForm.apply)(SignupForm.unapply)
  )
}


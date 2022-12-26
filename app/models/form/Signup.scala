package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, Reads}

case class Signup(userId: String, password: String)

object Signup {
  val signupForm: Form[Signup] = Form(
    mapping(
      "userId" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Signup.apply)(Signup.unapply)
  )

  implicit val signupReads: Reads[Signup] = Json.reads[Signup]
}

package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, Reads, Writes}

case class Login(userId: String, password: String)

object Login {
  val loginForm: Form[Login] = Form(
    mapping(
      "userId" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Login.apply)(Login.unapply)
  )

  implicit val loginReads: Reads[Login] = Json.reads[Login]
  implicit val loginWrite: Writes[Login] = Json.writes[Login]
}


package models.user

import play.api.libs.json.{Json, Writes}

case class UserData(name: String)

object UserData {
  implicit val userDataWrites: Writes[UserData] = Json.writes[UserData]
}

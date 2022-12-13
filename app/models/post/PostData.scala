package models.post

import models.user.UserData

trait PostData {
  val content: String
  val contentType: String
  val roomId: String
  val user: UserData
}

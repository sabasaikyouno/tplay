package models.posted

import java.time.LocalDateTime

import models.user.UserData

trait PostedData {
  val user: UserData
  val createdTime: LocalDateTime
}

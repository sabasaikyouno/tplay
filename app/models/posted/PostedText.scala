package models.posted

import java.time.LocalDateTime

import models.user.UserData

case class PostedText(user: UserData, text: String, createdTime: LocalDateTime) extends PostedData

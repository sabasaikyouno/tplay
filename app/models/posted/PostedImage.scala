package models.posted

import java.time.LocalDateTime

import models.user.UserData

case class PostedImage(user: UserData, img: String, createdTime: LocalDateTime) extends PostedData

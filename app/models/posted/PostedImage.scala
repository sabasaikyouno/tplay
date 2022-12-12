package models.posted

import java.time.LocalDateTime

case class PostedImage(id: Long, img: String, createdTime: LocalDateTime) extends PostedData

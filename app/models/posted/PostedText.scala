package models.posted

import java.time.LocalDateTime

case class PostedText(id: Long, text: String, createdTime: LocalDateTime) extends PostedData

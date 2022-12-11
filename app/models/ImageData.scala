package models

import java.time.LocalDateTime

case class ImageData(id: Long, img: String, createdTime: LocalDateTime) extends PostedData

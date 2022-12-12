package models.posted

import java.time.LocalDateTime

trait PostedData {
  val id: Long
  val createdTime: LocalDateTime
}

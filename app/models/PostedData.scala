package models

import java.time.LocalDateTime

trait PostedData {
  val id: Long
  val createdTime: LocalDateTime
}

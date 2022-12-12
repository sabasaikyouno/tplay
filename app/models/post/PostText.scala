package models.post

case class PostText(roomId: String, content: String, contentType: String = "text") extends PostData

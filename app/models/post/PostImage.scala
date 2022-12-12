package models.post

case class PostImage(roomId: String, content: String, contentType: String = "image") extends PostData

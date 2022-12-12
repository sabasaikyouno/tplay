package models.post

case class PostText(content: String, contentType: String = "text") extends PostData

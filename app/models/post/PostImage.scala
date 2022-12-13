package models.post

import models.user.UserData

case class PostImage(roomId: String, user: UserData, content: String, contentType: String = "image") extends PostData

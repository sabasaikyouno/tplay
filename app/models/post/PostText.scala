package models.post

import models.user.UserData

case class PostText(roomId: String, user: UserData, content: String, contentType: String = "text") extends PostData

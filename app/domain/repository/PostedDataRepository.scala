package domain.repository

import models.post.PostData
import models.posted.PostedData

import scala.concurrent.Future

trait PostedDataRepository {
  def create(postData: PostData): Future[_]

  def getLatestPosted(roomId: String, limit: Int, page: Int): Future[List[PostedData]]
}

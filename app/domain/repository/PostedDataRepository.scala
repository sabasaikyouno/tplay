package domain.repository

import models.{PostData, PostedData}

import scala.concurrent.Future

trait PostedDataRepository {
  def create(postData: PostData): Future[_]

  def getLatestPosted(count: Int): Future[List[PostedData]]
}

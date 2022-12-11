package domain.repository

import models.ImageData

import scala.concurrent.Future

trait ImageDataRepository {
  def create(imgPath: String): Future[_]

  def getLatestImage(count: Int): Future[List[ImageData]]
}

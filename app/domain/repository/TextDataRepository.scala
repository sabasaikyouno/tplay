package domain.repository

import models.TextData

import scala.concurrent.Future

trait TextDataRepository {
  def create(textData: TextData): Future[_]

  def getLatestText(count: Int): Future[List[TextData]]
}

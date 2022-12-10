package domain.repository

import models.{SendText, TextData}

import scala.concurrent.Future

trait TextDataRepository {
  def create(sendText: SendText): Future[_]

  def getLatestText(count: Int): Future[List[TextData]]
}

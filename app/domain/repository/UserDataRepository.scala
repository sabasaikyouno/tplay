package domain.repository

import models.user.UserData

import scala.concurrent.Future

trait UserDataRepository {
  def signup(userId: String, password: String): Future[_]

  def login(userId: String, password: String): Future[Option[UserData]]
}

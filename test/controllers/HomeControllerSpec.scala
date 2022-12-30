package controllers

import java.util.UUID

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.db.Databases
import play.api.db.evolutions.{ClassLoaderEvolutionsReader, Evolutions}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global


class HomeControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "db.default.driver" -> "com.mysql.jdbc.Driver",
        "db.default.url" -> "jdbc:mysql://192.168.99.100:3306/test_tplay")
      .build()
  }

  "HomeController Test" should {

    val testName, testPass = UUID.randomUUID().toString
    val signup = route(app, FakeRequest(POST, "/signup").withFormUrlEncodedBody("userId" -> testName, "password" -> testPass)).get
    val login = signup.flatMap(_ => route(app, FakeRequest(POST, "/login").withFormUrlEncodedBody("userId" -> testName, "password" -> testPass)).get)
    val loginCookie = cookies(login).get("id").get

    "ログインせずhomeにアクセスすると、login_formにリダイレクトする" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe 303
      redirectLocation(home) mustBe Some("/login")
    }

    "login_form" in {
      val login_form = route(app, FakeRequest(GET, "/login")).get

      status(login_form) mustBe 200
    }

    "signup" in {
      status(signup) mustBe 303
      redirectLocation(signup) mustBe Some("/login")
    }

    "login成功した場合、homeにリダイレクトする" in {
      status(login) mustBe 303
      redirectLocation(login) mustBe Some("/")
    }

    "login失敗した場合、login_formにリダイレクトする" in {
      val login = route(app, FakeRequest(POST, "/login").withFormUrlEncodedBody("userId" -> "b", "password" -> "b")).get

      status(login) mustBe 303
      redirectLocation(login) mustBe Some("/login")
    }

    "ログインしているときのhome" in {
      val home = route(app, FakeRequest(GET, "/").withCookies(loginCookie)).get

      status(home) mustBe 200
    }
  }

  "Room Test" should {

    val testName, testPass = UUID.randomUUID().toString
    val signup = route(app, FakeRequest(POST, "/signup").withFormUrlEncodedBody("userId" -> testName, "password" -> testPass)).get
    val login = signup.flatMap(_ => route(app, FakeRequest(POST, "/login").withFormUrlEncodedBody("userId" -> testName, "password" -> testPass)).get)
    val loginCookie = cookies(login).get("id").get

    "create roomパラメーターなし" in {
      val createRoom = route(app, FakeRequest(POST, "/room").withCookies(loginCookie)).get

      status(createRoom) mustBe 303
      redirectLocation(createRoom) mustBe Some("/")
    }

    "create room パラメーターあり" in {
      val createRoom = route(app, FakeRequest(POST, "/room").withCookies(loginCookie)
        .withFormUrlEncodedBody(
          "title" -> "test",
          "tag" -> "test test2",
          "authUser" -> "a",
          "contentType" -> "text/image"
        )).get

      status(createRoom) mustBe 303
      redirectLocation(createRoom) mustBe Some("/")
    }

    "create room ログインしないと作れない" in {
      val createRoom = route(app, FakeRequest(POST, "/room")).get

      status(createRoom) mustBe 303
      redirectLocation(createRoom) mustBe Some("/login")
    }
  }
}
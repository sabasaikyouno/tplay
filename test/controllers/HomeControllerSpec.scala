package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._


class HomeControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  "HomeController Test" should {

    "ログインせずhomeにアクセスすると、login_formにリダイレクトする" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe 303
      redirectLocation(home) mustBe Some("/login_form")
    }

    "login_form" in {
      val request = FakeRequest(GET, "/login_form")
      val home = route(app, request).get

      status(home) mustBe 200
    }

    "signup" in {
      val home = route(app, FakeRequest(POST, "/signup").withFormUrlEncodedBody("userId" -> "a", "password" -> "a")).get

      status(home) mustBe 303
      redirectLocation(home) mustBe Some("/login_form")
    }

    "login成功した場合、homeにリダイレクトする" in {
      val login = route(app, FakeRequest(POST, "/login").withFormUrlEncodedBody("userId" -> "a", "password" -> "a")).get

      status(login) mustBe 303
      redirectLocation(login) mustBe Some("/")
    }

    "login失敗した場合、login_formにリダイレクトする" in {
      val login = route(app, FakeRequest(POST, "/login").withFormUrlEncodedBody("userId" -> "b", "password" -> "b")).get

      status(login) mustBe 303
      redirectLocation(login) mustBe Some("/login_form")
    }

    "ログインしているときのhome" in {
      val login = route(app, FakeRequest(POST, "/login").withFormUrlEncodedBody("userId" -> "a", "password" -> "a")).get
      val home = route(app, FakeRequest(GET, "/").withCookies(cookies(login).get("id").get)).get

      status(home) mustBe 200
    }
  }
}
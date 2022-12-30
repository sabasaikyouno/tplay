package controllers

import java.util.UUID

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Reads._

import scala.concurrent.ExecutionContext.Implicits.global

class JsonControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "db.default.driver" -> "com.mysql.jdbc.Driver",
        "db.default.url" -> "jdbc:mysql://192.168.99.100:3306/test_tplay")
      .build()
  }

  "JsonController Test" should {

    val testName, testPass = UUID.randomUUID().toString
    val signup = route(app, FakeRequest(POST, "/json/signup").withJsonBody(Json.obj("userId" -> testName, "password" -> testPass))).get
    val login = signup.flatMap(_ => route(app, FakeRequest(POST, "/json/login").withJsonBody(Json.obj("userId" -> testName, "password" -> testPass))).get)
    val loginJson = contentAsJson(login).as[Map[String, String]]

    "signup" in {
      status(signup) mustBe OK
    }

    "login" in {
      status(login) mustBe OK
      contentAsJson(login).as[Map[String, String]].isDefinedAt("id") mustBe true
    }

    "createRoom" in {
      val createRoom = route(app, FakeRequest(POST, "/json/room").withJsonBody(Json.obj("title" -> "testTitle")).withHeaders("id" -> loginJson("id"))).get

      status(createRoom) mustBe OK
      contentAsJson(createRoom).as[Map[String, String]].isDefinedAt("roomId") mustBe true
    }

    "room" in {
      for {
        createRoom <- route(app, FakeRequest(POST, "/json/room").withJsonBody(Json.obj("title" -> "testTitle")).withHeaders("id" -> loginJson("id")))
        createRoomJson = contentAsJson(createRoom).as[Map[String, String]]
        room <- route(app, FakeRequest(GET, s"/json/room/${createRoomJson("roomId")}").withHeaders("id" -> loginJson("id")))
      } yield {
        status(room) mustBe OK
      }
    }
  }
}

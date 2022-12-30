package controllers

import java.util.UUID

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
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

    val defined = 'defined
    val testName, testPass = UUID.randomUUID().toString
    val signup = route(app, FakeRequest(POST, "/json/signup").withJsonBody(Json.obj("userId" -> testName, "password" -> testPass))).get
    val login = signup.flatMap(_ => route(app, FakeRequest(POST, "/json/login").withJsonBody(Json.obj("userId" -> testName, "password" -> testPass))).get)
    val loginJson = contentAsJson(login)
    val loginHeader = "id" -> (loginJson \ "id").as[String]

    "signup" in {
      status(signup) mustBe OK
    }

    "login" in {
      status(login) mustBe OK
      (loginJson \ "id").asOpt[String] mustBe defined
    }

    "createRoom" in {
      val createRoom = route(app, FakeRequest(POST, "/json/room").withJsonBody(Json.obj("title" -> "testTitle")).withHeaders(loginHeader)).get
      val json = contentAsJson(createRoom)

      status(createRoom) mustBe OK
      (json \ "roomId").asOpt[String] mustBe defined
    }

    "room" in {
      for {
        createRoom <- route(app, FakeRequest(POST, "/json/room").withJsonBody(Json.obj("title" -> "testTitle")).withHeaders(loginHeader))
        createRoomJson = contentAsJson(createRoom).as[Map[String, String]]
        room <- route(app, FakeRequest(GET, s"/json/room/${createRoomJson("roomId")}").withHeaders(loginHeader))
      } yield {
        status(room) mustBe OK
      }
    }
  }
}

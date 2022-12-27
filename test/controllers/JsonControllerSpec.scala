package controllers

import models.posted.PostedData
import models.room.RoomData
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{JsPath, Json}
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._


class JsonControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  "JsonController Test" should {

    "signup" in {
      val signup = route(app, FakeRequest(POST, "/json/signup").withJsonBody(Json.obj("userId" -> "jsona", "password" -> "jsona"))).get

      status(signup) mustBe OK
    }

    "login" in {
      val login = route(app, FakeRequest(POST, "/json/login").withJsonBody(Json.obj("userId" -> "jsona", "password" -> "jsona"))).get

      status(login) mustBe OK
      contentAsJson(login).as[Map[String, String]].isDefinedAt("id") mustBe true
    }

    "createRoom" in {
      val login = route(app, FakeRequest(POST, "/json/login").withJsonBody(Json.obj("userId" -> "jsona", "password" -> "jsona"))).get
      val loginJson = contentAsJson(login).as[Map[String, String]]
      val createRoom = route(app, FakeRequest(POST, "/json/room").withJsonBody(Json.obj("title" -> "testTitle")).withHeaders("id" -> loginJson("id"))).get

      status(createRoom) mustBe OK
      contentAsJson(createRoom).as[Map[String, String]].isDefinedAt("roomId") mustBe true
    }

    "room" in {
      for {
        login <- route(app, FakeRequest(POST, "/json/login").withJsonBody(Json.obj("userId" -> "jsona", "password" -> "jsona")))
        loginJson = contentAsJson(login).as[Map[String, String]]
        createRoom <- route(app, FakeRequest(POST, "/json/room").withJsonBody(Json.obj("title" -> "testTitle")).withHeaders("id" -> loginJson("id")))
        createRoomJson = contentAsJson(createRoom).as[Map[String, String]]
        room <- route(app, FakeRequest(GET, s"/json/room/${createRoomJson("roomId")}").withHeaders("id" -> loginJson("id")))
      } yield {
        status(room) mustBe OK
        contentAsJson(room) mustBe Json.obj(
          "status" -> "OK",
          "roomData" -> RoomData(4L, createRoomJson("roomId"), "jsona", "testTitle", 0, "text/image"),
          "postedList" -> List[PostedData](),
          "tags" -> List("noTag"))
      }
    }
  }
}

package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.ws._
import play.api.libs.json.{ JsResult, Json }
import models.{NewShortLinkEntry, ShortLinkEntry}
import play.api.mvc.Result

import scala.concurrent.Future
class ShortLinkControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  implicit val shorLinkJson = Json.format[ShortLinkEntry]
  implicit val newShorLinkJson = Json.format[NewShortLinkEntry]

  "ShortLinkController " should {

    "return valid http code and content type" in {
      val request = FakeRequest(GET, "/all")
      val all:Future[Result] = route(app, request).get

      status(all) mustBe OK
      contentType(all) mustBe Some("application/json")
      contentAsString(all) must include ("journiapp")
    }

    "return dummy entries in proper json format" in {
      val request = FakeRequest(GET, "/all")
      val all:Future[Result] = route(app, request).get

      val shortLinkItems: Seq[ShortLinkEntry] = Json.fromJson[Seq[ShortLinkEntry]](contentAsJson(all)).get
      shortLinkItems.filter(_.shortCode == "DummYShort").head mustBe (ShortLinkEntry("DummYShort","https://journiapp.com" ))
    }

    "return url mapping when shortcode is give in decode request" in {
      val request = FakeRequest(GET, "/decode/ShoRTdummY")
      val decodeResult:Future[Result] = route(app, request).get
      val shortLinkItems: ShortLinkEntry = Json.fromJson[ShortLinkEntry](contentAsJson(decodeResult)).get

      shortLinkItems.url mustBe "https://bambus.io"
      shortLinkItems.shortCode.size mustBe 10
    }

    "create new entry when new url is sent for encoding" in {
        val jsonBody = Json.obj("url" -> "https://google.com")
        val request = FakeRequest(
                  method = "POST",
                  uri =  "/encode",
                  headers = FakeHeaders(List("HOST" ->"localhost", "content-type" ->"text/json")),
                  body =  jsonBody
                )
      val encodeResult:Future[Result] = route(app, request).get
      val shortLinkItems: ShortLinkEntry = Json.fromJson[ShortLinkEntry](contentAsJson(encodeResult)).get

      shortLinkItems.url mustBe "https://google.com"
      shortLinkItems.shortCode.size mustBe 10
    }

  }

}

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

    "return 404 when shortcode given in decode request was never saved" in {
      val requestBadShortCode = FakeRequest(GET, "/decode/badShortCodeNeverSent")
      val decodeResultBadShortCode:Future[Result] = route(app, requestBadShortCode).get
      status(decodeResultBadShortCode) mustBe 404
    }

    "create new entry when new url is sent for encoding" in {
      val jsonBody = Json.obj("url" -> "https://www.journiapp.com/photo-book")
      val request = FakeRequest(
                method = "POST",
                uri =  "/encode",
                headers = FakeHeaders(List("HOST" ->"localhost", "content-type" ->"text/json")),
                body =  jsonBody
              )
      val encodeResult:Future[Result] = route(app, request).get
      val shortLinkItems: ShortLinkEntry = Json.fromJson[ShortLinkEntry](contentAsJson(encodeResult)).get

      shortLinkItems.url mustBe "https://www.journiapp.com/photo-book"
      shortLinkItems.shortCode.size mustBe 10

      // Verify that new url is present in the list of all urls
      val requestAfterAdd = FakeRequest(GET, "/all")
      val allAfterAdd:Future[Result] = route(app, requestAfterAdd).get

      val shortLinkItemsAfterAdd: Seq[ShortLinkEntry] = Json.fromJson[Seq[ShortLinkEntry]](contentAsJson(allAfterAdd)).get
      shortLinkItemsAfterAdd.size mustBe 3
      shortLinkItemsAfterAdd.exists(_.url == "https://www.journiapp.com/photo-book") mustBe true

      // Verify a url never sent is not in the list
      shortLinkItemsAfterAdd.exists(_.url == "https://example.com/badurlthatisneversent") mustBe false

    }

  }

}

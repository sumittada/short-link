package controllers

import javax.inject._
import scala.util.Random
import models.{NewShortLinkEntry, ShortLinkEntry}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import scala.collection.mutable

@Singleton
class ShortLinkController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  private val shortLinkList = new mutable.ListBuffer[ShortLinkEntry]()

  // dummy items to allow intial fetching before new links are added
  shortLinkList += ShortLinkEntry("DummYShort", "https://journiapp.com")
  shortLinkList += ShortLinkEntry("ShoRTdummY", "https://bambus.io")

  implicit val shorLinkJson = Json.format[ShortLinkEntry]
  implicit val newShorLinkJson = Json.format[NewShortLinkEntry]

  // /all
  def getAll(): Action[AnyContent] = Action {
    if (shortLinkList.isEmpty) NoContent else Ok(Json.toJson(shortLinkList))
  }

  // /decode/xxxx
  def getByShortCode(shortCode: String) = Action {
    val foundItem = shortLinkList.find(_.shortCode == shortCode)
    foundItem match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

 def uniqueShortCode(shortLinkList: mutable.ListBuffer[ShortLinkEntry]): String = {
    val newShortCode = Random.alphanumeric.take(10).mkString("")
    if (shortLinkList.exists(_.shortCode == newShortCode)) uniqueShortCode(shortLinkList)
    else newShortCode
  }

  // /encode
  def addNewShortLink() = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson

    val shortLinkEntry: Option[NewShortLinkEntry] = jsonObject.flatMap(Json.fromJson[NewShortLinkEntry](_).asOpt)

    shortLinkEntry match {
      case Some(newItem) =>
        val foundItem = shortLinkList.find(_.url == newItem.url)
        foundItem match {
          case Some(item) => Ok(Json.toJson(item))
          case None => {
            val newShortCode = uniqueShortCode(shortLinkList)
            val toBeAdded = ShortLinkEntry(newShortCode, newItem.url)
            shortLinkList += toBeAdded
            Created(Json.toJson(toBeAdded))
          }
        }
      case None =>
        BadRequest
    }
  }
}

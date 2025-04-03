package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json.{Json, OFormat, JsValue, JsSuccess, JsError}

import scala.collection.mutable

case class Person(name: String, age: Int, gender: String, verified: Boolean)

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class CrudController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  private val persons = mutable.ListBuffer[Person]()
  
  implicit val personFormat: OFormat[Person] = Json.format[Person]

def createPerson() = Action { implicit request: Request[AnyContent] =>
  request.body.asJson match {
    case Some(json) =>
      json.validate[Person] match {
        case JsSuccess(person, _) =>
          persons += person
          Created(Json.toJson(person))
        case JsError(errors) =>
          BadRequest(Json.obj(
            "error" -> "Invalid person data",
            "details" -> JsError.toJson(errors)
          ))
      }
    case None =>
      BadRequest(Json.obj("error" -> "Expecting JSON data"))
  }
}

  def getAllPersons() = Action {
    val jsonResponse = Json.toJson(persons.zipWithIndex.map { case (person, index) =>
      Json.obj("index" -> index, "person" -> person)
    })
    Ok(jsonResponse)
  }

  def updatePerson(index: Int) = Action { implicit request: Request[AnyContent] =>
    if (index < 0 || index >= persons.length) {
      NotFound(Json.obj("error" -> "Person not found"))
    } else {
      val json = request.body.asJson.get
      val updatedPerson = json.as[Person]
      persons.update(index, updatedPerson)
      Ok(Json.toJson(updatedPerson))
    }
  }

  def deletePerson(index: Int) = Action {
    if (index < 0 || index >= persons.length) {
      NotFound(Json.obj("error" -> "Person not found"))
    } else {
      persons.remove(index)
      NoContent
    }
  }
}
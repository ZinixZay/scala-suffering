package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._

class CrudControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  // Тестовые данные
  val testPerson1 = Json.obj(
    "name" -> "Alice",
    "age" -> 30,
    "gender" -> "female",
    "verified" -> true
  )
  
  val testPerson2 = Json.obj(
    "name" -> "Bob",
    "age" -> 25,
    "gender" -> "male",
    "verified" -> false
  )

  "CrudController" should {

    "create a new person" in {
      val controller = inject[CrudController]
      val request = FakeRequest(POST, "/create")
        .withJsonBody(testPerson1)
      
      val result = controller.createPerson().apply(request)
      
      status(result) mustBe CREATED
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe testPerson1
    }

    "return all persons" in {
      val controller = inject[CrudController]
      
      // Сначала создадим тестовые данные
      controller.createPerson().apply(FakeRequest(POST, "/create").withJsonBody(testPerson1))
      controller.createPerson().apply(FakeRequest(POST, "/create").withJsonBody(testPerson2))
      
      val result = controller.getAllPersons().apply(FakeRequest(GET, "/getAll"))
      
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      val jsonResult = contentAsJson(result)
      (jsonResult(0) \ "person").get mustBe testPerson1
      (jsonResult(1) \ "person").get mustBe testPerson2
    }

    "update an existing person" in {
      val controller = inject[CrudController]
      
      // Сначала создадим тестовые данные
      controller.createPerson().apply(FakeRequest(POST, "/create").withJsonBody(testPerson1))
      
      val updatedPerson = Json.obj(
        "name" -> "Alice Updated",
        "age" -> 31,
        "gender" -> "female",
        "verified" -> false
      )
      
      val request = FakeRequest(PUT, "/update/0")
        .withJsonBody(updatedPerson)
      
      val result = controller.updatePerson(0).apply(request)
      
      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe updatedPerson
    }

    "return 404 when trying to update non-existent person" in {
      val controller = inject[CrudController]
      val request = FakeRequest(PUT, "/update/999")
        .withJsonBody(testPerson1)
      
      val result = controller.updatePerson(999).apply(request)
      
      status(result) mustBe NOT_FOUND
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Person not found"
    }

    "delete an existing person" in {
      val controller = inject[CrudController]
      
      // Сначала создадим тестовые данные
      controller.createPerson().apply(FakeRequest(POST, "/create").withJsonBody(testPerson1))
      
      val result = controller.deletePerson(0).apply(FakeRequest(DELETE, "/delete/0"))
      
      status(result) mustBe NO_CONTENT
    }

    "return 404 when trying to delete non-existent person" in {
      val controller = inject[CrudController]
      val result = controller.deletePerson(999).apply(FakeRequest(DELETE, "/delete/999"))
      
      status(result) mustBe NOT_FOUND
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Person not found"
    }

    "return bad request when creating with invalid JSON" in {
      val controller = inject[CrudController]
      val invalidJson = Json.obj("invalid" -> "data")
      val request = FakeRequest(POST, "/create")
        .withJsonBody(invalidJson)
      
      val result = controller.createPerson().apply(request)
      
      status(result) mustBe BAD_REQUEST
    }
  }
}
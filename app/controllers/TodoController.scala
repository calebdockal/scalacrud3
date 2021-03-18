package controllers

import javax.inject._
import models.{NewTodoListItem, TodoListItem}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import scala.collection.mutable


// Singleton tells framework to define one instance
// Inject instructs framework to pass required dependencies

// Action gives me the ability to receive HTTP responses

// This todo controller will define a collection of in-memory tasks. Returns JSON
// JSON formatter converts the object into JSON
@Singleton
class TodoListController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  private val todoList = new mutable.ListBuffer[TodoListItem]()
  todoList += TodoListItem(1, "test", true)
  todoList += TodoListItem(2, "some other value", false)


  implicit val todoListJson = Json.format[TodoListItem]
  
  implicit val newTodoListJson = Json.format[NewTodoListItem]

  // curl localhost:9000/todo
  // this function will return nothing if no content exists, otherwise it will return the JSON list
  def getAll(): Action[AnyContent] = Action {
    if (todoList.isEmpty) NoContent else Ok(Json.toJson(todoList))
  }

  // curl localhost:9000/todo/1
  // method will get the item ID and find it in the list. Otherwise return 404
  def getById(itemId: Long) = Action {
    val foundItem = todoList.find(_.id == itemId)
    foundItem match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  // curl -X PUT localhost:9000/todo/done/1
  // takes an old item in JSON and replaces item with copy, this is meant for delete functionality
  def markAsDone(itemId: Long) = Action {
    val foundItem = todoList.find(_.id == itemId)
    foundItem match {
      case Some(item) =>
        val newItem = item.copy(isItDone = true)
        todoList.dropWhileInPlace(_.id == itemId)
        todoList += newItem
        Accepted(Json.toJson(newItem))
      case None => NotFound
    }
  }

  // curl -X DELETE localhost:9000/todo/done
  // once an item is replaced in JSON by the updated "Done" item, that new item can be deleted. 
  def deleteAllDone() = Action {
    todoList.filterInPlace(_.isItDone == false)
    Accepted
  }

  // curl -v -d '{"description": "some new item"}' -H 'Content-Type: application/json' -X POST localhost:9000/todo
  // when a post request is sent, an id is generated and the item will be added.
  // error handling if http is not valid
  def addNewItem() = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson

    val todoListItem: Option[NewTodoListItem] = jsonObject.flatMap(Json.fromJson[NewTodoListItem](_).asOpt)

    todoListItem match {
      case Some(newItem) =>
        val nextId = todoList.map(_.id).max + 1
        val toBeAdded = TodoListItem(nextId, newItem.description, false)
        todoList += toBeAdded
        Created(Json.toJson(toBeAdded))
      case None =>
        BadRequest
    }
  }
}
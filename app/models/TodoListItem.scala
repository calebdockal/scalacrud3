package models

case class TodoListItem(id: Long, description: String, isItDone: Boolean)

// New DTO with description field. 
case class NewTodoListItem(description: String)
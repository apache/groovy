import grails.pageflow.*;
import java.util.*;

class BooksPageFlow {

   String bookDetailName = "bookDetail"
   String bookSequenceName = "bookSequence"
   Map bookDetailFormAction = [
      class:BookDetailCommand.class,
      name:bookDetailName
   ]

   Closure loadBooks = {
         requestContext ->
	 requestContext.flowScope["books"] = [
            new Book(id:1, title:"The Da Vinci Code", author:"Dan Brown"),
            new Book(id:2, title:"Deception Point", author:"Dan Brown"),
            new Book(id:3, title:"Digital Fortress", author:"Dan Brown"),
            new Book(id:4, title:"Angels And Demons", author:"Dan Brown")
         ]
	 requestContext.flowScope[bookSequenceName] = new Integer(4)
	 return "success"
      }

   Closure getBooks = {
         requestContext ->
	 return requestContext.flowScope["books"]
      }

   Closure findBook = {
         requestContext ->
	 def bookCommand = requestContext.requestScope[bookDetailName]
	 for (book in getBooks.call(requestContext)) {
            if (book.id == bookCommand.id) {
               println("Found book id:" + book.id)
	       bookCommand.title = book.title
               bookCommand.author = book.author
               break
	    }
	 }
	 return "success"
      }

   Closure saveBook = {
          requestContext ->
          def bookCommand = requestContext.requestScope[bookDetailName]
          for (book in getBooks.call(requestContext)) {
             if (book.id = bookCommand.id) {
                println("Saving book id:" + book.id)
                book.title = bookCommand.title
                book.author = bookCommand.author
                break
             }
          }
          return "success"
      }

   Closure addBook = {
          requestContext ->
	  def sequence = requestContext.flowScope[bookSequenceName]
	  int id  = sequence.intValue()
	  id = id + 1
	  requestContext.flowScope[bookSequenceName] = new Integer(id)
	  def bookCommand = requestContext.requestScope[bookDetailName]
	  def title = bookCommand.title
	  def author = bookCommand.author
	  Book book = new Book(id:id, title:title, author:author)
	  requestContext.flowScope["books"].add(book)
	  return "success"
      }

   @Property Flow flow = new PageFlowBuilder().flow {
      loadBooks(action:loadBooks) {
         success("listBooks")
      }
      listBooks(view:"listBooks", model:[books:getBooks]) {
         detail("bookDetailBind")
	 addBook("addBookViewBind")
      }
      bookDetailBind(action:bookDetailFormAction) {
         success("bookDetailFind")
      }
      bookDetailFind(action:findBook) {
         success("bookDetailView")
      }
      bookDetailView(view:"bookDetail") {
         close("listBooks")
         save("saveBookDetailBind")
      }
      saveBookDetailBind(action:bookDetailFormAction) {
         success("saveBookDetail")
      }
      saveBookDetail(action:saveBook) {
         success("bookDetailView")
      }
      addBookViewBind(action:bookDetailFormAction) {
         success("addBookView")
      }
      addBookView(view:"bookDetail") {
         save("addBookBind")
      }
      addBookBind(action:bookDetailFormAction, method:"bindAndValidate") {
         success("addBook")
	 error("addBookView")
      }
      addBook(action:addBook) {
         success("listBooks")
      }
   }
}

class Book {
   @Property int id
   @Property String title
   @Property String author
}

class BookDetailCommand {
   @Property int id = 0
   @Property String title
   @Property String author
}

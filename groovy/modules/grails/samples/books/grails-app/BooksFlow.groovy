import grails.pageflow.*;
import java.util.*;

// todo: class name doesn't match filename (?)
class BooksPageFlow {

    final String BOOK_DETAIL_KEY        = 'bookDetail'
    final String BOOK_SEQUENCE_NO_KEY   = 'bookSequence'
    final String BOOK_COLLECTION_KEY    = 'books'
    final String SUCCESS_TOKEN          = 'success'

    Map bookDetailFormAction = [
        class:  BookDetailCommand.class,
        name:   BOOK_DETAIL_KEY
    ]

    Closure loadBooks = { requestContext ->
        requestContext.flowScope[BOOK_COLLECTION_KEY] = [
            new Book(id:1, title:'The Da Vinci Code',   author:'Dan Brown'),
            new Book(id:2, title:'Deception Point',     author:'Dan Brown'),
            new Book(id:3, title:'Digital Fortress',    author:'Dan Brown'),
            new Book(id:4, title:'Angels And Demons',   author:'Dan Brown')
        ]
        requestContext.flowScope[BOOK_SEQUENCE_NO_KEY] = 4
        return SUCCESS_TOKEN
    }

    Closure getBooks = { requestContext ->
       return requestContext.flowScope[BOOK_COLLECTION_KEY]
    }

    Closure findBook = { requestContext ->
        def bookCommand = requestContext.requestScope[BOOK_DETAIL_KEY]
        def book = getBooks(requestContext).find{it.id == bookCommand.id}
        if (book) {
            println('Found book id:' + book.id)
            bookCommand.title = book.title
            bookCommand.author = book.author
        }
        return SUCCESS_TOKEN
        // todo: what about error token when no book found?
    }

    // todo: this is same logic as findBook (?)
    Closure saveBook = { requestContext ->
        def bookCommand = requestContext.requestScope[BOOK_DETAIL_KEY]
    	def book = getBooks(requestContext).find{it.id == bookCommand.id}
        if (book) {
            println('Saving book id:' + book.id)
            book.title = bookCommand.title
            book.author = bookCommand.author
        }
        return SUCCESS_TOKEN
    }

	Closure getSequence = { requestContext ->
        def sequence = requestContext.flowScope[BOOK_SEQUENCE_NO_KEY]
        sequence++
        requestContext.flowScope[BOOK_SEQUENCE_NO_KEY] = sequence
		return sequence
	}

    Closure addBook = { requestContext ->
    	def sequence = getSequence(requestContext)
        def bookCommand = requestContext.requestScope[BOOK_DETAIL_KEY]
        def title = bookCommand.title
        def author = bookCommand.author
        Book book = new Book(id:sequence, title:title, author:author)
        requestContext.flowScope[BOOK_COLLECTION_KEY].add(book)
        return SUCCESS_TOKEN
    }

    @Property Flow flow = new PageFlowBuilder().flow {
        loadBooks(action:loadBooks) {
            success('listBooks')
        }
        listBooks(view:'listBooks', model:[books:getBooks]) {
            detail('bookDetailBind')
            addBook('addBookViewBind')
        }
        bookDetailBind(action:bookDetailFormAction) {
            success('bookDetailFind')
        }
        bookDetailFind(action:findBook) {
            success('bookDetailView')
        }
        bookDetailView(view:'bookDetail') {
            close('listBooks')
            save('saveBookDetailBind')
        }
        saveBookDetailBind(action:bookDetailFormAction) {
            success('saveBookDetail')
        }
        saveBookDetail(action:saveBook) {
            success('bookDetailView')
        }
        addBookViewBind(action:bookDetailFormAction) {
            success('addBookView')
        }
        addBookView(view:'bookDetail') {
            save('addBookBind')
            close('listBooks')
        }
        addBookBind(action:bookDetailFormAction, method:'bindAndValidate') {
            success('addBook')
            error('addBookView')
        }
        addBook(action:addBook) {
            success('listBooks')
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

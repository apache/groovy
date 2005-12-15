import grails.pageflow.*;
import java.util.*;

// todo: class name doesn't match filename (?)
class BooksPageFlow {

    final String BOOK_DETAIL_KEY        = 'bookDetail'
    final String BOOK_SEQUENCE_NO_KEY   = 'bookSequence'
    final String BOOK_COLLECTION_KEY    = 'books'
    final String AUTHOR_SEQUENCE_NO_KEY = 'authorSequence'
    final String AUTHOR_COLLECTION_KEY = 'authors'
    final String SUCCESS_TOKEN          = 'success'
	final Author DAN_BROWN = new Author(id:1, name:'Dan Brown')

    Map bookDetailFormAction = [
        class:  BookDetailCommand.class,
        name:   BOOK_DETAIL_KEY,
        validator: { target, errors -> }
    ]

    Map bookDetailFormActionScope = [
        class:  BookDetailCommand.class,
        name:   BOOK_DETAIL_KEY,
        scope: 'flow'
    ]


    Closure loadBooks = { requestContext ->
        requestContext.flowScope[BOOK_COLLECTION_KEY] = [
            new Book(id:1, title:'The Da Vinci Code',   author:DAN_BROWN),
            new Book(id:2, title:'Deception Point',     author:DAN_BROWN),
            new Book(id:3, title:'Digital Fortress',    author:DAN_BROWN),
            new Book(id:4, title:'Angels And Demons',   author:DAN_BROWN)
        ]
        requestContext.flowScope[BOOK_SEQUENCE_NO_KEY] = 4
        requestContext.flowScope[AUTHOR_COLLECTION_KEY] = [ DAN_BROWN ]
        requestContext.flowScope[AUTHOR_SEQUENCE_NO_KEY] = 1
        return SUCCESS_TOKEN
    }

// Calling this closure from findBook doesn't work, don't know why
// groovy.lang.MissingMethodException: No signature of method BooksPageFlow$_closure3.getBooks() is applicable for argument types: [...]
    Closure getBooks = { requestContext ->
       return getBooks(requestContext)
    }

	def getBooks(requestContext) {
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

// Calling this closure from addBook doesn't work, don't know why.
// groovy.lang.MissingMethodException: No signature of method BooksPageFlow$_closure6.getSequence() is applicable for argument types: [...]
//	Closure getSequence = { requestContext ->
//        def sequence = requestContext.flowScope[BOOK_SEQUENCE_NO_KEY]
//        sequence++
//        requestContext.flowScope[BOOK_SEQUENCE_NO_KEY] = sequence
//		return sequence
//	}

	def getSequence(requestContext) {
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

    @Property grails.pageflow.Flow flow = new PageFlowBuilder().flow {
        loadBooks(action:loadBooks) {
            success('listBooks')
        }
        listBooks(view:'listBooks', model:[books:getBooks]) {
            detail('bookDetailBind')
            addBook('addBookViewBind')
            endNoView('endNoView')
            endView('endView')
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
            select('selectAuthorBind')
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
            select('selectAuthorBind')
        }
        addBookBind(action:bookDetailFormAction) {
            success('addBook')
            error('addBookView')
        }
        addBook(action:addBook) {
            success('listBooks')
        }
        selectAuthorBind(action:bookDetailFormActionScope) {
        	success('selectAuthor')
        }
        selectAuthor(
        	subflow:'authors',
        	input:{ ctx -> 
        		[ 
        			authors:ctx.flowScope[AUTHOR_COLLECTION_KEY],
        			authorSequence:ctx.flowScope[AUTHOR_SEQUENCE_NO_KEY] 
        		] 
        	},
        	output:{ ctx -> 
        		[
        			selectedAuthor:ctx.requestScope['selectedAuthor'],
        			AUTHOR_SEQUENCE_NO_KEY:ctx.flowScope['authorSequence'],
        			AUTHOR_COLLECTION_KEY:ctx.flowScope['authors']
        		] 
        }) {
        	end('bindSelectedAuthor')
        }
        bindSelectedAuthor(
        	action:{ ctx -> 
        		ctx.flowScope[BOOK_DETAIL_KEY].author = ctx.flowScope['selectedAuthor']
        		ctx.requestScope[BOOK_DETAIL_KEY] = ctx.flowScope[BOOK_DETAIL_KEY]
        		return SUCCESS_TOKEN 
        }) {
        	success('returnToDetailView')
        }
        returnToDetailView(action:{ ctx -> if (ctx.requestScope[BOOK_DETAIL_KEY].id > 0) { return 'edit' } else { return 'add' } }) {
        	edit('bookDetailView')
        	add('addBookView')
        }
        endNoView()
        endView(view:'end',end:true)
    }
}

class Book {
    @Property int id
    @Property String title
    @Property Author author
}

class BookDetailCommand {
    @Property int id = 0
    @Property String title
    @Property Author author = new Author()
}

class Author {
	@Property int id = 0;
	@Property String name;
}

class AuthorDetailCommand {
	@Property int id = 0
	@Property String name
}

class AuthorsPageFlow {

    final String AUTHOR_COLLECTION_KEY = 'authors'
    final String SUCCESS_TOKEN          = 'success'
	final String AUTHOR_DETAIL_KEY = 'authorDetail'
	final String SELECTED_AUTHOR = 'selectedAuthor'

	Map authorDetailFormAction = [
		class:AuthorDetailCommand,
		name:AUTHOR_DETAIL_KEY
	]

	def getAuthors(requestContext) {
		return requestContext.flowScope[AUTHOR_COLLECTION_KEY]
	}

	Closure getAuthors = { requestContext ->
		return getAuthors(requestContext)
	}

	def getSequence(requestContext) {
		def sequence = requestContext.flowScope['authorSequence']
		sequence++
		requestContext.flowScope['authorSequence'] = sequence
		return sequence
	}

	Closure selectAuthor = { requestContext ->
		def authorDetail = requestContext.requestScope[AUTHOR_DETAIL_KEY]
		requestContext.requestScope[SELECTED_AUTHOR] = getAuthors(requestContext).find { it.id == authorDetail.id }
		return SUCCESS_TOKEN
	}

	Closure saveAuthor = { requestContext ->
		def sequence = getSequence(requestContext)
		def authorDetail = requestContext.requestScope[AUTHOR_DETAIL_KEY]
		getAuthors(requestContext).add(new Author(id:sequence,name:authorDetail.name))
		return SUCCESS_TOKEN;
	}

	@Property boolean accessible = false
	@Property grails.pageflow.Flow flow = new PageFlowBuilder().flow {
		listAuthors(view:'listAuthors',model:[authors:getAuthors]) {
			select('selectAuthorBind')
			add('addAuthorBind')
		}
		selectAuthorBind(action:authorDetailFormAction) {
			success('selectAuthor')
		}
		selectAuthor(action:selectAuthor) {
			success('end')
		}
		addAuthorBind(action:authorDetailFormAction) {
			success('addAuthorView')
		}
		addAuthorView(view:'authorDetail') {
			close('listAuthors')
			save('saveNewAuthorBind')			
		}
		saveNewAuthorBind(action:authorDetailFormAction) {
			success('saveNewAuthor')
			error('addAuthorView')
		}
		saveNewAuthor(action:saveAuthor) {
			success('listAuthors')
		}
		end()
	}
}
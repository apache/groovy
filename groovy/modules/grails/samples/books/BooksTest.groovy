// to be called by build.groovy

class BooksTest extends WebTest {

    // Unlike unit test, functional tests are often sequence dependent.
    // Specify that sequence here.
    void suite() {
        testInitialBooks()
        testAddBook()
     }
     
    def testInitialBooks(){
        def bookTitles = [
            'The Da Vinci Code',
            'Deception Point',
            'Digital Fortress',
            'Angels And Demons'
        ]
        webtest('books: Run through the details of the 4 inital books'){
            invoke(url:'books')
            def index = 0
            bookTitles.each { title -> index++                  // starts at 1
                ant.group(description:"Test Book No. $index: $title"){
                    verifyTitle(text:'Book list')
                    selectForm(index: index)
                    clickButton(label:'Details')
                    verifyTitle(text:'Book detail')
                    verifyXPath(
                        description:"hidden index id must be $index",
                        xpath:"//input[@type='hidden'][@name='id'][@value='$index']")
                    verifyInputField(name:'title', value: title)
                    verifyText(text:'Dan Brown')
                    // go into author details here
                    clickButton(label:'Save')
                    verifyTitle(text:'Book detail')
                    clickButton(label:'Close')
    }   }   }   }

    def addAuthor(name){
        ant.group(description:"adding a new author to the authors list with name '$name'"){
            clickButton(label:'Add author ...')
            setInputField(name:'name', value: name)
            clickButton(label:'Save')
        }
    }

    def addBook(title, author){
        ant.group(description:"adding a new book with title '$title' and new author '$author'"){
            clickButton(label:'Add book ...')
            verifyTitle(text:'Book detail')
            setInputField(name:'title', value: title)
            clickButton(label:'...', description:'see the list of authors')
                addAuthor(author) // todo: also try with ö
            clickButton(htmlId:'detail2', description: 'choose new author')
            clickButton(label:'Save')
        }
    }

    def testAddBook() {
        webtest('books: add new book, make sure it\'s there'){
            invoke(url:'books')
            verifyTitle(text:'Book list')
                addBook('Groovy in Action', 'Dierk Koenig et al.')
            verifyTitle(text:'Book list')
            verifyText(text:'Groovy in Action.*Dierk Koenig et al.', regex:true)
            clickButton(label:'End')
    }   }

}
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
                    verifyInputField(name:'title', value:title)
                    verifyInputField(name:'author', value:'Dan Brown')
                    clickButton(label:'Save')
                    verifyTitle(text:'Book detail')
                    clickButton(label:'Close')
    }   }   }   }

    def testAddBook() {
        webtest('books: add new book, make sure it\'s there'){
            invoke(url:'books')
            verifyTitle(text:'Book list')
            clickButton(label:'Add book ...')

            verifyTitle(text:'Book detail')
            setInputField(name:'title', value:'Groovy in Action')
            setInputField(name:'author', value:'Dierk Koenig et al.')
            clickButton(label:'Save')

            verifyTitle(text:'Book list')
            selectForm(index: '5')
            clickButton(label:'Details')

            verifyXPath(
                description:"hidden index id must be 5",
                xpath:"//input[@type='hidden'][@name='id'][@value='5']")
            verifyInputField(name:'title', value:'Groovy in Action')
            verifyInputField(name:'author', value:'Dierk Koenig et al.')
    }   }

}
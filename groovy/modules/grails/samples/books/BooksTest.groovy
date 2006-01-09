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
                    verifyText(text:'Dan Brown')
                    // go into author details here
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
            clickButton(label:'...')
            clickButton(label:'Add author ...')
            setInputField(name:'name', value:'Dierk Koenig et al.') // todo: also try with ö
            clickButton(label:'Save')
            clickButton(htmlId:'detail2')
            
            clickButton(label:'Save')

            verifyTitle(text:'Book list')
            verifyText(text:'Groovy in Action.*Dierk Koenig et al.', regex:true)
            clickButton(label:'End')
    }   }

}
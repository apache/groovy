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
        ant.testSpec(name:'books: Run through the details of the 4 inital books'){
            config(configMap)
            steps(){
                invoke(url:'books')
                def index = 0
                bookTitles.each { title -> index++                  // starts at 1
                    ant.group(description:"Test Book No. $index"){
                        verifyTitle(text:'Books list')
                        selectForm(index: index)
                        clickButton(label:'Details')
                        verifyTitle(text:'Books detail')
                        verifyXPath(
                            description:"hidden index id must be $index",
                            xpath:"//input[@type='hidden'][@name='id'][@value='$index']")
                        verifyInputField(name:'title', value:title)
                        verifyInputField(name:'author', value:'Dan Brown')
                        clickButton(label:'Save')
                        verifyTitle(text:'Books detail')
                        clickButton(label:'Close')
    }   }   }   }   }

    def testAddBook() {
        ant.testSpec(name:'books: add new book, make sure it\'s there'){
            config(configMap)
            steps(){
                invoke(url:'books')
                verifyTitle(text:'Books list')
                clickButton(label:'Add book ...')

                verifyTitle(text:'Books detail')
                setInputField(name:'title', value:'Groovy in Action')
                setInputField(name:'author', value:'Dierk Koenig et al.')
                clickButton(label:'Save')

                verifyTitle(text:'Books list')
                selectForm(index: '5')
                clickButton(label:'Details')

                verifyXPath(
                    description:"hidden index id must be 5",
                    xpath:"//input[@type='hidden'][@name='id'][@value='5']")
                verifyInputField(name:'title', value:'Groovy in Action')
                verifyInputField(name:'author', value:'Dierk Koenig et al.')
    }   }   }

}
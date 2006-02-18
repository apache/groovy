// to be called by WebTest.groovy via
// grails run-webtest

class @webtest.name@Test extends grails.util.WebTest {

    // Unlike unit tests, functional tests are often sequence dependent.
    // Specify that sequence here.
    void suite() {
        testInitial@webtest.name@s()
        testAdd@webtest.name@s()
        // add tests for more operations here
    }

    /** Example of an intial test. It can rely on bootstrapped data. */
    def testInitial@webtest.name@s(){
        webtest('@webtest.name@: verify initial page'){
            invoke(url:'@webtest.name@')
            verifyTitle(text:'@webtest.name@ list')
            selectForm(index: 0)
            clickButton(label:'Details')
            verifyTitle(text: '@webtest.name@ detail')
            verifyXPath(
                description:  "hidden index id must be 0",
                xpath:"//input[@type='hidden'][@name='id'][@value='0']")
            clickButton(label:'Save')
            verifyTitle(text: '@webtest.name@ detail')
            clickButton(label:'Close')
    }   }

    /** Example of a test for an operation on the domain object XXX.*/
    def testAdd@webtest.name@s() {
        webtest('@webtest.name@: add new domain object, make sure it\'s there'){
            invoke(url:'@webtest.name@')
            verifyTitle(text:'@webtest.name@ list')
                addXXX('argument1', 'argument2')                // tests can be factored into methods
            verifyTitle(text:'@webtest.name@ list')
            verifyText(text:'argument1.*argument2', regex:true)
            clickButton(label:'End')
    }   }

    def addXXX(param1, param2){
        ant.group(description:"adding a new XXX with param1 = '$param1' and param2 '$param2'"){
            clickButton(label:'Add XXX ...')
            verifyTitle(text:'XXX detail')
            setInputField(name:'param1', value: param1)
            setInputField(name:'param2', value: param2)
            clickButton(label:'Save')
        }
    }
}
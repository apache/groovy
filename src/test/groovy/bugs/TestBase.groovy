package groovy.bugs

/**
 * A base class for testing constructors
 * 
 * @version $Revision$
 */

 class TestBase {

     String foo
     
     TestBase() {
     }
     
     TestBase(String aFoo) {
         this.foo = aFoo
     }
     /** @todo fix bug
     */
     
     def doSomething() {
     	"TestBase"
     }
 }
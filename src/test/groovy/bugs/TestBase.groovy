/**
 * A base class for testing constructors
 * 
 * @version $Revision$
 */

 class TestBase {

     @Property String foo
     
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
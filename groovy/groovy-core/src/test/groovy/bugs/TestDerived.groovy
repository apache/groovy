/**
 * A base class for testing constructors
 * 
 * @version $Revision$
 */

 class TestDerived extends TestBase {

     TestDerived(String aFoo) {
         super(aFoo)
     }
     /** @todo fix bug
     */
     
     doSomething() {
     	"TestDerived" + super.doSomething()
     }
 }
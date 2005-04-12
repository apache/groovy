/**
 * A base class for testing constructors
 * 
 * @version $Revision$
 */

 class TestDerived extends TestBase {

     TestDerived(String aFoo) {
         super(aFoo)
     }
     
     doSomething() {
     	"TestDerived" + super.doSomething()
     }
 }
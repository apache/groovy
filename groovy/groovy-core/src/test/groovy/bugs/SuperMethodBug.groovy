/**
 * @version $Revision$
 */
class SuperMethodBug extends GroovyTestCase {
     
    void testBug() {
    	base = new TestBase()
    	value = base.doSomething()
    	assert value == "TestBase"
    	
    	/** @todo fix bug
    	base = new TestDerived()
    	value = base.doSomething()
    	assert value == "TestDerived"
    	*/
    }

}
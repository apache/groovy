/**
 * @version $Revision$
 */
class SuperMethodBug extends GroovyTestCase {
     
    void testBug() {
    	base = new TestBase("yyy")
    	value = base.doSomething()
    	assert value == "TestBase"
    	
    	base = new TestDerived("abc")
    	value = base.doSomething()
    	assert value == "TestDerivedTestBase"
    }

}

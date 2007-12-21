package groovy.bugs

/**
 * @version $Revision$
 */
class SuperMethodBug extends GroovyTestCase {
     
    void testBug() {
    	def base = new TestBase("yyy")
    	def value = base.doSomething()
    	assert value == "TestBase"
    	
    	base = new TestDerived("abc")
    	value = base.doSomething()
    	assert value == "TestDerivedTestBase"
    }

}

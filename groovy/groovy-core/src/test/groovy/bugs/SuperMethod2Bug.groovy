/**
 * @version $Revision$
 */
 
class SuperMethod2Bug extends GroovyTestCase {
     
    void testBug() {
    /*
    	base = new SuperBase()
    	value = base.doSomething()
    	assert value == "TestBase"
    */
    	/** @todo fix bug
    	
    	base = new SuperDerived()
    	value = base.doSomething()
    	assert value == "TestDerived"
    	*/
    }

}

class SuperBase {
    doSomething() {
    	"TestBase"
    }
}

class SuperDerived extends SuperBase {

	calls = 0
	
    doSomething() {
    	/** @todo ++calls causes bug */
    	//calls++
    	calls = calls + 1
    	assert calls < 3
    	
    	"TestDerived" + super.doSomething()
    }
}


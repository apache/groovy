/**
 * @version $Revision$
 */
 
class SuperMethod2Bug extends GroovyTestCase {
     
    void testBug() {
    	base = new SuperBase()
    	value = base.doSomething()
    	assert value == "TestBase"
    	
    	
    	base = new SuperDerived()
    	value = base.doSomething()
    	assert value == "TestDerivedTestBase"
    }

    void testBug2() {
    	base = new SuperBase()
    	value = base.foo(2)
    	assert value == "TestBase2"
    	
    	
    	base = new SuperDerived()
    	value = base.foo(3)
    	assert value == "TestDerived3TestBase3"
    }

    void testBug3() {
    	base = new SuperBase()
    	value = base.foo(2,3)
    	assert value == "foo(x,y)Base2,3"
    	
    	
    	base = new SuperDerived()
    	value = base.foo(3,4)
    	assert value == "foo(x,y)Derived3,4foo(x,y)Base3,4"
    }

    void testBug4() {
    	base = new SuperBase("Cheese")
    	value = base.name
    	assert value == "Cheese"
    	
    	
    	base = new SuperDerived("Cheese")
    	value = base.name
    	assert value == "CheeseDerived"
    }
}

class SuperBase {
    @Property String name

    SuperBase() {
    }
    
    SuperBase(String name) {
        this.name = name
    }
    
    def doSomething() {
    	"TestBase"
    }

    def foo(param) {
    	"TestBase" + param
    }
    
    def foo(x, y) {
    	"foo(x,y)Base" + x + "," + y
    }
}

class SuperDerived extends SuperBase {
    
	def calls = 0
	
	SuperDerived() {
	}
	
	SuperDerived(String name) {
	    super(name + "Derived")
	}
	
    def doSomething() {
    	/** @todo ++calls causes bug */
    	//calls++
    	/*
    	calls = calls + 1
    	assert calls < 3
    	*/
    	
    	"TestDerived" + super.doSomething()
    }
	
    def foo(param) {
    	"TestDerived" + param + super.foo(param)
    }
	
    def foo(x, y) {
    	"foo(x,y)Derived" + x + "," + y + super.foo(x, y)
    }
}


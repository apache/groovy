class DefaultParamTest extends GroovyTestCase {

    void testDefaultParameters() {
    	value = doSomething("X", "Y", "Z")
    	assert value == "X-Y-Z"
    	
    	value = doSomething("X", "Y")
    	assert value == "X-Y-defC"
    	
    	value = doSomething("X")
    	assert value == "X-defB-defC"
    	
    	shouldFail { doSomething() }
    }


	doSomething(a, b = 'defB', c = 'defC') {
		println "Called with a: ${a}, b ${b}, c ${c}"
		
		return a + "-" + b + "-" + c
	}
}
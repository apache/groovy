package groovy

class DefaultParamClosureTest extends GroovyTestCase {

    void testDefaultParameters() {
        // Default parameters working for closures 
	def doSomething = { a, b = 'defB', c = 'defC' ->
			println "Called with a: ${a}, b ${b}, c ${c}"
			return a + "-" + b + "-" + c
		}

    	def value = doSomething("X", "Y", "Z")
    	assert value == "X-Y-Z"

    	value = doSomething("X", "Y")
    	assert value == "X-Y-defC"

    	value = doSomething("X")
    	assert value == "X-defB-defC"

    	shouldFail { doSomething() }
    }

    void testDefaultTypedParameters() {
	// Handle typed parameters
	def doTypedSomething = { String a = 'defA', String b = 'defB', String c = 'defC' ->
			println "Called typed method with a: ${a}, b ${b}, c ${c}"
			return a + "-" + b + "-" + c
		}
	
    	def value = doTypedSomething("X", "Y", "Z")
    	assert value == "X-Y-Z"
    	
    	value = doTypedSomething("X", "Y")
    	assert value == "X-Y-defC"
    	
    	value = doTypedSomething("X")
    	assert value == "X-defB-defC"
    	
    	value = doTypedSomething()
    	assert value == "defA-defB-defC"
    }

}
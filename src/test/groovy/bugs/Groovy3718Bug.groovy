package groovy.bugs

class Groovy3718Bug extends GroovyTestCase {
    void testPatternMatchOfNull() {
    	def doNullMatch1 = { ->
    		null ==~ /[^0-9]+/
	    }
	
	    def doNullMatch2 = { ->
	        null ==~ /[0-9]+/
	    }
    	
    	shouldFail NullPointerException, doNullMatch1
        shouldFail NullPointerException, doNullMatch2
    }
}

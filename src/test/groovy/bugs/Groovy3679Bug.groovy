package groovy.bugs

class Groovy3679Bug extends GroovyTestCase {
    void testMapEntryWinOverPvtAndPkgPrivateClassFields() {
    	// map entry should win over a package-private field
    	def map1 = new HashMap()
    	map1["table"] = "Some table"
    	assert map1["table"] != null
    	
        // map entry should win over a private field
		def map2 = [:]
		map2["header"] = "Some header"
		assert map2["header"] != null
        
    	// following is to verify that setting of private fields with .@"$x" syntax is not
    	// broken by the fix introduced
    	def x = new X3679()
    	x.setSomething("foo",2)
    	assert x.getAFoo() == 2    
    }
}

class X3679 extends HashMap {
    private foo
    def setSomething(String x,y) {
        this.@"$x" = y
    }
    def getAFoo() {
        return foo
    }
}
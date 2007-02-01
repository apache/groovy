package groovy

class GeneratorTest extends GroovyTestCase {

    void testGenerator() {
        def x = this.&sampleGenerator
        //System.out.println("x: " + x)
		
        def result = ''
        for (i in x) {
            result = result + i
        }
	    
        assert result == "ABC"
    }

    void testFindAll() {
        def x = this.&sampleGenerator
 	    
        def value = x.findAll { item -> return item == "C" }
        assert value == ["C"]

        value = x.findAll { item -> return item != "B" }
        assert value == ["A", "C"]
    }
    
	
    void testEach() {
        def x = this.&sampleGenerator
 	    
        def value = x.each { println(it) }
    }
    
	
    void testMissingThisBug() {
        def result = ''
        for (i in this.&sampleGenerator) {
            result = result + i
        }
	    
        assert result == "ABC"
    }
	
    void sampleGenerator(closure) {
        // kinda like yield statements
        closure.call("A")
        closure.call("B")
        closure.call("C")
    }
}

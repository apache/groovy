
class GeneratorTest extends GroovyTestCase {

    void testGenerator() {
        x = this.sampleGenerator
        //System.out.println("x: " + x)
		
        result = ''
        for (i in x) {
            result = result + i
        }
	    
        assert result == "ABC"
    }

    void testFindAll() {
        x = this.sampleGenerator
 	    
        value = x.findAll { item :: return item == "C" }
        assert value == ["C"]
 	    
        value = x.findAll { item :: return item != "B" }
        assert value == ["A", "C"]
    }
    
	
    void testEach() {
        x = this.sampleGenerator
 	    
        value = x.each { println(it) }
    }
    
	
    void testMissingThisBug() {
        result = ''
        for (i in sampleGenerator) {
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

class ArrayTest extends GroovyTestCase {

    void testFixedSize() {
        array = new String[10]
        
        assert array.size() == 10
        
        array[0] = "Hello"
        
        assert array[0] == "Hello"
        
        println "Created array ${array.inspect()} with type ${array.class}"
	}
    
    void testArrayWithInitializer() {
        array = new String[] { "nice", "cheese", "gromit" }
        
        println "Created array ${array.inspect()} with type ${array.class}"
        
        assert array.size() == 3
        assert array[0] == "nice" : array.inspect()
        assert array[1] == "cheese"
        assert array[2] == "gromit"
    }

    /** @todo 
    void testIntArrayCreate() {
        array = new int[5]
        
        assert array.size() == 5
    }
    */
}
import groovy.DummyInterface

class ArrayParamMethodTest extends GroovyTestCase implements DummyInterface {

    void testMethodCall() {
        array = "a b c".split(' ')
        
        assert array.size() == 3
        
        methodWithArrayParam(array)
	}
    
    void methodWithArrayParam(String[] args) {
        println("first item: ${args[0]}")
    }
}
package groovy

class ArrayParamMethodTest extends GroovyTestCase implements DummyInterface {

    void testMethodCall() {
        def array = "a b c".split(' ')
        
        assert array.size() == 3
        
        methodWithArrayParam(array)
    }
    
    void methodWithArrayParam(String[] args) {
        println("first item: ${args[0]}")
        
        // lets turn it into a list
        def list = args.toList()
        assert list instanceof java.util.List
        list[4] = "e"
        
        assert list == ["a", "b", "c", null, "e"]
        
        println("Created list ${list}")
    }
}
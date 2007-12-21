package groovy.bugs

class SubscriptOnStringArrayBug extends TestSupport {

    void testArraySubscript() {
        def array = getMockArguments()
 
        assert array[1] == "b"
        
        array[0] = "d"
        
        assert array[0] == "d"
        
        println("Contents of array are ${array.inspect()}")
    }
    
    void testRobsTestCase() {
        def array = "one two three".split(" ")
        
        assert array[1] == "two"
    }
}
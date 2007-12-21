package groovy

/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureUsingOuterVariablesTest extends GroovyTestCase {
    
    void testUseOfOuterVariable() {
        
        def x = 123
        def y = "hello"
        
        def closure = { i ->
            println("x ${x}")
            println("y ${y}")
            println("i ${i}")
                
            assert x == 123
            assert y == 'hello'
        }
        closure.call(321)
	}

     /*
     TODO: is this a valid test case?
     void testInnerVariablesVisibleInOuterScope() {
                
        closure = { z = 456 } 
        closure.call(321)
        
        assert z == 456
    }
    */
    
    void testModifyingOuterVariable() {
        
        def m = 123
        
        def closure = { m = 456 } 
        closure.call(321)
        
        assert m == 456
    }
    
    void testCounting() {
        def sum = 0

        [1, 2, 3, 4].each { sum = sum + it }

        assert sum == 10
    }
    
    void testExampleUseOfClosureScopes() {
        def a = 123
        def b
        def c = { b = a + it }
        c(5)
        
        println(b)
        assert b == a + 5
    }    

    void testExampleUseOfClosureScopesUsingEach() {
        def a = 123
        def b
        [5].each { b = a + it }

        assert b == a + 5
    }
}

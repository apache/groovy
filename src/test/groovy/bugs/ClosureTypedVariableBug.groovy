package groovy.bugs

/**
 * @version $Revision$
 */
class ClosureTypedVariableBug extends GroovyTestCase {
    
    void testBug2() {
        def count = makeClosure(0)
        assert count == 1
        
        count = makeClosure2(0)
        assert count == 1
    }


    def makeClosure(Number count) {
        def closure = { count = it }
        closure(1)
        return count
    }

    def makeClosure2(Number c) {
        def count = c
        def closure = { count = it }
        closure(1)
        return count
    }

    void testBug() {
        Integer count = 0
        def closure = { count = it }
        closure(1)
        assert count == 1
    }
    
    void testBug3() {
        def closure = getElementClosure("p")
        def answer = closure("b")
        def value = answer("c")
        println "returned : ${value}"
    }
    
    Closure getElementClosure(tag) {
        return { body ->
            if (true) {
                return {"${body}"}
            }
            else {
                body = null
            }
        }
    }
    
    void testDoubleSlotReference() {
        // there was a bug that the local variable index
        // was wrong set for a closure shared variable. 
        // One slot should have be used and one was used sometimes
        // Thus resulting in sometimes assuming a wrong index 
        double d1 = 1.0d
        double d2 = 10.0d
        1.times { d1=d1*d2 }
        assert d1==10d
        
        long l1 = 1l
        long l2 = 10l
        1.times { l1=l1*l2 }
        assert l1==10l
    }
}
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
}
/**
 * @version $Revision$
 */
class ClosureTypedVariableBug extends GroovyTestCase {
    
    void testBug2() {
        count = makeClosure(0)
        assert count == 1
        
        count = makeClosure2(0)
        assert count == 1
    }


    def makeClosure(Number count) {
        closure = { count = it }
        closure(1)
        return count
    }

    def makeClosure2(Number c) {
        count = c
        closure = { count = it }
        closure(1)
        return count
    }

    void testBug() {
        Integer count = 0
        closure = { count = it }
        closure(1)
        assert count == 1
    }
    
    void testBug3() {
        closure = getElementClosure("p")
        answer = closure("b")
        value = answer("c")
        println "returned : ${value}"
    }
    
    Closure getElementClosure(tag) {
        return { body |
            if (true) {
                return {"${body}"}
            }
            else {
                body = null
            }
        }
    }
}
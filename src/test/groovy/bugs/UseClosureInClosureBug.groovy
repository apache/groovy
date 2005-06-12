/**
 * @version $Revision$
 */
class UseClosureInClosureBug extends GroovyTestCase {
    
    void testBug() {
        def closure = { println it }
        
        def anotherClosure = { closure(it) }
        anotherClosure("Hello")
    }
}
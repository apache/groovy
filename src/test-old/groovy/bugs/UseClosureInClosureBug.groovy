/**
 * @version $Revision$
 */
class UseClosureInClosureBug extends GroovyTestCase {
    
    void testBug() {
        closure = { println it }
        
        anotherClosure = { closure(it) }
        anotherClosure("Hello")
    }
}
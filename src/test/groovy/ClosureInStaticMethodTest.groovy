/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureInStaticMethodTest extends GroovyTestCase {

    void testClosureInStaticMethod() {
        closure = closureInStaticMethod()
        assertClosure(closure)    
    }

    void testMethodClosureInStaticMethod() {
        closure = methodClosureInStaticMethod()
        assertClosure(closure)    
    }
    
    static closureInStaticMethod() {
        { println(it) }
    }
    
    static methodClosureInStaticMethod() {
        System.out.println
    }
    
    static assertClosure(Closure block) {
        assert block != null
        block.call("hello!")
    }
}

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
    
    static def closureInStaticMethod() {
        return { println(it) }
    }

    static def methodClosureInStaticMethod() {
        System.out.println
    }
    
    static def assertClosure(Closure block) {
        assert block != null
        block.call("hello!")
    }
}

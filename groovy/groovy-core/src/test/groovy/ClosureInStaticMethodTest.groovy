/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureInStaticMethodTest extends GroovyTestCase {

    void testClosureInStaticMethod() {
        closure = staticMethod()
        assertClosure(closure)    
    }
  
	static staticMethod() {
	    { println(it) }
	}
	
    static assertClosure(Closure block) {
        assert block != null
        block.call("hello!")
    }
}

package groovy;



/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureAsParamTest extends GroovyTestCase {

    void testSimpleBlockCall() {
        assertClosure({owner| System.out.println(owner) });
    }
  
	assertClosure(Closure block) {
	    assert block != null
	    block.call("hello!");
	}
}

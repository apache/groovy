/** 
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
class ClosureCloneTest extends GroovyTestCase {

    void testCloneOfClosure() {
        factor = 2
        closure = { it * factor }

        value = closure(5)
        assert value == 10

        // now lets clone the closure
        c2 = closure.clone()
        assert c2 != null

        value = c2(6)
        assert value == 12
    }  
}

/** 
 * @author Hallvard Tr¾tteberg
 * @version $Revision$
 */
class ClosureCurryTest extends GroovyTestCase {

    void testCurry() {
		clos1 = {s1, s2 | s1 + s2}
		clos2 = clos1("hi")
		value = clos2("there") 
		assert value == "hithere"
    }  
}

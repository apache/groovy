/** 
 * @author Robert Kuzelj
 * @version $Revision$
 */
class StaticClosurePropertyBug extends GroovyTestCase {

    def static out = {System.out.println(it)}
    
    void testCallStaticClosure() {
        callStaticClosure()
    }
    
    def static callStaticClosure() {
        out("TEST")
    }
}

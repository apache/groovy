/** 
 * @author Robert Kuzelj
 * @version $Revision$
 */
class StaticClosurePropertyBug extends GroovyTestCase {

    static out = {System.out.println(it)}
    
    void testCallStaticClosure() {
        callStaticClosure()
    }
    
    static callStaticClosure() {
        out("TEST")
    }
}

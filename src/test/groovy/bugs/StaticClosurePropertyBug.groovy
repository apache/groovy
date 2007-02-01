package groovy.bugs

/** 
 * @author Robert Kuzelj
 * @version $Revision$
 */
class StaticClosurePropertyBug extends GroovyTestCase {

    static def out = {System.out.println(it)}
    
    void testCallStaticClosure() {
        callStaticClosure()
    }
    
    static def callStaticClosure() {
        out("TEST")
    }
}

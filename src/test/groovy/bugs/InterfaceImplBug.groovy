package groovy.bugs

import java.io.Reader
import org.codehaus.groovy.dummy.FooHandler

/**
 * @author Robert Fuller
 * @version $Revision$
 */
class InterfaceImplBug extends GroovyTestCase implements FooHandler {

    void testMethodCall() {
        handle(null)
    }
    
    void handle(Reader reader){
        println("in handle method")
        def called = true
    }
}
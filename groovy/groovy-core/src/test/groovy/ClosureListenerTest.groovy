import javax.swing.JButton
import java.util.Arrays

/**
 * @version $Revision$
 */
class ClosureListenerTest extends GroovyTestCase {
     
    void testBug() {
        value = System.getProperty('java.awt.headless')
        println("Value of java.awt.headless = ${value}")
        
        b = new JButton("foo")
        b.actionPerformed = { println("Found ${it}") }

        size = b.actionListeners.size()
        assert size == 1
        
        l = b.actionListeners[0]
		code = l.hashCode()
        
        println("listener: ${l} with hashCode code ${code}")
        
        assert l.toString() != "null"
        
        assert l.equals(b) == false
        assert l.equals(l)
        
        assert l.hashCode() != 0
        
        b.removeActionListener(l)
        
        println(b.actionListeners)
        
        size = b.actionListeners.size()
        assert size == 0
    }
}
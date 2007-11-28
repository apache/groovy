package groovy

import javax.swing.JButton

/**
 * @version $Revision$
 */
class ClosureListenerTest extends GroovyTestCase {
     
    void testAddingAndRemovingAClosureListener() {
        def value = System.getProperty('java.awt.headless')
        println("Value of java.awt.headless = ${value}")
        
        def b = new JButton("foo")
        b.actionPerformed = { println("Found ${it}") }

        def size = b.actionListeners.size()
        assert size == 1
        
        def l = b.actionListeners[0]
		def code = l.hashCode()
        
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
    
    void testGettingAListenerProperty() {
    	def b = new JButton("foo")
    	def foo = b.actionPerformed
    	assert foo == null
    }
    
    void testNonStandardListener() {
        def myWhat = null
        def myWhere = null

        def strangeBean = new StrangeBean()
        strangeBean.somethingStrangeHappened = { what, where -> myWhat = what; myWhere = where}
        strangeBean.somethingStrangeHappened('?', '!')
    
        assert myWhat == '?'
        assert myWhere == '!'
    }
}
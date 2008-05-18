package groovy.bugs

/**
 * @version $Revision$
 */
 
class OverloadInvokeMethodBug extends GroovyTestCase {
     
    void testBug() {
    	def a = new OverloadA()
    	a.duh()
    	
    	def b = new OverloadB()
    	b.duh()
    }

}

class OverloadA {
    def invokeMethod(String name, Object args) {
        try {
            metaClass.invokeMethod(this, name, args)
        } catch (MissingMethodException e) {
            println "Missing method: ${name}"
        }
    }
}

class OverloadB extends OverloadA {

}

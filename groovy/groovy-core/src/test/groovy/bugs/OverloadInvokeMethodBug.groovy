/**
 * @version $Revision$
 */
 
class OverloadInvokeMethodBug extends GroovyTestCase {
     
    void testBug() {
    	a = new OverloadA()
    	a.duh()
    	
    	b = new OverloadB()
    	b.duh()
    }

}

class OverloadA {
        invokeMethod(String name, Object args) {
                try {
                        metaClass.invokeMethod(this, name, args)
                } catch(MissingMethodException e) {
                        println "Missing method: ${name}"
                }
        } 
}

class OverloadB extends OverloadA {

}


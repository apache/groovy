package groovy.bugs

import groovy.Foo

/**
 * @author Mark Volkmann 
 * @version $Revision: 1.3 $
 */
class PrintlnWithNewBug extends GroovyTestCase {
    
    void testBug() {
        println(new Foo(name:'abc')) 
        println new Foo(name:'def') 
    }
}

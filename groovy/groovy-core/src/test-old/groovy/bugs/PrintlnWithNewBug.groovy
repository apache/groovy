/**
 * @author Mark Volkmann 
 * @version $Revision$
 */
class PrintlnWithNewBug extends GroovyTestCase {
    
    void testBug() {
        println(new Foo(name:'abc')) 
        println new Foo(name:'def') 
    }
}

/**
 * @author Mark Volkmann 
 * @version $Revision: 1.2 $
 */
class PrintlnWithNewBug extends GroovyTestCase {
    
    void testBug() {
        println(new Foo(name:'abc')) 
        println new Foo(name:'def') 
    }
}

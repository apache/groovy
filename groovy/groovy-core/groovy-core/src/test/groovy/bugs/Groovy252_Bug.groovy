package groovy.bugs

/**
 * @version $Revision$
 */
class Groovy252_Bug extends GroovyTestCase {
    
    def count = 0
    
    void testBug() {
        def value = f()
        assert value == null
        
        value = g()
        assert value == null
        
        value = h()
        assert value == null
    }
    
    
    def f() {
         if (count++ == 5)
            return null
         else
            return null
    } 
    
    def g() {
         ++count
         return null
    } 
    
    def h() {
         ++count
         return null
    } 
}

/**
 * @version $Revision$
 */
class Groovy252_Bug extends GroovyTestCase {
    
    count = 0
    
    void testBug() {
        value = f()
        assert value == null
        
        value = g()
        assert value == null
        
        value = h()
        assert value == null
    }
    
    
    f() {
         if (count++ == 5)
            return null
         else
            return null
    } 
    
    g() {
         count++
	     return null
    } 
    
    h() {
         ++count
	     return null
    } 
}

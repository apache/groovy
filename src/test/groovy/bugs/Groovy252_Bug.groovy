/**
 * @version $Revision$
 */
class Groovy252_Bug extends GroovyTestCase {
    
    count = 0
    
    void testBug() {
        value = f()
        assert value == null
    }
    
    
    f() {
        if (count++ == 5)
    	    return null
        else
	        return null
    } 
}

/**
 * @author Morgan Hankins 
 * @version $Revision$
 */
class TryCatch2Bug extends GroovyTestCase {
    
    void testBug() {
        try {
        }
        catch (Throwable t) { 
        } 
    }
    
    void testBug2() {
    	try {
    		x = 123
    	}
    }
}
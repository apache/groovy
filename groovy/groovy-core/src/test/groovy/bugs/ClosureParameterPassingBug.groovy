/**
 * @author John Wilson
 * @version $Revision$
 */
class ClosureParameterPassingBug extends GroovyTestCase {
    
    void testBug() {
		c = { x | 
			c1 = { 
				println x[0] 
			} 
			
			c1() 
		} 

		c([1]) 
    }
}
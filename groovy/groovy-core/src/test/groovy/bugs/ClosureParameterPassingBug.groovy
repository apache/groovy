import org.codehaus.groovy.classgen.TestSupport

/**
 * @author John Wilson
 * @version $Revision$
 */
class ClosureParameterPassingBug extends TestSupport {
    
    void testBugInMethod() {
		c = { x ::
			y = 123
			c1 = { 
				println y
				println x
				println x[0] 
			} 
			
			c1() 
		} 

		c([1]) 
    }
    
    void testBug() {
    	assertScript """
c = { x ::
	y = 123
	c1 = { 
		assert x != null , "Could not find a value for x"
		assert y == 123 , "Could not find a value for y"
		println x[0] 
	} 
	
	c1() 
} 

c([1]) 
"""
	}
   
}
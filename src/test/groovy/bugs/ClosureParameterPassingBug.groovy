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
    
/** @todo this reproduces John's bug...
    
    void testBugInScript() {
    	assertScript <<<EOF
c = { x | 
	c1 = { 
		assert x != null : "Could not find a value for x"
		println x[0] 
	} 
	
	c1() 
} 

c([1]) 
EOF    	
	}
*/
   
}
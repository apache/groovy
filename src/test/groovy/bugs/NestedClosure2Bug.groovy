package groovy.bugs

/**
 * @version $Revision$
 */
class NestedClosure2Bug extends TestSupport {
     
    Object f
     
    void testFieldBug() {
    	def closure = {
    		return {
	    		f = 123
	    		return null
	        }
	    }
        def value = closure()
        value = value()
        assert f == 123
    }
     
    void testBugOutsideOfScript() {
    	def a = 123
    	def b = 456
    	def closure = {
    		println b
    		def c = 999
    		return {
    			f = 2222111
    			
    			println f
    			
    			println c
    			def d = 678
    			return { 
    				println f
    				assert f == 2222111
    				println d
    				return a
    			}
    		}
    	}
    	def c2 = closure()
    	def c3 = c2()
    	def value = c3()

		assert f == 2222111    	
    	assert value == 123
    }
    
    void testBug() {
    	assertScript """
	    	def a = 123
	    	def closure = {
	    		return {
	    			return { 
	    				return a
	    			}
	    		}
	    	}
	    	def c2 = closure()
	    	def c3 = c2()
	    	value = c3()
	    	
	    	assert value == 123
"""
	}
}
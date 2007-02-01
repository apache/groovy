package groovy.bugs

import org.codehaus.groovy.classgen.TestSupport

/**
 * @author John Wilson
 * @version $Revision$
 */
class ClosureParameterPassingBug extends TestSupport {
    
    void testBugInMethod() {
        def c = { x ->
            def y = 123
            def c1 = {
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
def c = { x ->
    def y = 123
    def c1 = { 
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
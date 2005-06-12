import org.codehaus.groovy.classgen.TestSupport

/**
 * @version $Revision$
 */
class VariableScopingBug extends TestSupport {
    
    void testBug() {
    	// undeclared variable x
    	
    	shouldFail {
            for (z in 0..2) {
                def x = makeCollection()
            }

               for (t in 0..3) {
                for (y in x) {
                    println x
                }
               }
           }
    }

    void testVariableReuse() {
        for (z in 0..2) {
            def x = makeCollection()
        }

           for (t in 0..3) {
            def x = 123
            println x
           }
    }

    def makeCollection() {
        return [1, 2, 3]
    }
}
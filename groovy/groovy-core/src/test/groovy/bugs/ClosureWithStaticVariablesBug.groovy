package groovy.bugs

import org.codehaus.groovy.classgen.TestSupport

/**
 * @version $Revision: 1.5 $
 */
class ClosureWithStaticVariablesBug extends TestSupport {
    
    static def y = [:]
    
    void testBug() {
        c = { x ::
            return {
                foo = Cheese.z
                println foo
                assert foo.size() == 0

                println y
                assert y.size() == 0

                return 6
            }
        }
        c2 = c(5)
        answer = c2()
        assert answer == 6
    }
}

class Cheese {
    @Property static def z = [:]

}
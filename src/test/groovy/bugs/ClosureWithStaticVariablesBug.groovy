package groovy.bugs

/**
 * @version $Revision: 1.5 $
 */
class ClosureWithStaticVariablesBug extends TestSupport {
    
    static def y = [:]
    
    void testBug() {
        def c = { x ->
            return {
                def foo = Cheese.z
                println foo
                assert foo.size() == 0

                println y
                assert y.size() == 0

                return 6
            }
        }
        def c2 = c(5)
        def answer = c2()
        assert answer == 6
    }
}

class Cheese {
    public static z = [:]
}
package groovy.bugs

/**
 * @author John Wilson
 * @version $Revision$
 */
class Groovy239_Bug extends GroovyTestCase {
    
    void testBug() {
        def a = makeClosure()
        def b = makeClosure()
        def c = makeClosure()

        a() {
            println("A")
            b() {
                println("B")
                c() {
                    println("C")
                }
            }
        }
    }

    def makeClosure() {
        return { it() }
    }

    void testBug2() {
        def a = { it() }
        def b = { it() }
        def c = { it() }

        a() {
            b() {
                c() {
                }
            }
        }
    }
   
}
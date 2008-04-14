package groovy.operator

/** 
 * Test to negate lists in Classic Groovy.
 * Test to check whether a given function is even/odd on a given domain.
 * 
 * @author Pilho Kim
 * @version $Revision$
 */
class NegateListsTest extends GroovyTestCase {

    void testNegateList() {
        assert -[1, 2, 3] == [-1, -2, -3]

        def x = [1, 2, 3]
        assert -x == [-1, -2, -3]
        assert x == -[-1, -2, -3]
        assert -(-x) == x

        def y = [-1, -2, -3]
        assert -x == y
        assert x == -y
    }

    void testBitwiseNegateList() {
        assert ~[1, 2, 3] == [-2, -3, -4]

        def x = [1, 2, 3]
        assert ~x == [-2, -3, -4]
        assert x == ~[-2, -3, -4]
        assert ~~x == x
        assert ~(~x) == x

        def y = [-2, -3, -4]
        assert ~x == [-2, -3, -4]
        assert x == ~y
    }

    void testEvenFunction() {
        def PI = Math.PI

        /////////////////////////////////////////////////////////////////////
        // A case of partition having 10 subintervals.
        // x = [0.0*PI/2, 0.1*PI/2, 0.2*PI/2, 0.3*PI/2, 0.4*PI/2, 0.5*PI/2, 
        //               0.6*PI/2, 0.7*PI/2, 0.8*PI/2, 0.9*PI/2, 1.0*PI/2]

        /////////////////////////////////////////////////////////////////////
        // Generate a domain of function used om testing.
        def n = 1000    // the number of partitions for the interval 0..2/PI
        def x = []
        for (i in 0..n) {
            x << i*PI/n
        }
        // println x

        def cos = { Math.cos(it) }
        assertTrue(isEvenFn(cos, x))

        def sin = { Math.sin(it) }
        assertTrue(isOddFn(sin, x))

        def tan = { Math.tan(it) }
        assertTrue(isOddFn(tan, x))
    }

    boolean isEvenFn(f, domain) {
        // println domain.collect(f)
        // println( (-domain).collect(f) )
        println( (domain.collect(f)) == ((-domain).collect(f)) )
        return (domain.collect(f)) == ((-domain).collect(f))
    }

    boolean isOddFn(f, domain) {
        // println domain.collect(f)
        // println(  (-domain).collect(f) )
        println( domain.collect(f) == -((-domain).collect(f)) )
        return domain.collect(f) == -((-domain).collect(f))
    }
}
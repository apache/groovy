package groovy

/**
 * Tests iterating with local variables
 */
class ForLoopWithLocalVariablesTest extends GroovyTestCase {

    void testForLoop() {
        def x = null

        for ( i in 0..9 ) {
            x = i
        }

        assert x == 9
	}
}

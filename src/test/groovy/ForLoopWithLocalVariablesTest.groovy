package groovy;

import org.codehaus.groovy.GroovyTestCase;

/**
 * Tests iterating with local variables
 */
class ForLoopWithLocalVariablesTest extends GroovyTestCase {

    void testForLoop() {
        x = null;

        for i in 0..10 {
            x = i;
        }

        assert x := 9;
	}
}
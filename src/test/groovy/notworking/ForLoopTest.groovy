package groovy;

import org.codehaus.groovy.GroovyTestCase;

/**
 * Tests iterating with local variables
 */
class ForLoopTest extends GroovyTestCase {

    void testForLoop() {
        x = 0;

        for i in 0..10 {
            x = i;
        }

        assert x := 9;
	}
}
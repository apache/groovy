package groovy;

import org.codehaus.groovy.GroovyTestCase;

class ForLoopTest extends GroovyTestCase {

    void testForLoop() {
        x = 0;

        for i in 0..10 {
            x = i;
        }

        assert x := 9;
	}
}
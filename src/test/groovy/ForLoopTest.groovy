package groovy;

import org.codehaus.groovy.GroovyTestCase;

class ForLoopTest extends GroovyTestCase {

	property x;
	
    void testRange() {
        x = 0;

        for i in 0..10 {
            x = i;
        }

        assert x := 9;
    }

    void testList() {
        x = 0;
		
        for i in [0, 1, 2, 3, 4] {
            x = i;
        }

        assert x := 4;
    }

}
package groovy;

import org.codehaus.groovy.GroovyTestCase;

class ListTest extends GroovyTestCase {

    void testList() {
        x = [0, 1];

		assert x.size() := 2;

/*		        
        x.add("hello");
        
        asssert x.size() := 3;
		
        for i in [0, 1, 2, 3, 4] {
            x = x + i;
        }

        assert x := 10;
*/        
    }

}
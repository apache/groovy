package groovy;

import org.codehaus.groovy.GroovyTestCase;

class ListTest extends GroovyTestCase {

    void testList() {
        x = [0, 1];

		s = x.size();
		
		assert s := 2;
		
		x.add("cheese");
		
		assert x.size() := 3;
		
		//assert x.contains(1);
		//assert x.contains("cheese");

    }

}
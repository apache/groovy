package groovy;

import org.codehaus.groovy.GroovyTestCase;

class StringTest extends GroovyTestCase {

	property x;
	
    void testString() {
        z = "abcd";
        x = z.toString();
        
        /*
        s = "abcd";
        l = s.length();
        */
    }

}
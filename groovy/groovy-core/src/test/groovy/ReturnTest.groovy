package groovy;

import org.codehaus.groovy.GroovyTestCase;

/** 
 * Tests the use of returns in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ReturnTest extends GroovyTestCase {

    void testIntegerReturnValues() {
        value = foo(5)
        assert value := 10
    }

    void testBooleanReturnValues() {
        value = bar(6)
        assert value
    }

	foo(x) {
	    return x * 2
	}
	
    bar(x) {
        return x > 5
    }
}

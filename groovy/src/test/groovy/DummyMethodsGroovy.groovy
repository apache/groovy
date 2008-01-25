/**
 * methods with specific parameters (e.g.&nbsp;primitives)
 * for use with groovy tests
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
 
package groovy;

public class DummyMethodsGroovy {
    public static void main(String[] args) {
        DummyMethodsGroovy tmp = new DummyMethodsGroovy();
        String answer = tmp.foo("Hey", 1, 2);
        System.out.println("Answer: " + answer);
    }

    public String foo(String a, float b, float c) {
    	return "float args";
    }

    public String foo(String a, int b, int c) {
    	return "int args";
    }
}
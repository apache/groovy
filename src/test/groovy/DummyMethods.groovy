/**
 * methods with specific parameters (e.g. primitives)
 * for use with groovy tests
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
 
package groovy;

public class DummyMethods {
    public static void main(String[] args) {
        DummyMethods tmp = new DummyMethods();
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
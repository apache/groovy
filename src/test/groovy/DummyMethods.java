package groovy;

/**
 * methods with specific parameters (e.g. primitives)
 * for use with groovy tests
 */
public class DummyMethods {
    public String foo(String a, float b, float c) {
    	return "float args";
    }
    public String foo(String a, int b, int c) {
    	return "int args";
    }
}
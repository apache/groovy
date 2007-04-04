package groovy;

/**
 * Arbitrary holder for Java Methods to be called by Groovy TestCases.
 *
 * @author Dierk Koenig
 */

public class SomeClass {
    // currently not supported to be called from Groovy
    public String[][] anArrayOfStringArrays() {
        return new String[][]{{"whatever"}};
    }

    public Object[] anArrayOfStringArraysWorkaround() {
        return new Object[]{new String[]{"whatever", null}};
    }
}
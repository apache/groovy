import junit.framework.Test;
import junit.framework.TestSuite;
import groovy.security.SecurityTest;
import groovy.security.SignedJarTest;

/**
 * All Java security-related Unit tests in the 'groovy' dir
 */

public class JavaSourceGroovyPackagesSecuritySuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SecurityTest.class);
        suite.addTestSuite(SignedJarTest.class);
        return suite;
    }
}

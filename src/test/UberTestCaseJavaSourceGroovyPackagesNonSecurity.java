import junit.framework.TestCase;
import junit.framework.Test;

/**
 * Collecting all security-related Unit Tests under groovy package, written in Java.
 *
 * @author Christian Stein
 * @author Dierk Koenig
 */
public class UberTestCaseJavaSourceGroovyPackagesNonSecurity extends TestCase {
    public static Test suite() {
        return JavaSourceGroovyPackagesNonSecuritySuite.suite();
    }
}

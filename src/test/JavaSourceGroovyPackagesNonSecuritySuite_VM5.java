import groovy.xml.XmlTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.codehaus.groovy.ast.vm5.LineColumnCheckTestSuite;

/**
 * JDK 1.5 non-security related Java Unit tests in the 'groovy' dir
 */

public class JavaSourceGroovyPackagesNonSecuritySuite_VM5 {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(XmlTest.class);
        suite.addTest(LineColumnCheckTestSuite.suite());
        return suite;
    }
}
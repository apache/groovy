import junit.framework.Test;
import junit.framework.TestSuite;
import groovy.inspect.InspectorTest;
import groovy.lang.*;
import groovy.security.SecurityTest;
import groovy.security.SignedJarTest;
import groovy.servlet.GroovyServletTest;
import groovy.text.TemplateTest;
import groovy.text.XmlTemplateEngineTest;
import groovy.tree.NodePrinterTest;
import groovy.util.EvalTest;
import groovy.util.MBeanTest;
import groovy.xml.XmlTest;
import groovy.xml.FactorySupportTest;

/**
 * All Java security-related Unit tests in the 'groovy' dir
 */

public class GroovyJavaSecurityTestsSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SecurityTest.class);
        suite.addTestSuite(SignedJarTest.class);
        return suite;
    }
}

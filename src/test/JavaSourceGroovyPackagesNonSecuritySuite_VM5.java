import groovy.inspect.InspectorTest;
import groovy.lang.*;
import groovy.servlet.GroovyServletTest;
import groovy.text.TemplateTest;
import groovy.text.XmlTemplateEngineTest;
import groovy.tree.NodePrinterTest;
import groovy.util.EvalTest;
import groovy.util.MBeanTest;
import groovy.xml.FactorySupportTest;
import groovy.xml.XmlTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JDK 1.5 non-security related Java Unit tests in the 'groovy' dir
 */

public class JavaSourceGroovyPackagesNonSecuritySuite_VM5 {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(XmlTest.class);
        return suite;
    }
}
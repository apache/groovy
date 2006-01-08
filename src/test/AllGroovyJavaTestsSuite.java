import groovy.inspect.InspectorTest;
import groovy.lang.*;
import groovy.security.SecurityTestDisabled;
import groovy.security.SignedJarTest;
import groovy.servlet.GroovyServletTest;
import groovy.text.TemplateTest;
import groovy.text.XmlTemplateEngineTest;
import groovy.tree.NodePrinterTest;
import groovy.util.EvalTest;
import groovy.util.MBeanTest;
import groovy.xml.XmlTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All Java Unit tests in the 'groovy' dir
 */

public class AllGroovyJavaTestsSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(InspectorTest.class);
        suite.addTestSuite(GroovyClassLoaderTest.class);
        suite.addTestSuite(GroovyShellTest.class);
        suite.addTestSuite(GStringTest.class);
        suite.addTestSuite(IntRangeTest.class);
        suite.addTestSuite(MetaClassTest.class);
        suite.addTestSuite(RangeTest.class);
        suite.addTestSuite(ScriptIntegerDivideTest.class);
        suite.addTestSuite(ScriptPrintTest.class);
        suite.addTestSuite(ScriptTest.class);
        suite.addTestSuite(SequenceTest.class);
        suite.addTestSuite(TupleTest.class);
        // suite.addTestSuite(SecurityTestDisabled.class); // todo: re-enable as soon as it is working on the build server
        // suite.addTestSuite(SignedJarTest.class); // todo: re-enable as soon as it is working on the build server
        suite.addTestSuite(GroovyServletTest.class);
        suite.addTestSuite(TemplateTest.class);
        suite.addTestSuite(XmlTemplateEngineTest.class);
        suite.addTestSuite(NodePrinterTest.class);
        suite.addTestSuite(EvalTest.class);
        suite.addTestSuite(MBeanTest.class);
        suite.addTestSuite(XmlTest.class);

        return suite;
    }
}

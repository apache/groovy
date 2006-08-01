/**
 * Collecting all Groovy unit tests that are written in Groovy, not in root, and not Bug-related.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig
 * @version $Revision$
 */
import junit.framework.Test;
import junit.framework.TestCase;
import groovy.util.AllTestSuite;

public class UberTestCase3 extends TestCase {
    public static Test suite() {
        return AllTestSuite.suite("src/test/groovy", "*/**/*Test.groovy");
    }

// no tests inside (should we have an AbstractGroovyTestCase???)
//
//        suite.addTestSuite(org.codehaus.groovy.classgen.DummyTestDerivation.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.TestSupport.class);

//  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
//
//        suite.addTestSuite(org.codehaus.groovy.classgen.DerivedBean.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.DummyReflector.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.DumpClass.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.DumpClass2.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.DumpClass3.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.DumpClass4.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.DumpingClassLoader.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.Main.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.MyBean.class);
//        suite.addTestSuite(org.codehaus.groovy.classgen.SimpleBean.class);
//        suite.addTestSuite(org.codehaus.groovy.dummy.FooHandler.class);
//        suite.addTestSuite(org.codehaus.groovy.runtime.DummyBean.class);
//        suite.addTestSuite(org.codehaus.groovy.runtime.MockGroovyObject.class);
//        suite.addTestSuite(org.codehaus.groovy.syntax.parser.TestParserSupport.class);
//        suite.addTestSuite(org.codehaus.groovy.tools.DocGeneratorMain.class);

}

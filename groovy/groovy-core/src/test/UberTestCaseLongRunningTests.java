/**
 * The tests collected here all take a 'significant' length of time to execute,
 * i.e. greater than 2 seconds elapsed on my machine.
 *
 * to prevent a JVM startup-shutdown time per test, it should be more efficient to
 * collect the tests together into a suite.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
import junit.framework.*;
public class UberTestCaseLongRunningTests extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ClosureListenerTest.class);
        //todo suite.addTestSuite(ScriptTest.class);
        suite.addTestSuite(groovy.util.AntTest.class);
        //todo suite.addTestSuite(org.codehaus.groovy.bsf.BSFTest.class);
        suite.addTestSuite(org.codehaus.groovy.bsf.CacheBSFTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.ReflectorGeneratorTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.RunBugsTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.RunClosureTest.class);
        //todo suite.addTestSuite(org.codehaus.groovy.runtime.PropertyTest.class);

        // TODO
        //suite.addTestSuite(org.codehaus.groovy.sandbox.markup.StreamingMarkupTest.class);
        //todo suite.addTestSuite(org.codehaus.groovy.syntax.parser.ASTBuilderTest.class);
        //todo suite.addTestSuite(org.codehaus.groovy.syntax.parser.CompilerErrorTest.class);
        //todo suite.addTestSuite(org.codehaus.groovy.wiki.RunHtml2WikiTest.class);
        return suite;
    }

}

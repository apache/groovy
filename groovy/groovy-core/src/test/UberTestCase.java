/**
 * Collects all TestCases in the Groovy test root that are written in Groovy.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig (refactored to use AllTestSuite)
 * @version $Revision$
 */
import junit.framework.*;
import groovy.util.AllTestSuite;

import java.io.File;
import java.io.IOException;

public class UberTestCase extends TestCase {
    public static Test suite() {
        try {
            System.out.println("*** " + new File(".").getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        TestSuite suite = (TestSuite) AllTestSuite.suite("./src/test/groovy","Test.groovy");

        String osName = System.getProperty ( "os.name" ) ;
        if ( osName.equals ( "Linux" ) || osName.equals ( "SunOS" ) ) {
          suite.addTestSuite ( ExecuteTest_LinuxSolaris.class ) ;
        }
        else {
          System.err.println ( "XXXXXX  No execute testsfor this OS.  XXXXXX" ) ;
        }

        return suite;
    }

//  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
//
//        suite.addTestSuite(AnotherMockInputStream.class);
//        suite.addTestSuite(Bean.class);
//        suite.addTestSuite(Bean249.class);
//        suite.addTestSuite(BooleanBean.class);
//        suite.addTestSuite(CallAnotherScript.class);
//        suite.addTestSuite(ClassWithScript.class);
//        suite.addTestSuite(ComparableFoo.class);
//        suite.addTestSuite(CreateData.class);
//        suite.addTestSuite(Entry.class);
//        suite.addTestSuite(EvalInScript.class);
//        suite.addTestSuite(Feed.class);
//        suite.addTestSuite(Foo.class);
//        suite.addTestSuite(HelloWorld.class);
//        suite.addTestSuite(HelloWorld2.class);
//        suite.addTestSuite(Html2Wiki.class);
//        suite.addTestSuite(IntegerCategory.class);
//        suite.addTestSuite(Loop.class);
//        suite.addTestSuite(Loop2.class);
//        suite.addTestSuite(MapFromList.class);
//        suite.addTestSuite(MarkupTestScript.class);
//        suite.addTestSuite(MethodTestScript.class);
//        suite.addTestSuite(MockInputStream.class);
//        suite.addTestSuite(MockProcess.class);
//        suite.addTestSuite(MockSocket.class);
//        suite.addTestSuite(OverloadA.class);
//        suite.addTestSuite(OverloadB.class);
//        suite.addTestSuite(NavToWiki.class);
//        suite.addTestSuite(Person.class);
//        suite.addTestSuite(SampleMain.class);
//        suite.addTestSuite(ScriptWithFunctions.class);
//        suite.addTestSuite(ShowArgs.class);
//        suite.addTestSuite(StringCategory.class);
//        suite.addTestSuite(SuperBase.class);
//        suite.addTestSuite(SuperDerived.class);
//        suite.addTestSuite(TestBase.class);
//        suite.addTestSuite(TestCaseBug.class);
//        suite.addTestSuite(TestDerived.class);
//        suite.addTestSuite(TinyAgent.class);
//        suite.addTestSuite(UnitTestAsScript.class);
//        suite.addTestSuite(UseClosureInScript.class);
//        suite.addTestSuite(X.class);
//        suite.addTestSuite(createLoop.class);
}

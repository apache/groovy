/**
 * Collects all TestCases in the Groovy test root that are written in Groovy.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @author Dierk Koenig (refactored to use AllTestSuite)
 * @version $Revision$
 */

import groovy.util.AllTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UberTestCaseGroovySourceRootPackage extends TestCase {
    public static Test suite() throws ClassNotFoundException {
        TestSuite suite = (TestSuite) AllTestSuite.suite("src/test", "groovy/*Test.groovy");

        String osName = System.getProperty("os.name");
        if (osName.equals("Linux") || osName.equals("SunOS") || osName.equals("Solaris") || osName.equals("Mac OS X")) {
            Class linuxTestClass = Class.forName("groovy.execute.ExecuteTest_LinuxSolaris");
            suite.addTestSuite(linuxTestClass);
        } else if (osName.equals("Windows 2000") || osName.equals("Windows 2003") || osName.equals("Windows XP") || osName.equals("Windows Vista")) {
            Class windowsTestClass = Class.forName("groovy.execute.ExecuteTest_Windows");
            suite.addTestSuite(windowsTestClass);
        } else {
            System.err.println("No execute tests for operating system: " + osName + "!!!");
        }

        return suite;
    }
}

//  The following classes appear in target/test-classes but do not extend junit.framework.TestCase
//
//        AnotherMockInputStream.class
//        Bean.class
//        Bean249.class
//        BooleanBean.class
//        CallAnotherScript.class
//        ClassWithScript.class
//        ComparableFoo.class
//        CreateData.class
//        Entry.class
//        EvalInScript.class
//        Feed.class
//        Foo.class
//        HelloWorld.class
//        HelloWorld2.class
//        Html2Wiki.class
//        IntegerCategory.class
//        Loop.class
//        Loop2.class
//        MapFromList.class
//        MarkupTestScript.class
//        MethodTestScript.class
//        MockInputStream.class
//        MockProcess.class
//        MockSocket.class
//        OverloadA.class
//        OverloadB.class
//        NavToWiki.class
//        Person.class
//        SampleMain.class
//        ScriptWithFunctions.class
//        ShowArgs.class
//        StringCategory.class
//        SuperBase.class
//        SuperDerived.class
//        TestBase.class
//        TestCaseBug.class
//        TestDerived.class
//        TinyAgent.class
//        UnitTestAsScript.class
//        UseClosureInScript.class
//        X.class
//        createLoop.class

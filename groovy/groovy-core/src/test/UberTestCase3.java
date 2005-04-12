/**
 * to prevent a JVM startup-shutdown time per test, it should be more efficient to
 * collect the tests together into a suite.
 *
 * @author <a href="mailto:jeremy.rayner@bigfoot.com">Jeremy Rayner</a>
 * @version $Revision$
 */
import junit.framework.*;
public class UberTestCase3 extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(org.codehaus.groovy.classgen.BytecodeHelperTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.CallClosureFieldAsMethodTest.class);

        suite.addTestSuite(org.codehaus.groovy.classgen.CapitalizeTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.ConstructorIssueTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.ConstructorTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.ForTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.GetPropertyTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.GroovyClassLoaderTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.GStringTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.IfElseTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.MainTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.MetaClassTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.MethodTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.PropertyTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.RunGroovyTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.TupleListTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.VerifierCodeVisitorTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.DefaultGroovyMethodsTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.InheritedInterfaceMethodTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.InvokeConstructorTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.InvokeGroovyMethodTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.InvokeMethodTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.InvokerTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.MethodFailureTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.MethodKeyTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.NewStaticMetaMethodTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.TupleListTest.class);
        suite.addTestSuite(org.codehaus.groovy.sandbox.util.XmlSlurperTest.class);
        suite.addTestSuite(org.codehaus.groovy.syntax.TokenTest.class);
        suite.addTestSuite(org.codehaus.groovy.tools.CompilerTest.class);
        suite.addTestSuite(org.codehaus.groovy.tools.FileSystemCompilerTest.class);
        suite.addTestSuite(org.codehaus.groovy.tools.xml.DomToGroovyTest.class);
        suite.addTestSuite(org.codehaus.groovy.wiki.TestCaseRenderEngineTest.class);
        return suite;
    }

// todo - Are we still using wiki tests as none turn up on my build???
// GL: no, we don't use them anymore, so let's ditch it
//        suite.addTestSuite(org.codehaus.groovy.wiki.RunWikiTest.class);

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

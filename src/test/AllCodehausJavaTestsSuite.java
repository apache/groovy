import org.codehaus.groovy.syntax.TokenTest;
import org.codehaus.groovy.runtime.*;
//import org.codehaus.groovy.wiki.RunWikiTest;
//import org.codehaus.groovy.wiki.TestCaseRenderEngineTest;
import org.codehaus.groovy.tools.xml.DomToGroovyTest;
import org.codehaus.groovy.tools.FileSystemCompilerTest;
import org.codehaus.groovy.tools.CompilerTest;
import org.codehaus.groovy.control.CompilationUnitTest;
import org.codehaus.groovy.control.messages.SyntaxErrorMessageTest;
import org.codehaus.groovy.antlr.*;
import org.codehaus.groovy.antlr.treewalker.*;
import org.codehaus.groovy.bsf.*;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.classgen.*;
import org.codehaus.groovy.classgen.TupleListTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All Java Unit tests in the 'org.codehaus.groovy' dir
 */

public class AllCodehausJavaTestsSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(GroovySourceASTTest.class);
        suite.addTestSuite(SourceBufferTest.class);
        suite.addTestSuite(SourcePrinterTest.class);  
        suite.addTestSuite(ClassNodeTest.class);
        suite.addTestSuite(ModuleNodeTest.class);
        suite.addTestSuite(BSFTest.class);
        suite.addTestSuite(CacheBSFTest.class);
        suite.addTestSuite(BytecodeHelperTest.class);
        suite.addTestSuite(CapitalizeTest.class);
        suite.addTestSuite(ConstructorTest.class);
        suite.addTestSuite(ForTest.class);
        suite.addTestSuite(GetPropertyTest.class);
        suite.addTestSuite(GroovyClassLoaderTest.class);
        suite.addTestSuite(GStringTest.class);
        suite.addTestSuite(IfElseTest.class);
        suite.addTestSuite(MainTest.class);
        suite.addTestSuite(MethodTest.class);
        suite.addTestSuite(ReflectorGeneratorTest.class);
        suite.addTestSuite(RunBugsTest.class);
        suite.addTestSuite(RunClosureTest.class);
        suite.addTestSuite(RunGroovyTest.class);
        suite.addTestSuite(TupleListTest.class);
        suite.addTestSuite(VerifierCodeVisitorTest.class);
        suite.addTestSuite(CompilationUnitTest.class);
//        suite.addTestSuite(TestCaseRenderEngineTest.class);
//        suite.addTestSuite(RunWikiTest.class);
        suite.addTestSuite(DomToGroovyTest.class);
        suite.addTestSuite(FileSystemCompilerTest.class);
        suite.addTestSuite(CompilerTest.class);
        suite.addTestSuite(TokenTest.class);
        suite.addTestSuite(TupleListTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.PropertyTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.PropertyTest.class);
        suite.addTestSuite(NewStaticMetaMethodTest.class);
        suite.addTestSuite(MethodKeyTest.class);
        suite.addTestSuite(MethodFailureTest.class);
        suite.addTestSuite(InvokerTest.class);
        suite.addTestSuite(InvokeMethodTest.class);
        suite.addTestSuite(InvokeGroovyMethodTest.class);
        suite.addTestSuite(InvokeConstructorTest.class);
        suite.addTestSuite(InheritedInterfaceMethodTest.class);
        suite.addTestSuite(DefaultGroovyMethodsTest.class);
        suite.addTestSuite(SyntaxErrorMessageTest.class);
        return suite;
    }
}
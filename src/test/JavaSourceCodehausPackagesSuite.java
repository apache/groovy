/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import junit.framework.Test;
import junit.framework.TestSuite;
//import org.codehaus.groovy.ant.GroovyTest;
//import org.codehaus.groovy.ant.GroovycTest;
import org.codehaus.groovy.antlr.AnnotationSourceParsingTest;
import org.codehaus.groovy.antlr.EnumSourceParsingTest;
import org.codehaus.groovy.antlr.GroovySourceASTTest;
import org.codehaus.groovy.antlr.SourceBufferTest;
import org.codehaus.groovy.antlr.treewalker.*;
import org.codehaus.groovy.ast.ClassNodeTest;
import org.codehaus.groovy.ast.LineColumnCheckTestSuite;
import org.codehaus.groovy.ast.MethodCallExpressionTest;
import org.codehaus.groovy.ast.MethodNodeTest;
import org.codehaus.groovy.ast.ModuleNodeTest;
import org.codehaus.groovy.ast.VariableExpressionTest;
import org.codehaus.groovy.classgen.*;
import org.codehaus.groovy.control.CompilationUnitTest;
import org.codehaus.groovy.control.CompilerConfigurationTest;
import org.codehaus.groovy.control.messages.SyntaxErrorMessageTest;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.syntax.TokenTest;
import org.codehaus.groovy.tools.CompilerTest;
import org.codehaus.groovy.tools.FileSystemCompilerTest;
//import org.codehaus.groovy.tools.groovydoc.GroovyDocToolTest;
import org.codehaus.groovy.tools.gse.DependencyTest;

/**
 * All Java Unit tests in the 'org.codehaus.groovy' dir
 */

public class JavaSourceCodehausPackagesSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(BytecodeHelperTest.class);
        suite.addTestSuite(CapitalizeTest.class);
        suite.addTestSuite(ClassCompletionVerifierTest.class);
        suite.addTestSuite(ClassNodeTest.class);
        suite.addTestSuite(CompilationUnitTest.class);
        suite.addTestSuite(CompilerTest.class);
        suite.addTestSuite(CompositeVisitorTest.class);
        suite.addTestSuite(ConstructorTest.class);
        suite.addTestSuite(DefaultGroovyMethodsTest.class);
        suite.addTestSuite(ResourceGroovyMethodsTest.class);
        suite.addTestSuite(FileSystemCompilerTest.class);
        suite.addTestSuite(ForTest.class);
        suite.addTestSuite(GetPropertyTest.class);
//        suite.addTest(GroovyTest.suite());
//        suite.addTestSuite(GroovycTest.class);
        suite.addTestSuite(GroovyClassLoaderTest.class);
//        suite.addTestSuite(GroovyDocToolTest.class);
        suite.addTestSuite(GroovySourceASTTest.class);
        suite.addTestSuite(EnumSourceParsingTest.class);
        suite.addTestSuite(AnnotationSourceParsingTest.class);
        suite.addTestSuite(GStringTest.class);
        suite.addTestSuite(IfElseTest.class);
        suite.addTestSuite(InvokerTest.class);
        suite.addTestSuite(InvokeMethodTest.class);
        suite.addTestSuite(InvokeGroovyMethodTest.class);
        suite.addTestSuite(InvokeConstructorTest.class);
        suite.addTestSuite(InheritedInterfaceMethodTest.class);
        suite.addTestSuite(MainTest.class);
        suite.addTestSuite(MethodFailureTest.class);
        suite.addTestSuite(MethodKeyTest.class);
        suite.addTestSuite(MethodTest.class);
        suite.addTestSuite(ModuleNodeTest.class);
        suite.addTestSuite(MindMapPrinterTest.class);
        suite.addTestSuite(NewStaticMetaMethodTest.class);
        suite.addTestSuite(NodeAsHTMLPrinterTest.class);
        suite.addTestSuite(NodePrinterTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.PropertyTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.PropertyTest.class);
//        suite.addTestSuite(ReflectorGeneratorTest.class);
        suite.addTestSuite(RunBugsTest.class);
        suite.addTestSuite(RunClosureTest.class);
        suite.addTestSuite(RunGroovyTest.class);
//        suite.addTestSuite(RunWikiTest.class);
        suite.addTestSuite(SourceBufferTest.class);
        suite.addTestSuite(SourcePrinterTest.class);
        suite.addTestSuite(UnimplementedSyntaxTest.class);
        suite.addTestSuite(SyntaxErrorMessageTest.class);
//        suite.addTestSuite(TestCaseRenderEngineTest.class);
        suite.addTestSuite(TokenTest.class);
        suite.addTestSuite(org.codehaus.groovy.classgen.TupleListTest.class);
        suite.addTestSuite(org.codehaus.groovy.runtime.TupleListTest.class);
        suite.addTestSuite(VerifierCodeVisitorTest.class);
        suite.addTestSuite(JdkDynamicProxyTest.class);
        suite.addTestSuite(CompilerConfigurationTest.class);
        suite.addTestSuite(MethodNodeTest.class);
        suite.addTestSuite(VariableExpressionTest.class);
        suite.addTest(LineColumnCheckTestSuite.suite());
        suite.addTestSuite(MethodCallExpressionTest.class);
        suite.addTestSuite(DependencyTest.class);
        suite.addTestSuite(MethodRankHelperTest.class);
        return suite;
    }
}

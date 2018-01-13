/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package groovy.transform.stc

class LambdaTest extends GroovyTestCase {
    void testFunction() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
        
            public static void p() {
                assert [2, 3, 4] == Stream.of(1, 2, 3).map(e -> e.plus 1).collect(Collectors.toList());
            }
        }
        '''
    }

    void testFunctionScript() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        void p() {
            assert [2, 3, 4] == Stream.of(1, 2, 3).map(e -> e + 1).collect(Collectors.toList());
        }
        
        p()
        '''
    }

    /**
     * Depends on https://issues.apache.org/jira/browse/GROOVY-8445
     */
    void testBinaryOperator() {
        if (true) return

        // the test can pass only in dynamic mode now, it can not pass static type checking...

        /* FIXME
TestScript0.groovy: 13: [Static type checking] - Cannot find matching method java.util.stream.Stream#reduce(int, groovy.lang.Closure). Please check if the declared type is correct and if the method exists.
 @ line 13, column 30.
                   assert 13 == Stream.of(1, 2, 3).reduce(7, (r, e) -> r + e);
                                ^

TestScript0.groovy: 13: [Static type checking] - Cannot find matching method java.lang.Object#plus(java.lang.Object). Please check if the declared type is correct and if the method exists.
 @ line 13, column 69.
   (1, 2, 3).reduce(7, (r, e) -> r + e);
                                 ^

2 errors
         */

        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
            
            public static void p() {
                assert 13 == Stream.of(1, 2, 3).reduce(7, (r, e) -> r + e);
            }
        }
        '''
    }

    void testConsumer() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
            
            public static void p() {
                Stream.of(1, 2, 3).forEach(e -> { System.out.println(e + 1); });
            }
            
        }
        '''
    }

    void testPredicate() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
            
            public static void p() {
                def list = ['ab', 'bc', 'de']
                list.removeIf(e -> e.startsWith("a"))
                assert ['bc', 'de'] == list
            }
        }
        '''
    }

    /**
     * Depends on https://issues.apache.org/jira/browse/GROOVY-8445
     */
    void testUnaryOperator() {
        if (true) return

        /* FIXME
TestScript0.groovy: 14: [Static type checking] - Cannot find matching method java.util.List#replaceAll(groovy.lang.Closure). Please check if the declared type is correct and if the method exists.
 @ line 14, column 17.
                   list.replaceAll(e -> e + 10)
                   ^

TestScript0.groovy: 14: [Static type checking] - Cannot find matching method java.lang.Object#plus(int). Please check if the declared type is correct and if the method exists.
 @ line 14, column 38.
                   list.replaceAll(e -> e + 10)
                                        ^

2 errors
         */

        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
            
            public static void p() {
                def list = [1, 2, 3]
                list.replaceAll(e -> e + 10)
                assert [11, 12, 13] == list
            }
        }
        '''
    }

    void testBiConsumer() {
        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
            
            public static void p() {
                def map = [a: 1, b: 2, c: 3]
                map.forEach((k, v) -> System.out.println(k + ":" + v));
            }
        }
        '''
    }

    void testFunctionWithLocalVariables() {
        if (true) return

        // FIXME
        /*
General error during class generation: ASM reporting processing error for Test1#p with signature void p() in TestScript0.groovy:12. TestScript0.groovy

groovy.lang.GroovyRuntimeException: ASM reporting processing error for Test1#p with signature void p() in TestScript0.groovy:12. TestScript0.groovy
	at org.codehaus.groovy.classgen.AsmClassGenerator.visitConstructorOrMethod(AsmClassGenerator.java:447)
	at org.codehaus.groovy.ast.ClassCodeVisitorSupport.visitMethod(ClassCodeVisitorSupport.java:132)
	at org.codehaus.groovy.classgen.AsmClassGenerator.visitMethod(AsmClassGenerator.java:568)
	at org.codehaus.groovy.ast.ClassNode.visitContents(ClassNode.java:1095)
	at org.codehaus.groovy.ast.ClassCodeVisitorSupport.visitClass(ClassCodeVisitorSupport.java:54)
	at org.codehaus.groovy.classgen.AsmClassGenerator.visitClass(AsmClassGenerator.java:261)
	at org.codehaus.groovy.control.CompilationUnit$18.call(CompilationUnit.java:853)
	at org.codehaus.groovy.control.CompilationUnit.applyToPrimaryClassNodes(CompilationUnit.java:1092)
	at org.codehaus.groovy.control.CompilationUnit.doPhaseOperation(CompilationUnit.java:634)
	at org.codehaus.groovy.control.CompilationUnit.processPhaseOperations(CompilationUnit.java:612)
	at org.codehaus.groovy.control.CompilationUnit.compile(CompilationUnit.java:589)
	at groovy.lang.GroovyClassLoader.doParseClass(GroovyClassLoader.java:359)
	at groovy.lang.GroovyClassLoader.access$300(GroovyClassLoader.java:92)
	at groovy.lang.GroovyClassLoader$5.provide(GroovyClassLoader.java:328)
	at groovy.lang.GroovyClassLoader$5.provide(GroovyClassLoader.java:325)
	at org.codehaus.groovy.runtime.memoize.ConcurrentCommonCache.getAndPut(ConcurrentCommonCache.java:138)
	at groovy.lang.GroovyClassLoader.parseClass(GroovyClassLoader.java:323)
	at groovy.lang.GroovyShell.parseClass(GroovyShell.java:548)
	at groovy.lang.GroovyShell.parse(GroovyShell.java:560)
	at groovy.lang.GroovyShell.evaluate(GroovyShell.java:444)
	at groovy.lang.GroovyShell.evaluate(GroovyShell.java:483)
	at groovy.lang.GroovyShell.evaluate(GroovyShell.java:464)
	at groovy.test.GroovyAssert.assertScript(GroovyAssert.java:83)
	at groovy.util.GroovyTestCase.assertScript(GroovyTestCase.java:203)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite$PogoCachedMethodSiteNoUnwrapNoCoerce.invoke(PogoMetaMethodSite.java:210)
	at org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite.callCurrent(PogoMetaMethodSite.java:59)
	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCallCurrent(CallSiteArray.java:51)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:157)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callCurrent(AbstractCallSite.java:169)
	at groovy.transform.stc.LambdaTest.testFunctionWithLocalVariables(LambdaTest.groovy:203)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at junit.framework.TestCase.runTest(TestCase.java:176)
	at junit.framework.TestCase.runBare(TestCase.java:141)
	at junit.framework.TestResult$1.protect(TestResult.java:122)
	at junit.framework.TestResult.runProtected(TestResult.java:142)
	at junit.framework.TestResult.run(TestResult.java:125)
	at junit.framework.TestCase.run(TestCase.java:129)
	at junit.framework.TestSuite.runTest(TestSuite.java:252)
	at junit.framework.TestSuite.run(TestSuite.java:247)
	at org.junit.internal.runners.JUnit38ClassRunner.run(JUnit38ClassRunner.java:86)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
Caused by: java.lang.ArrayIndexOutOfBoundsException: -1
	at org.objectweb.asm.Frame.merge(Frame.java:1501)
	at org.objectweb.asm.Frame.merge(Frame.java:1478)
	at org.objectweb.asm.MethodWriter.visitMaxs(MethodWriter.java:1497)
	at org.codehaus.groovy.classgen.AsmClassGenerator.visitConstructorOrMethod(AsmClassGenerator.java:428)
	... 51 more

         */

        assertScript '''
        import groovy.transform.CompileStatic
        import java.util.stream.Collectors
        import java.util.stream.Stream
        
        @CompileStatic
        public class Test1 {
            public static void main(String[] args) {
                p();
            }
        
            public static void p() {
                String x = "#"
                assert ['#1', '#2', '#3'] == Stream.of(1, 2, 3).map(e -> x + e).collect(Collectors.toList());
            }
        }
        '''
    }
}

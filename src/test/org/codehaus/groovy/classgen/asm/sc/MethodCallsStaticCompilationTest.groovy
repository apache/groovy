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
package org.codehaus.groovy.classgen.asm.sc;

import groovy.transform.stc.MethodCallsSTCTest
import org.codehaus.groovy.control.MultipleCompilationErrorsException

public class MethodCallsStaticCompilationTest extends MethodCallsSTCTest implements StaticCompilationTestSupport {

    void testCallToSuper() {
        assertScript '''
            class Foo {
                int foo() { 1 }
            }
            class Bar extends Foo {
                int foo() { super.foo() }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        '''
    }
    
    void testNullSafeCall() {
        assertScript '''
            String str = null
            assert str?.toString() == null
        '''
    }

    void testCallToPrivateInnerClassMethod() {
        assertScript '''
                class A {
                    static class B { private static void foo() {} }
                   public static void main(args) { B.foo() }
                }
            '''
    }

    void testCallToPrivateOuterClassMethod() {
        assertScript '''
                class A {
                   private static void foo() {}
                   static class B { private static void bar() { A.foo() } }
                }
                new A.B()
            '''
    }

    void testCallToPrivateInnerClassConstant() {
        assertScript '''
                class A {
                   static class B { private static int foo = 333 }
                   public static void main(args) { B.foo }
                }
            '''
    }

    void testCallToPrivateOuterClassConstant() {
        assertScript '''
                class A {
                   private static int foo = 333
                   static class B { private static void bar() { A.foo } }
                }
                new A.B()
            '''
    }

    void testForbiddenCallToPrivateMethod() {
        shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
            class A {
               public static void main(args) { B.foo() }
            }
            class B { private static void foo() {} }
        '''
        }
    }

    void testForbiddenCallToPrivateConstant() {
        shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
            class A {
               public static void main(args) { B.foo }
            }
            class B { private static int foo = 666 }
        '''
        }
    }

    void testExplicitTargetMethodWithCast() {
        assertScript '''
            String foo(String str) { 'STRING' }
            String foo(Object o) { 'OBJECT' }
            assert foo('call') == 'STRING'
            assert foo((Object)'call') == 'OBJECT'
        '''
    }

    void testPlusStaticMethodCall() {
        assertScript '''
            static int foo() { 1 }
            assert 1+foo() == 2
        '''
    }

    // GROOVY-5703
    void testShouldNotConvertStringToStringArray() {
        assertScript '''
        int printMsgs(String ... msgs) {
            int i = 0
            for(String s : msgs) { i++ }

            i
        }
        assert printMsgs('foo') == 1
        assert printMsgs('foo','bar') == 2
        '''
    }

    // GROOVY-5780
    void testShouldNotConvertGStringToStringArray() {
        assertScript '''
        int printMsgs(String ... msgs) {
            int i = 0
            for(String s : msgs) { i++ }

            i
        }
        assert printMsgs("f${'o'}o") == 1
        assert printMsgs("${'foo'}","${'bar'}") == 2
        '''
    }

    void testMethodCallWithDefaultParams() {
            assertScript '''import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode//import org.codehaus.groovy.classgen.asm.sc.MethodCallsStaticCompilationTest.DefaultParamTestSupport as Support
@CompileStatic(TypeCheckingMode.SKIP)
 class Support {
        Support(String name, String val, List arg=null, Set set = null, Date suffix = new Date()) {
            "$name$val$suffix"
        }
    }
                new Support(null, null, null, null)

            '''
    }

    static class DefaultParamTestSupport {
        DefaultParamTestSupport(String name, String val, List arg=null, Set set = null, Date suffix = new Date()) {
            "$name$val$suffix"
        }
    }

    // GROOVY-6647
    void testInaccessibleConstructor() {
        shouldFailWithMessages '''
            class Foo {
                private Foo(){}
            }

            class Bar {
                def foo() {new Foo()}
            }
        ''', '[Static type checking] - Cannot find matching method Foo#<init>()'
    }

    // GROOVY-7063
    void testCallToProtectedMethodFromClosureInSubclassAndDifferentPackage() {
        assertScript ''' import org.codehaus.groovy.classgen.asm.sc.MethodCallsStaticCompilationTest.Base

        class Ext extends Base {

            int doSomething() {
                def c = {
                    foo()
                }
                c.call()
            }
        }
        def ext = new Ext()
        assert ext.doSomething() == 123
        '''
    }

    // GROOVY-7264
    void testCallProtectedMethodWithGenericTypes() {
        assertScript '''
            import org.codehaus.groovy.classgen.asm.sc.MethodCallsStaticCompilationTest.BaseGeneric

            class Ext extends BaseGeneric<Integer> {

                int doSomething() {
                    def c = {
                        foo(123)
                    }
                    c.call()?1:0
                }
            }
            def ext = new Ext()
            assert ext.doSomething() == 1
        '''
    }

    //GROOVY-7863
    void testDoublyNestedPrivateMethodAccess() {
        assertScript '''
            class A {
                private int bar() { 123 }

                class B {

                    int testInner() { new C().barInner() }

                    class C {
                        int barInner() { bar() }
                    }
                }

                int test() {
                    new B().testInner()
                }
            }
            assert new A().test() == 123
        '''
    }

    //GROOVY-7862
    void testProtectedCallFromInnerClassInSeparatePackage() {
        assertScript '''
            import org.codehaus.groovy.classgen.asm.sc.MethodCallsStaticCompilationTest.Base
            class SubBase extends Base {
                class Inner {
                    int test() {
                        foo()
                    }
                }

                int innerTest() {
                    new Inner().test()
                }
            }
            assert new SubBase().innerTest() == 123
        '''
    }

    //GROOVY-8509
    void testProtectedCallFromClassInSamePackage() {
        assertScript '''
            package org.foo

            class A {
                protected A() {}
                protected int m() { 123 }
            }
            class B {
                int test() {
                    new A().m()
                }
            }
            assert new B().test() == 123
        '''
    }

    public static class Base {
        protected int foo() {
            123
        }
    }

    public static class BaseGeneric<T> {
        protected boolean foo(T t) {
            true
        }
    }
}

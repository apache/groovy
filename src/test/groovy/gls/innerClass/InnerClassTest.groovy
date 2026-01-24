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
package gls.innerClass

import groovy.test.NotYetImplemented
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class InnerClassTest {

    @Test
    void testAIC() {
        assertScript '''
            class C {
            }

            def c = new C() {
                def m() { 1 }
            }
            assert c.m() == 1
        '''
    }

    // GROOVY-8254
    @Test
    void testAliasAIC() {
        assertScript '''import Foo as Bar
            class Foo {}

            def regular = new Bar()
            def anonymous = new Bar() {}
            assert regular.class.name == 'Foo'
            assert anonymous.class.superclass.name == 'Foo'
        '''

        assertScript '''import static Baz.Foo as Bar
            class Baz {
                static class Foo {}
            }

            def regular = new Bar()
            def anonymous = new Bar() {}
            assert regular.class.name == 'Baz$Foo'
            assert anonymous.class.superclass.name == 'Baz$Foo'
        '''
    }

    // GROOVY-10840
    @Test
    void testArrayAIC() {
        assertScript '''
            class BAIS extends ByteArrayInputStream {
                BAIS(String input) {
                    super(input.bytes)
                }
            }

            assert new BAIS('input').available() >= 5
        '''
    }

    // GROOVY-7370, GROOVY-10722
    @Test
    void testVargsAIC() {
        String pogo = '''
            class C {
                C(String... args) {
                    strings = args
                }
                public String[] strings
            }
        '''

        assertScript pogo + '''
            def c = new C() { }
            assert c.strings.length == 0
        '''

        assertScript pogo + '''
            def c = new C('x') { }
            assert c.strings.length == 1
        '''

        assertScript pogo + '''
            def c = new C('x','y') { }
            assert c.strings.length == 2
        '''

        assertScript pogo + '''
            def c = new C(null) { }
            assert c.strings == null
        '''

        assertScript pogo + '''
            def a = new String[0]
            def c = new C( a ) { }
            assert c.strings.length == 0
        '''
    }

    @Test
    void testTimerAIC() {
        assertScript '''
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            CountDownLatch called = new CountDownLatch(1)

            Timer timer = new Timer()
            timer.schedule(new TimerTask() {
                @Override
                void run() {
                    called.countDown()
                }
            }, 0)

            assert called.await(10, TimeUnit.SECONDS)
        '''
    }

    @Test
    void testAccessLocalVariableInAIC() {
        assertScript '''
            final String objName = 'My name is Guillaume'

            assert new Object() {
                String toString() { objName }
            }.toString() == objName
        '''
    }

    // GROOVY-5041
    @Test
    void testAccessLocalVariableInAIC2() {
        assertScript '''
            abstract class A {
                abstract call()
            }

            def x = 1
            def a = new A() {
                def call() { x }
            }
            assert a.call() == 1
            x = 2
            assert a.call() == 2
        '''
    }

    // GROOVY-8448
    @Test
    void testAccessLocalVariableVsGetterInAIC() {
        assertScript '''
            def x = 'local' // shared variable written as field in AIC
            def c = new java.util.concurrent.Callable<String>() {
                def getX() { 'getter' }
                @Override String call() {
                    x + ' then ' + this.x
                }
            }
            String result = c()
            assert result == 'local then getter'
        '''
    }

    @Test
    void testAccessLocalVariableFromClosureInAIC() {
        assertScript '''
            def x = [true]
            def o = new Object() {
                def m() {
                    def c = { ->
                        assert x[0]
                    }
                    c()
                }
            }
            o.m()
        '''

        shouldFail '''
            def x = [false]
            def o = new Object() {
                def m() {
                    def c = { ->
                        assert x[0]
                    }
                    c()
                }
            }
            o.m()
        '''
    }

    // GROOVY-9825
    @Test
    void testAccessSuperInterfaceConstantWithInnerClass() {
        assertScript '''
            class Baz {
                static void main(args) {
                    assert new Inner().inner() == 1
                }
                static class Inner implements Bar {
                    def inner() {
                        FOO
                    }
                }
            }

            interface Foo {
                int FOO = 1
            }

            interface Bar extends Foo {
                int BAR = 3
            }
        '''
    }

    // GROOVY-9499
    @Test
    void testAccessStaticMethodFromAICInSuperCtorCall() {
        assertScript '''
            class One {
                One(ref) {
                    HASH_CODE = ref.hashCode()
                }
                public static int HASH_CODE
            }

            class Two extends One {
              Two() {
                super(new Object() { // AIC before special ctor call completes
                  int hashCode() {
                    hash() // should be able to call static method safely
                  }
                })
              }
              static int hash() { 42 }
            }

            def obj = new Two()
            assert One.HASH_CODE == 42
        '''
    }

    @Test
    void testAccessMethodParameterFromGStringInAICMethod() {
        assertScript '''
            Object makeObj0(String name) {
                new Object() {
                    String toString() { "My name is \${name}" }
                }
            }

            assert makeObj0("Guillaume").toString() == "My name is Guillaume"
        '''
    }

    @Test
    void testAccessMethodParameterFromGStringInAICProperty() {
        assertScript '''
            Object makeObj1(String name) {
                 new Object() {
                    String objName = "My name is \${name}"

                    String toString() { objName }
                 }
            }

            assert makeObj1("Guillaume").toString() == "My name is Guillaume"
        '''
    }

    @Test
    void testUsageOfInitializerBlockWithinAnAIC() {
        assertScript '''
            Object makeObj2(String name) {
                 new Object() {
                    String objName
                    // initializer block
                    {
                        objName = "My name is " + name
                    }

                    String toString() {
                        objName
                    }
                 }
            }

            assert makeObj2("Guillaume").toString() == "My name is Guillaume"
        '''
    }

    // GROOVY-8423
    @Test
    void testPrivateInnerClassHasPrivateModifier() {
        assertScript '''
            import static java.lang.reflect.Modifier.*

            class A {
                private class B {}
            }

            int modifiers = A.B.modifiers
            assert isPrivate(modifiers)
        '''
    }

    // GROOVY-8423
    @Test
    void testProtectedInnerClassHasProtectedModifier() {
        assertScript '''
            import static java.lang.reflect.Modifier.*

            class A {
                protected class B {}
            }

            int modifiers = A.B.modifiers
            assert isProtected(modifiers)
        '''
    }

    // GROOVY-8423
    @Test
    void testPackagePrivateInnerClassHasProtectedModifier() {
        assertScript '''
            import static java.lang.reflect.Modifier.*

            class A {
                @groovy.transform.PackageScope class B {}
            }

            int modifiers = A.B.modifiers
            assert !isPrivate(modifiers) && !isProtected(modifiers) && !isPublic(modifiers)
        '''
    }

    // GROOVY-11600
    @Test
    void testInnerEnumOrRecordOrInterfaceHasStaticModifier() {
        assertScript '''
            import static java.lang.reflect.Modifier.*

            class C {
                enum E {}
                class C {}
                trait T {}
                record R() {}
                interface I {}
                @interface A {}
            }

            assert  isStatic(C.E.modifiers)
            assert !isStatic(C.C.modifiers)
            assert  isStatic(C.T.modifiers)
            assert  isStatic(C.R.modifiers)
            assert  isFinal (C.R.modifiers)
            assert  isStatic(C.I.modifiers)
            assert  isStatic(C.A.modifiers)
        '''
    }

    @Test
    void testStaticInnerClass() {
        assertScript '''
            import static java.lang.reflect.Modifier.*

            class A {
                static class B {}
            }
            def b = new A.B()
            assert b != null

            int modifiers = A.B.modifiers
            assert isPublic(modifiers)
        '''
    }

    @Test
    void testStaticInnerClass2() {
        assertScript '''
            class A {
                static class B {}
            }
            assert A.declaredClasses.length == 1
            assert A.declaredClasses[0] == A.B
        '''
    }

    @Test
    void testStaticInnerClass3() {
        assertScript '''
            class A {
                static class B {
                    String p
                }
                B m() {
                    return [p:'x'] // calls ScriptBytecodeAdapter.castToType([p:'x'], A$B.class)
                }
                static final String q = 'y'
            }

            o = new A().m()
            assert o.p == 'x'
            assert o.q == 'y'
        '''
    }

    @Test
    void testNonStaticInnerClass() {
        assertScript '''
            class A {
                class B {
                    final String foo = 'foo'
                }
            }
            def b = new A.B(new A())
            assert b.foo == 'foo'
        '''
    }

    // GROOVY-7944
    @Test
    void testNonStaticInnerClass2() {
        assertScript '''
            class A {
                class B {
                }
                static main(args) {
                    new B(new A())
                    new A().with {
                        new B(it)
                        new B(delegate)
                    }
                }
            }
        '''

        def err = shouldFail '''
            class A {
                class B {
                }
                static main(args) {
                    new A().with {
                        new B(this)
                        new B(owner)
                    }
                }
            }
        '''
        assert err =~ 'Could not find matching constructor for: A\\$B\\(Class\\)'
    }

    @Test @NotYetImplemented
    void testNonStaticInnerClass3() {
        shouldFail CompilationFailedException, '''
            class A {
                class B {}
            }
            def x = new A.B() // requires reference to A
        '''
    }

    // GROOVY-9781
    @Test @NotYetImplemented
    void testNonStaticInnerClass4() {
        assertScript '''
            class A {
                class B {
                    String p
                }
                B m() {
                    return [p:'x'] // calls ScriptBytecodeAdapter.castToType([p:'x'], A$B.class)
                }
                final String q = 'y'
            }

            o = new A().m()
            assert o.p == 'x'
            assert o.q == 'y'
        '''
    }

    // GROOVY-8104
    @Test
    void testNonStaticInnerClass5() {
        assertScript '''
            class A {
                void foo() {
                    C c = new C()
                    ['1','2','3'].each { obj ->
                        c.baz(obj, new I() {
                            @Override
                            void bar(Object o) {
                                B b = new B() // Could not find matching constructor for: A$B(A$_foo_closure1)
                            }
                        })
                    }
                }

                class B {
                }
            }

            class C {
                void baz(Object o, I i) {
                    i.bar(o)
                }
            }

            interface I {
                void bar(Object o)
            }

            A a = new A()
            a.foo()
        '''
    }

    // GROOVY-11485
    @Test
    void testNonStaticInnerClass6() {
        def err = shouldFail '''
            abstract class A {
                A(x) {
                }
            }
            class B {
                class C extends A {
                    // default ctor
                }
                static m() {
                    def b = new B()
                    def c = new C(b)
                }
            }
            B.m()
        '''
        assert err =~ /An explicit constructor is required because the implicit super constructor A\(\) is undefined/
    }

    @Test
    void testLocalVariable() {
        assertScript '''
            class Foo {}
            final val = 2
            def x = new Foo() {
              def bar() { val }
            }
            assert x.bar() == val
            assert x.bar() == 2
        '''
    }

    @Test
    void testConstructor() {
        shouldFail CompilationFailedException, '''
            class Foo {}
            def x = new Foo() {
                Foo() {}
            }
        '''
    }

    @Test
    void testUsageOfOuterField() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private x = 1

                def foo() {
                    def runner = new Run() {
                        def run() { return x }
                    }
                    runner.run()
                }

                void x(y) { x = y }
            }
            def foo = new Foo()
            assert foo.foo() == 1
            foo.x(2)
            assert foo.foo() == 2
        '''
    }

    @Test
    void testUsageOfOuterField2() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private static x = 1

                static foo() {
                    def runner = new Run() {
                        def run() { return x }
                    }
                    runner.run()
                }

                static x(y) { x = y }
            }
            assert Foo.foo() == 1
            Foo.x(2)
            assert Foo.foo() == 2
        '''
    }

    @Test
    void testUsageOfOuterField3() {
        assertScript '''
            interface X {
                def m()
            }

            class A {
                def pm = "pm"

                def bar(x) {x().m()}
                def foo() {
                    bar { ->
                        return new X() {
                            def m() { pm }
                        }
                    }
                }
            }
            def a = new A()
            assert "pm" == a.foo()
        '''
    }

    // GROOVY-6141
    @Test
    void testUsageOfOuterField4() {
        assertScript '''
            class A {
                def x = 1
                def b = new B()
                class B {
                    def y = 2
                    def c = new C()
                    def f () {
                        assert y==2
                        assert x==1
                    }
                    class C {
                        def z = 3
                        def f() {
                            assert z==3
                            assert y==2
                            assert x==1
                        }
                    }
                }
            }

            def a = new A()
            a.b.f()
            a.b.c.f()
        '''
    }

    // GROOVY-9189
    @Test
    void testUsageOfOuterField5() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private static x = 1

                static foo(def runner = new Run() {
                    def run() { return x }
                }) {
                    runner.run()
                }

                static x(y) { x = y }
            }
            assert Foo.foo() == 1
            Foo.x(2)
            assert Foo.foo() == 2
        '''
    }

    // GROOVY-9168
    @Test
    void testUsageOfOuterField6() {
        assertScript '''
            class A {
                //                  AIC in this position can use static properties:
                A(Runnable action = new Runnable() { void run() { answer = 42 }}) {
                    this.action = action
                }
                Runnable   action
                static int answer
            }

            def a = new A()
            a.action.run();
            assert a.answer == 42
        '''
    }

    // GROOVY-9501
    @Test
    void testUsageOfOuterField7() {
        assertScript '''
            class Main extends Outer {
                static main(args) {
                    newInstance().newThread()
                    assert Outer.Inner.error == null
                }
            }

            abstract class Outer {
                private static volatile boolean flag

                void newThread() {
                    Thread thread = new Inner()
                    thread.start()
                    thread.join()
                }

                private final class Inner extends Thread {
                    @Override
                    void run() {
                        try {
                            if (!flag) {
                                // do work
                            }
                        } catch (e) {
                            error = e
                        }
                    }
                    public static error
                }
            }
        '''
    }

    @Test // inner class is static instead of final
    void testUsageOfOuterField8() {
        assertScript '''
            class Main extends Outer {
                static main(args) {
                    newInstance().newThread()
                    assert Outer.Inner.error == null
                }
            }

            abstract class Outer {
                private static volatile boolean flag

                void newThread() {
                    Thread thread = new Inner()
                    thread.start()
                    thread.join()
                }

                private static class Inner extends Thread {
                    @Override
                    void run() {
                        try {
                            if (!flag) {
                                // do work
                            }
                        } catch (e) {
                            error = e
                        }
                    }
                    public static error
                }
            }
        '''
    }

    // GROOVY-9569
    @Test
    void testUsageOfOuterField9() {
        assertScript '''
            class Main extends Outer {
                static main(args) {
                    newInstance().newThread()
                    assert Outer.Inner.error == null
                }
            }

            @groovy.transform.CompileStatic
            abstract class Outer {
                private static volatile boolean flag

                void newThread() {
                    Thread thread = new Inner()
                    thread.start()
                    thread.join()
                }

                private static class Inner extends Thread {
                    @Override
                    void run() {
                        try {
                            if (flag) {
                                assert false : 'boolean conversion'
                            }
                        } catch (e) {
                            error = e
                        }
                    }
                    public static error
                }
            }
        '''
    }

    @Test
    void testUsageOfOuterField10() {
        assertScript '''
            class Outer {
                static final String OUTER_CONSTANT = 'Constant Value'

                class Inner {
                    String access() {
                        return OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuterConst() {
                    def inner = new Inner()
                    assert inner.access() == OUTER_CONSTANT
                }
            }

            def outer = new Outer()
            outer.testInnerClassAccessOuterConst()
        '''
    }

    // GROOVY-5259
    @Test
    void testUsageOfOuterField11() {
        assertScript '''
            class Base {
                Base(String string) {
                }
            }

            class Outer {
                static final String OUTER_CONSTANT = 'Constant Value'

                class Inner extends Base {
                    Inner() {
                        super(OUTER_CONSTANT) // "this" is not initialized yet
                    }

                    String access() {
                        return OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuterConst() {
                    def inner = new Inner()
                    assert inner.access() == OUTER_CONSTANT
                }
            }

            def outer = new Outer()
            outer.testInnerClassAccessOuterConst()
        '''
    }

    @Test
    void testUsageOfOuterField12() {
        def err = shouldFail '''
            class C {
                int count
                static def m() {
                    new LinkedList() {
                        def get(int i) {
                            count += 1
                            super.get(i)
                        }
                    }
                }
            }
            C.m()
        '''

        assert err =~ /Apparent variable 'count' was found in a static scope but doesn't refer to a local variable, static field or class./
    }

    // GROOVY-8050
    @Test
    void testUsageOfOuterField13() {
        assertScript '''
            class Outer {
                class Inner {
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            assert i.p == 1
        '''
    }

    @Test
    void testUsageOfOuterSuperField() {
        assertScript '''
            class InnerBase {
                InnerBase(String string) {
                }
            }

            class OuterBase {
                protected static final String OUTER_CONSTANT = 'Constant Value'
            }

            class Outer extends OuterBase {

                class Inner extends InnerBase {
                    Inner() {
                        super(OUTER_CONSTANT)
                    }

                    String access() {
                        return OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuterConst() {
                    def inner = new Inner()
                    assert inner.access() == OUTER_CONSTANT
                }
            }

            def outer = new Outer()
            outer.testInnerClassAccessOuterConst()
        '''
    }

    @Test
    void testUsageOfOuterSuperField2() {
        assertScript '''
            interface I {
                String CONST = 'value'
            }
            class A implements I {
                static class B {
                    def test() {
                        CONST
                    }
                }
            }
            def x = new A.B().test()
            assert x == 'value'
        '''
    }

    // GROOVY-9905
    @Test
    void testUsageOfOuterSuperField3() {
        assertScript '''
            abstract class A {
                protected final f = 'foo'
                abstract static class B {}
            }

            class C extends A {
                private class D extends A.B { // B is static inner
                    String toString() {
                        f + 'bar' // No such property: f for class: A
                    }
                }
                def m() {
                    new D().toString()
                }
            }

            assert new C().m() == 'foobar'
        '''
    }

    @Test
    void testUsageOfOuterField_WrongCallToSuper() {
        shouldFail '''
            class Outer {
                protected static final String OUTER_CONSTANT = 'Constant Value'

                class Inner {
                    Inner() {
                        // there is no Object#<init>(String) method, but it throws a VerifyError for uninitialized this
                        super(OUTER_CONSTANT)
                    }

                    String access() {
                        return OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuterConst() {
                    def inner = new Inner()
                    inner.access()
                }
            }

            def outer = new Outer()
            outer.testInnerClassAccessOuterConst()
        '''
    }

    @Test
    void testUsageOfOuterFieldOverridden() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private x = 1

                def foo() {
                    def runner = new Run() {
                        def run() { return x } // <-- dynamic variable
                    }
                    runner.run()
                }

                void setX(val) { x = val }
            }
            class Bar extends Foo {
                def x = 'string' // hides 'foo.@x' and overrides 'foo.setX(val)'
            }
            def bar = new Bar()
            assert bar.foo() == 'string'
            bar.x = 'new string'
            assert bar.foo() == 'new string'
        '''
    }

    @Test
    void testUsageOfOuterMethod() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private x() { return 1 }

                def foo() {
                    def runner = new Run(){
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        '''
    }

    @Test
    void testUsageOfOuterMethod2() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private static x() { return 1 }

                def foo() {
                    def runner = new Run() {
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        '''
    }

    @Test
    void testUsageOfOuterMethod3() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private static x() { return 1 }

                def foo(def runner = new Run() {
                    def run() { return x() }
                }) {
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        '''
    }

    // GROOVY-9189
    @Test
    void testUsageOfOuterMethod4() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private static x() { return 1 }

                static def foo(def runner = new Run() {
                    def run() { return x() }
                }) {
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        '''
    }

    // GROOVY-9168
    @Test
    void testUsageOfOuterMethod5() {
        assertScript '''
            class A {
                //                  AIC in this position can use static methods:
                A(Runnable action = new Runnable() { void run() { setAnswer(42) }}) {
                    this.action = action
                }
                Runnable action
                static int answer
            }

            def a = new A()
            a.action.run();
            assert a.answer == 42
        '''
    }

    // GROOVY-10558
    @Test
    void testUsageOfOuterMethod6() {
        assertScript '''
            class Outer {
                static byte[] hash(byte[] bytes) {
                    bytes
                }
                static class Inner {
                    def test(byte[] bytes) {
                        hash(bytes)
                    }
                }
            }

            Object result = new Outer.Inner().test(new byte[1])
            assert result instanceof byte[]
            assert result.length == 1
            assert result[0] == 0
        '''
    }

    // GROOVY-11352
    @Test
    void testUsageOfOuterMethod7() {
        assertScript '''
            class Super {
                protected final String s
                Super(String s) { this.s = s }
            }
            class Outer {
                static String initValue() { 'ok' }
                static class Inner extends Super {
                    Inner() {
                        super(initValue()) // here
                    }
                }
                String test() { new Inner().s }
            }

            assert new Outer().test() == 'ok'
        '''
    }

    // GROOVY-11581
    @Test
    void testUsageOfOuterMethod8() {
        assertScript '''
            class Outer {
                void foo() {
                    def byteArr = "FOO".bytes
                    new Inner().checkClass(byteArr)
                }
                static bar(xxx, yyy) {
                    assert xxx instanceof byte[]
                    assert yyy instanceof String
                }
                static baz(zzz) {
                    assert zzz instanceof byte[]
                }
                static class Inner {
                    def checkClass(val) {
                        assert val instanceof byte[]
                        bar(val, "")
                        baz(val)
                    }
                }
            }

            new Outer().foo()
        '''
    }

    @Test
    void testUsageOfOuterMethodOverridden() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private x() { return 1 }

                def foo() {
                    def runner = new Run() {
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            class Bar extends Foo {
                def x() { return 2 }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        '''
    }

    @Test
    void testUsageOfOuterMethodOverridden2() {
        assertScript '''
            interface Run {
                def run()
            }
            class Foo {
                private static x() { return 1 }

                static foo() {
                    def runner = new Run() {
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            class Bar extends Foo {
                static x() { return 2 }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        '''
    }

    // GROOVY-11611
    @Test
    void testUsageOfOuterMethodOverridden3() {
        assertScript '''
            class C {
                String value() { 'C' }
                static class D extends C {
                    @Override
                    String value() {
                        super.value() + 'D'
                    }
                    final class B {
                        def m() {
                            'B' + D.super.value()
                        }
                    }
                }
            }
            def d = new C.D()
            def b = new C.D.B(d)
            assert b.m() == 'BC'
        '''
    }

    @Test
    void testUsageOfOuterType() {
        assertScript '''
            class Foo {
                class Bar {
                    def test() {
                        new Baz()
                    }
                }
                class Baz {
                }
            }
            def baz = new Foo().new Foo.Bar().test()
            assert baz instanceof Foo.Baz
        '''
    }

    @Test
    void testUsageOfOuterType2() {
        assertScript '''
            class Foo {
                static class Bar {
                    static test() {
                        new Baz()
                    }
                }
                static class Baz {
                }
            }
            def baz = Foo.Bar.test()
            assert baz instanceof Foo.Baz
        '''
    }

    @Test
    void testUsageOfOuterType3() {
        def err = shouldFail '''
            class Foo {
                static class Bar {
                    static test() {
                        new Baz()
                    }
                }
                class Baz {
                }
            }
        '''
        assert err =~ /No enclosing instance passed in constructor call of a non-static inner class/
    }

    // GROOVY-10289
    @Test
    void testUsageOfOuterType4() {
        def err = shouldFail '''
            class Foo {
                static class Bar {
                    def test() {
                        new Baz()
                    }
                }
                class Baz {
                }
            }
        '''
        assert err =~ /No enclosing instance passed in constructor call of a non-static inner class/
    }

    @Test
    void testUsageOfOuterType5() {
        def err = shouldFail '''
            class Foo {
                static class Bar {
                    class Baz {
                        def test() {
                            new Foo.Baz()
                        }
                    }
                }
                class Baz {
                }
            }
        '''
        assert err =~ /No enclosing instance passed in constructor call of a non-static inner class/
    }

    // GROOVY-11711
    @Test
    void testUsageOfOuterType6() {
        assertScript '''
            class Foo<T> {
                static class Bar {
                }
                /*non-static*/ class Baz
                    implements java.util.concurrent.Callable<T> {
                    T call() {
                    }
                }
            }
            def foo = new Foo<Short>()
            def baz = new Foo.Baz(foo)
            assert baz.call() == null
        '''
    }

    @Test
    void testClassOutputOrdering() {
        // this does actually not do much, but before this
        // change the inner class was tried to be executed
        // because a class ordering bug. The main method
        // makes the Foo class executable, but Foo$Bar is
        // not. So if Foo$Bar is returned, asserScript will
        // fail. If Foo is returned, asserScript will not
        // fail.
        assertScript '''
            class Foo {
                static class Bar{}
                static main(args){}
            }
        '''
    }

    @Test
    void testInnerClassDotThisUsage() {
        assertScript '''
            class A{
                int x = 0;
                class B{
                    int y = 2;
                    class C {
                        void foo() {
                          A.this.x  = 1
                          A.B.this.y = 2*B.this.y;
                        }
                    }
                }
            }
            def a = new A()
            def b = new A.B(a)
            def c = new A.B.C(b)
            c.foo()
            assert a.x == 1
            assert b.y == 4
        '''
    }

    @Test
    void testInnerClassDotThisUsage2() {
        assertScript '''
            interface X {
                def m()
            }

            class A {
                def foo() {
                    def c = {
                        return new X(){def m(){
                            A.this
                         } }
                    }
                    return c().m()
                }
            }
            class B extends A {}
            def b = new B()
            assert b.foo() instanceof B
        '''
    }

    // GROOVY-4028
    @Test
    void testImplicitThisPassingWithNamedArguments() {
        assertScript '''
            class Outer {
                def inner() {
                    new Inner(fName: 'Roshan', lName: 'Dawrani')
                }
                class Inner {
                    Map props
                    Inner(Map props) {
                        this.props = props
                    }
                }
            }
            def outer = new Outer()
            def inner = outer.inner()
            assert inner.props.size() == 2
        '''
    }

    @Test
    void testThis0() {
        assertScript '''
            class A {
                static field = 10

                static main(args) {
                    new A().test()
                }

                void test() {
                    assert new C().m() == [10,12,14,16]
                }

                class C {
                    def m() {
                        def x = []
                        4.times { n ->
                            x << new D(n).f(n)
                        }
                        x
                    }
                }

                class D {
                    def p
                    D(p) { this.p = p }
                    def f(i) { i + p + field }
                }
            }
        '''
    }

    @Test
    void testReferencedVariableInAIC() {
        assertScript '''
            interface X {}

            final double delta = 0.1
            (0 ..< 1).collect { n ->
                new X () {
                    Double foo () {
                        delta
                    }
                }
            }
        '''
    }

    @Test
    void testReferencedVariableInAIC2() {
        assertScript '''
            interface X {}

            final double delta1 = 0.1
            final double delta2 = 0.1
            (0 ..< 1).collect { n ->
                new X () {
                    Double foo () {
                        delta1 + delta2
                    }
                }
            }
        '''
    }

    // GROOVY-7686
    @Test
    void testReferencedVariableInAIC3() {
        assertScript '''
            abstract class A {
                abstract void m()
            }
            void test() {
                def v = false
                def a = new A() {
                    @Override void m() {
                        assert v == true
                    }
                }
                v = true
                a.m()
            }
            test()
        '''
    }

    // GROOVY-5754
    @Test
    void testResolveInnerOfSuperType() {
        assertScript '''
            interface I { class C { } }

            class Outer implements I {
                static class Inner extends C {}
            }

            new Outer.Inner()
        '''
    }

    // GROOVY-5989
    @Test
    void testResolveInnerOfSuperType2() {
        assertScript '''
            interface I { class C { } }

            class Outer implements I {
                static class Inner extends C { }
            }

            new Outer()
            new Outer.Inner()
        '''
    }

    // GROOVY-8364
    @Test
    void testResolveInnerOfSuperType3() {
        assertScript '''
            abstract class A { static class C { } }

            class B extends A {
                static m() {
                    C
                }
            }

            assert B.m() == A.C
        '''
    }

    // GROOVY-8364
    @Test
    void testResolveInnerOfSuperType4() {
        assertScript '''
            abstract class A { interface I { } }

            class B extends A {
                static m() {
                    I
                }
            }

            assert B.m() == A.I
        '''
    }

    // GROOVY-8364
    @Test
    void testResolveInnerOfSuperType5() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()
            new File(parentDir, 'q').mkdir()

            def a = new File(parentDir, 'p/A.java')
            a.write '''
                package p;
                public abstract class A {
                    public interface I { }
                }
            '''
            def b = new File(parentDir, 'q/B.groovy')
            b.write '''
                package q
                import p.A
                class B extends A {
                    static m() {
                        I
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            assert loader.loadClass('q.B').m() instanceof Class
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    // GROOVY-8359
    @Test
    void testResolveInnerOfSuperType6() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()
            new File(parentDir, 'q').mkdir()

            def a = new File(parentDir, 'p/A.java')
            a.write '''
                package p;
                public abstract class A {
                    public interface I { }
                }
            '''
            def b = new File(parentDir, 'q/B.groovy')
            b.write '''
                package q
                import p.A
                class B extends A {
                    static m() {
                        I
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a)
            cu.compile()

            loader = new GroovyClassLoader(this.class.classLoader)
            cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(b)
            cu.compile()

            assert loader.loadClass('q.B').m() instanceof Class
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    // GROOVY-8358
    @Test
    void testResolveInnerOfSuperType7() {
        assertScript '''
            class Outer implements I {
                static class Inner extends C {
                    static usage() {
                        new T() // whoami?
                    }
                }
            }

            class C implements H { }

            interface H {
                static class T {}
            }

            interface I {
                static class T {}
            }

            assert Outer.Inner.usage() instanceof H.T
        '''
    }

    // GROOVY-8358
    @Test
    void testResolveInnerOfSuperType8() {
        assertScript '''
            class C implements H { } // moved ahead of Outer

            class Outer implements I {
                static class Inner extends C {
                    static usage() {
                        new T() // whoami?
                    }
                }
            }

            interface H {
                static class T {}
            }

            interface I {
                static class T {}
            }

            assert Outer.Inner.usage() instanceof H.T
        '''
    }

    // GROOVY-9642
    @Test
    void testResolveInnerOfSuperType9() {
        assertScript '''
            class C {
                interface I {}
                static class T {}
            }
            class D extends C {
                static I one() {
                    new I() {}
                }
                static T two() {
                    new T() {}
                }
            }
            assert D.one() instanceof C.I
            assert D.two() instanceof C.T
        '''
    }

    @Test
    void testResolveInnerOfSuperType10() {
        assertScript '''
            abstract class A {
                static class B {}
            }

            def test(A.B[] bees) {
                assert bees != null
            }

            test(new A.B[0])
        '''
    }

    @Test
    void testResolveInnerOfSuperType10a() {
        assertScript '''
            abstract class A {
                static class B {}
            }

            def test(A.B... bees) {
                assert bees != null
            }

            test()
        '''
    }

    // GROOVY-8715
    @Test
    void testResolveInnerOfSuperType10b() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/A.java')
            a.write '''
                package p;
                public abstract class A {
                    public interface I {}
                }
            '''
            def b = new File(parentDir, 'p/B.groovy')
            b.write '''
                package p
                def test(A.I... eyes) {
                    assert eyes != null
                }
                test()
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('p.B').main()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    // GROOVY-7762
    @Test
    void testResolveInnerOfSuperType12() {
        assertScript '''
            class C extends gls.innerClass.Parent8914 {
                C() {
                    def innerOfSuper = new Nested()
                }
            }
            new C()
        '''
    }

    // GROOVY-9866
    @Test
    void testResolveInnerOfSuperType13() {
        assertScript '''
            class X {                   // System
                interface Y {           // Logger
                    enum Z { ONE, TWO } // Level
                }
            }

            interface I extends X.Y { }

            class C implements I {
                def m(Z z) {
                    z.name()
                }
            }

            assert new C().m(X.Y.Z.ONE) == 'ONE'
        '''
    }

    // GROOVY-5679, GROOVY-5681
    @Test
    void testEnclosingMethodIsSet() {
        assertScript '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.expr.*
            import static org.codehaus.groovy.classgen.Verifier.*

            class A {
                @ASTTest(phase=CLASS_GENERATION, value={
                    def initialExpression = node.parameters[0].getNodeMetaData(INITIAL_EXPRESSION)
                    assert initialExpression instanceof ConstructorCallExpression
                    def icn = initialExpression.type
                    assert icn instanceof org.codehaus.groovy.ast.InnerClassNode
                    assert icn.enclosingMethod != null
                    assert icn.enclosingMethod.name == 'bar'
                    assert icn.enclosingMethod.parameters.length == 0 // ensure the enclosing method is bar(), not bar(Object)
                })
                void bar(action = new Runnable() { void run() { x = 123 }}) {
                    action.run()
                }
                int x
            }
            def a = new A()
            a.bar()
            assert a.x == 123
        '''
    }

    // GROOVY-5681, GROOVY-9151
    @Test
    void testEnclosingMethodIsSet2() {
        assertScript '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.expr.*
            import static org.codehaus.groovy.classgen.Verifier.*

            @ASTTest(phase=CLASS_GENERATION, value={
                def init = node.parameters[0].getNodeMetaData(INITIAL_EXPRESSION)
                assert init instanceof MapExpression
                assert init.mapEntryExpressions[0].valueExpression instanceof ConstructorCallExpression
                def type = init.mapEntryExpressions[0].valueExpression.type

                assert type.enclosingMethod != null
                assert type.enclosingMethod.name == 'bar'
                assert type.enclosingMethod.parameters.length == 0 // ensure the enclosing method is bar(), not bar(Map)
            })
            void bar(Map args = [action: new Runnable() { void run() { result = 123 }}]) {
                args.action.run()
            }

            bar()
        '''
    }

    // GROOVY-5681, GROOVY-9151
    @Test
    void testEnclosingMethodIsSet3() {
        assertScript '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.ast.stmt.*
            import static org.codehaus.groovy.classgen.Verifier.*

            @ASTTest(phase=CLASS_GENERATION, value={
                def init = node.parameters[0].getNodeMetaData(INITIAL_EXPRESSION)
                assert init instanceof ConstructorCallExpression
                assert init.type.enclosingMethod != null
                assert init.type.enclosingMethod.name == 'bar'
                assert init.type.enclosingMethod.parameters.length == 0 // ensure the enclosing method is bar(), not bar(Runnable)

                assert init.type.getMethods('run')[0].code instanceof BlockStatement
                assert init.type.getMethods('run')[0].code.statements[0] instanceof ExpressionStatement
                assert init.type.getMethods('run')[0].code.statements[0].expression instanceof DeclarationExpression

                init = init.type.getMethods('run')[0].code.statements[0].expression.rightExpression
                assert init instanceof ConstructorCallExpression
                assert init.isUsingAnonymousInnerClass()
                assert init.type.enclosingMethod != null
                assert init.type.enclosingMethod.name == 'run'
                assert init.type.enclosingMethod.parameters.length == 0
            })
            void bar(Runnable runner = new Runnable() {
                @Override void run() {
                    def comparator = new Comparator<int>() {
                        int compare(int one, int two) {
                        }
                    }
                }
            }) {
                args.action.run()
            }
        '''
    }

    // GROOVY-6810
    @Test
    void testThisReferenceForAICInOpenBlock() {
        assertScript '''
            import java.security.AccessController
            import java.security.PrivilegedAction

            static void injectVariables(final def instance, def variables) {
                instance.class.declaredFields.each { field ->
                    if (variables[field.name]) {
                        AccessController.doPrivileged(new PrivilegedAction() {
                            @Override
                            Object run() {
                                boolean wasAccessible = field.isAccessible()
                                try {
                                    field.accessible = true
                                    field.set(instance, variables[field.name])
                                    return null; // return nothing...
                                } catch (IllegalArgumentException | IllegalAccessException ex) {
                                    throw new IllegalStateException("Cannot set field: " + field, ex)
                                } finally {
                                    field.accessible = wasAccessible
                                }
                            }
                        })
                    }
                }
            }

            class Test {def p}
            def t = new Test()
            injectVariables(t, ['p': 'q'])
        '''
    }

    // GROOVY-4896
    @Test
    void testThisReferenceForAICInOpenBlock2() {
        assertScript '''
            def doSomethingUsingLocal(){
                logExceptions {
                    String s1 = "Ok"
                    Runnable ifA = new Runnable(){
                        void run(){
                            s1.toString()
                        }
                    }
                    ifA.run()
                }
            }

            def doSomethingUsingParamWorkaround(final String s2){
                logExceptions {
                    String s1=s2
                    Runnable ifA = new Runnable(){
                        void run(){
                            s1.toString()
                        }
                    }
                    ifA.run()
                }
            }

            def doSomethingUsingParam(final String s1){ // This always fails
                logExceptions {
                    Runnable ifA = new Runnable(){
                        void run(){
                            s1.toString()
                        }
                    }
                    ifA.run()
                }
            }

            def doSomethingEmptyRunnable(final String s1){
                logExceptions {
                    Runnable ifA = new Runnable(){
                        void run(){
                        }
                    }
                    ifA.run()
                }
            }


            def logExceptions(Closure c){
                try{
                    c.call()
                } catch (Throwable e){
                    return false
                }
                return true
            }

            assert doSomethingUsingLocal()
            assert doSomethingEmptyRunnable("")
            assert doSomethingUsingParamWorkaround("Workaround")
            assert doSomethingUsingParam("anyString")
        '''
    }

    // GROOVY-5582
    @Test
    void testAICExtendingAbstractInnerClass() {
        assertScript '''
            class Outer {
                int outer() { 1 }
                abstract class Inner {
                    abstract int inner()
                }
                int test() {
                    Inner inner = new Inner() {
                        int inner() { outer() }
                    }
                    inner.inner()
                }
            }
            assert new Outer().test() == 1
        '''
    }

    // GROOVY-10141
    @Test
    void testInnerClassIn2xAIC() {
        assertScript '''
            class Outer {
                class Inner {
                }
                def obj = new Object() {
                    String toString() {
                        new Object() {
                            String toString() {
                                new Inner()
                            }
                        }
                    }
                }
            }
            new Outer().obj.toString()
        '''
    }

    // GROOVY-8274
    @Test
    void testMissingMethodHandling() {
        assertScript '''
            class Outer {
                class Inner {
                    def methodMissing(String name, args) {
                        return name
                    }
                }

                def test(Closure c) {
                    c.resolveStrategy = Closure.DELEGATE_ONLY
                    c.delegate = new Inner()
                    c.call()
                }
            }

            def x = new Outer().test { ->
                hello() // missing
            }
            assert x == 'hello'
        '''
    }

    // GROOVY-6831
    @Test
    void testNestedPropertyHandling() {
        assertScript '''
            class Outer {
                private static List items = []
                void add() { items.add('Outer') }
                static class Inner {
                    void add() { items.add('Inner') }
                    static class InnerInner {
                        void add() { items.add('InnerInner') }
                        void set() { items = ['Overwritten'] }
                    }
                }
            }
            new Outer().add()
            new Outer.Inner().add()
            new Outer.Inner.InnerInner().add()
            assert Outer.items == ['Outer', 'Inner', 'InnerInner']
            new Outer.Inner.InnerInner().set()
            assert Outer.items == ['Overwritten']
        '''
    }

    // GROOVY-10935
    @Test
    void testNestedPropertyHandling2() {
        def err = shouldFail '''
            class Outer {
                static class Inner {}
            }
            new Outer.Inner().missing
        '''
        assert err =~ /No such property: missing for class: Outer.Inner/
    }

    // GROOVY-11612
    @Test
    void testNestedPropertyHandling3() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Outer {
                private final String description
                Outer(Inner inner) {
                    this.description = inner.description
                }
                static class Inner {
                    public final String description = 'test'
                }
            }
            assert new Outer(new Outer.Inner()).description == 'test'
        '''
    }

    // GROOVY-11822
    @ParameterizedTest
    @ValueSource(strings=['void','Void','def','Object'])
    void testNestedPropertyHandling4(String returnType) {
        def err = shouldFail """
            class Upper {
                $returnType propertyMissing(String name, Object value) {
                    throw new MissingPropertyException(name, getClass())
                }
            }
            class Outer {
                static class Inner extends Upper {
                }
            }
            new Outer.Inner().missing = 42
        """
        assert err =~ /No such property: missing for class: Outer.Inner/
    }

    // GROOVY-11823
    @Test
    void testNestedPropertyHandling5() {
        assertScript '''
            class Upper {
                Object propertyMissing(String name) {
                    if (name == 'fizz') return 'buzz'
                    throw new MissingPropertyException(name, getClass())
                }
            }
            class Outer {
                static class Inner extends Upper {
                }
            }
            def inner = new Outer.Inner()
            assert inner.fizz == 'buzz'
        '''
    }

    // GROOVY-7312
    @Test
    void testInnerClassOfInterfaceIsStatic() {
        assertScript '''
            import java.lang.reflect.Modifier
            interface Baz {
                class Pls {}
            }

            assert Modifier.isStatic(Baz.Pls.modifiers)
        '''
    }

    // GROOVY-7312
    @Test
    void testInnerClassOfInterfaceIsStatic2() {
        assertScript '''
            import groovy.transform.ASTTest
            import org.objectweb.asm.Opcodes

            @ASTTest(phase = CLASS_GENERATION, value = {
                assert node.innerClasses.every { it.modifiers & Opcodes.ACC_STATIC }
            })
            interface Baz {
                def foo = { "bar" }
            }
            null
        '''
    }

    // GROOVY-8914
    @Test
    void testNestedClassInheritingFromNestedClass() {
        // control
        assert new Outer8914.Nested()

        assertScript '''
            class OuterReferencingPrecompiled {
                static class Nested extends gls.innerClass.Parent8914.Nested {}
            }
            assert new OuterReferencingPrecompiled.Nested()
        '''
    }

    // GROOVY-6809
    @Test
    void testReferenceToUninitializedThis() {
        def err = shouldFail '''
            class Test {
                static main(args) {
                    def a = new A()
                }

                static class A {
                    A() {
                        def b = new B()
                    }

                    class B extends A {
                        B() {
                            super(A.this)
                        }
                    }
                }
            }
        '''

        assert err =~ / Could not find matching constructor for: Test.A\(Test.A\)/
    }

    // GROOVY-6809
    @Test
    void testReferenceToUninitializedThis2() {
        assertScript '''
            class A {
                A() {
                    this(new Runnable() {
                        @Override
                        void run() {
                        }
                    })
                }

                private A(Runnable action) {
                }
            }

            new A()
        '''
    }

    // GROOVY-6809
    @Test
    void testReferenceToUninitializedThis3() {
        assertScript '''
            class A {
                A(x) {
                }
            }
            class B extends A {
              B() {
                super(new Object() {})
              }
            }

            new B()
        '''
    }

    // GROOVY-9168
    @Test
    void testReferenceToUninitializedThis4() {
        def err = shouldFail '''
            class Outer {
              class Inner {
              }
              Outer(Inner inner) {
              }
              Outer() {
                  this(new Inner())
              }
            }
            new Outer()
        '''

        assert err =~ / Cannot reference 'this' before supertype constructor has been called. /
    }

    // GROOVY-9168
    @Test
    void testReferenceToUninitializedThis5() {
        def err = shouldFail '''
            class Outer {
              class Inner {
              }
              Outer(Inner inner = new Inner()) {
              }
            }
            new Outer()
        '''

        assert err =~ / Cannot reference 'this' before supertype constructor has been called. /
    }

    // GROOVY-9168
    @Test
    void testReferenceToUninitializedThis6() {
        assertScript '''
            import groovy.transform.ASTTest
            import java.util.concurrent.Callable
            import org.codehaus.groovy.ast.expr.*
            import static org.codehaus.groovy.classgen.Verifier.*

            class A {
                @ASTTest(phase=CLASS_GENERATION, value={
                    def init = node.parameters[0].getNodeMetaData(INITIAL_EXPRESSION)
                    assert init instanceof ConstructorCallExpression
                    assert init.isUsingAnonymousInnerClass()
                    assert init.type.enclosingMethod != null
                    assert init.type.enclosingMethod.name == '<init>'
                    assert init.type.enclosingMethod.parameters.length == 0 // ensure the enclosing method is A(), not A(Runnable)
                })
                A(Callable action = new Callable() { def call() { return 42 }}) {
                    this.action = action
                }
                Callable action
            }

            def a = new A()
            assert a.action.call() == 42
        '''
    }
}

//------------------------------------------------------------------------------

class Parent8914 {
    static class Nested {}
}

class Outer8914 {
    static class Nested extends Parent8914.Nested {}
}

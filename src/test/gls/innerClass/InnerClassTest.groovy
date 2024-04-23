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
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
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

    @Test // GROOVY-8254
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

    @Test // GROOVY-10840
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

    @Test // GROOVY-7370, GROOVY-10722
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

    @Test // GROOVY-8448
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

    @Test
    void testAccessFinalLocalVariableFromMethodInAIC() {
        assertScript '''
            final String objName = "My name is Guillaume"

            assert new Object() {
                String toString() { objName }
            }.toString() == objName
        '''
    }

    @Test // GROOVY-9825
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

    @Test // GROOVY-9499
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

    @Test // GROOVY-8423
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

    @Test // GROOVY-8423
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

    @Test // GROOVY-8423
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

    @Test // GROOVY-7944
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

    @Test @NotYetImplemented // GROOVY-9781
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

    @Test // GROOVY-8104
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

    @Test // GROOVY-6141
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

    @Test // GROOVY-9189
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

    @Test // GROOVY-9168
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

    @Test // GROOVY-9501
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

    @Test // GROOVY-9569
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

    @Test // GROOVY-5259
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

    @Test // GROOVY-8050
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

    @Test // GROOVY-9905
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

    @Test // GROOVY-9189
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

    @Test // GROOVY-9168
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

    @Test // GROOVY-10558
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

    @Test // GROOVY-11352
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

    @Test // GROOVY-10289
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

    @Test // GROOVY-4028
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

    @Test // GROOVY-7686
    void testReferencedVariableInAIC3() {
        assertScript '''
            abstract class A {
                A() {
                    m()
                }
                abstract void m();
            }
            void test() {
                def v = false
                def a = new A() {
                    // run by super ctor
                    @Override void m() {
                        assert v != null
                    }
                }
                v = true
                a.m()
            }
            test()
        '''
    }

    @Test // GROOVY-5754
    void testResolveInnerOfSuperType() {
        assertScript '''
            interface I { class C { } }

            class Outer implements I {
                static class Inner extends C {}
            }

            new Outer.Inner()
        '''
    }

    @Test // GROOVY-5989
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

    @Test // GROOVY-8364
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

    @Test // GROOVY-8364
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

    @CompileDynamic @Test // GROOVY-8364
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

    @CompileDynamic @Test // GROOVY-8359
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

    @Test // GROOVY-8358
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

    @Test // GROOVY-8358
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

    @Test // GROOVY-9642
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

    @CompileDynamic @Test // GROOVY-8715
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

    @Test // GROOVY-7762
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

    @Test // GROOVY-9866
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

    @Test // GROOVY-5679, GROOVY-5681
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

    @Test // GROOVY-5681, GROOVY-9151
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

    @Test // GROOVY-5681, GROOVY-9151
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

    @Test // GROOVY-6810
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

    @Test // GROOVY-4896
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

    @Test // GROOVY-5582
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

    @Test // GROOVY-10141
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

    @Test // GROOVY-8274
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

    @Test // GROOVY-6831
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

    @Test // GROOVY-10935
    void testNestedPropertyHandling2() {
        def err = shouldFail '''
            class Outer {
                static class Inner {}
            }
            new Outer.Inner().missing
        '''
        assert err =~ /No such property: missing for class: Outer.Inner/
    }

    @Test // GROOVY-7312
    void testInnerClassOfInterfaceIsStatic() {
        assertScript '''
            import java.lang.reflect.Modifier
            interface Baz {
                class Pls {}
            }

            assert Modifier.isStatic(Baz.Pls.modifiers)
        '''
    }

    @Test // GROOVY-7312
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

    @Test // GROOVY-8914
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

    @Test // GROOVY-6809
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

    @Test // GROOVY-6809
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

    @Test // GROOVY-6809
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

    @Test // GROOVY-9168
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

    @Test // GROOVY-9168
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

    @Test // GROOVY-9168
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

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

import gls.CompilableTestSupport

class InnerClassTest extends CompilableTestSupport {

    void testTimerAIC() {
        assertScript """
            import java.util.concurrent.CountDownLatch
            import java.util.concurrent.TimeUnit

            CountDownLatch called = new CountDownLatch(1)

            Timer timer = new Timer()
            timer.schedule(new TimerTask() {
                void run() {
                    called.countDown()
                }
            }, 0)

            assert called.await(10, TimeUnit.SECONDS)
        """
    }

    void testAICReferenceInClosure() {
        assertScript """
            def y = [true]
            def o = new Object() {
              def foo() {
                def c = {
                  assert y[0]
                }
                c()
              }
            }
            o.foo()
        """
    }

    void testExtendsObjectAndAccessAFinalVariableInScope() {
        assertScript """
            final String objName = "My name is Guillaume"

            assert new Object() {
                String toString() { objName }
            }.toString() == objName
        """
    }

    void testExtendsObjectAndReferenceAMethodParameterWithinAGString() {
        assertScript """
            Object makeObj0(String name) {
                 new Object() {
                    String toString() { "My name is \${name}" }
                 }
            }

            assert makeObj0("Guillaume").toString() == "My name is Guillaume"
        """
    }

    void testExtendsObjectAndReferenceAGStringPropertyDependingOnAMethodParameter() {
        assertScript """
            Object makeObj1(String name) {
                 new Object() {
                    String objName = "My name is \${name}"

                    String toString() { objName }
                 }
            }

            assert makeObj1("Guillaume").toString() == "My name is Guillaume"
        """
    }

    void testUsageOfInitializerBlockWithinAnAIC() {
        assertScript """
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
        """
    }

    void testStaticInnerClass() {
        assertScript """
            import java.lang.reflect.Modifier
        
            class A {
                static class B{}
            }
            def x = new A.B()
            assert x != null
            
            def mods = A.B.modifiers
            assert Modifier.isPublic(mods)
        """

        assertScript """
            class A {
                static class B{}
            }
            assert A.declaredClasses.length==1
            assert A.declaredClasses[0]==A.B
        """
    }

    void testNonStaticInnerClass_FAILS() {
        if (notYetImplemented()) return

        shouldNotCompile """
            class A {
                class B {}
            }
            def x = new A.B()
        """
    }

    void testAnonymousInnerClass() {
        assertScript """
            class Foo {}

            def x = new Foo(){
                def bar() { 1 }
            }
            assert x.bar() == 1
        """
    }

    void testLocalVariable() {
        assertScript """
            class Foo {}
            final val = 2
            def x = new Foo() {
              def bar() { val }
            }
            assert x.bar() == val
            assert x.bar() == 2
        """
    }

    void testConstructor() {
        shouldNotCompile """
            class Foo {}
            def x = new Foo() {
                Foo() {}
            }
        """
    }

    void testUsageOfOuterField() {
        assertScript """
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
        """

        assertScript """
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
        """

        assertScript """
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
        """

        //GROOVY-6141
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

    void testUsageOfOuterFieldOverridden_FAILS() {
        if (notYetImplemented()) return

        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private x = 1
                def foo() {
                    def runner = new Run(){
                        def run() { return x }
                    }
                    runner.run()
                }
                void setX(y) { x=y }
            }
            class Bar extends Foo {
                def x = "string"
            }
            def bar = new Bar()
            assert bar.foo() == 1
            bar.x(2)
            assert bar.foo() == 2
            bar.x = "new string"
            assert bar.foo() == 2
        """

        //TODO: static part

    }

    void testUsageOfOuterMethod() {
        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private x(){1}
                def foo() {
                    def runner = new Run(){
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        """

        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private static x() {1}

                def foo() {
                    def runner = new Run() {
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        """
    }

    // GROOVY-9501
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

    // inner class is static instead of final
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

    void testUsageOfOuterMethodOverridden() {
        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private x(){1}
                def foo() {
                    def runner = new Run(){
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            class Bar extends Foo{
                def x() { 2 }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        """

        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private static x() { 1 }

                static foo() {
                    def runner = new Run() {
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            class Bar extends Foo {
                static x() { 2 }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        """
    }

    void testClassOutputOrdering() {
        // this does actually not do much, but before this
        // change the inner class was tried to be executed
        // because a class ordering bug. The main method 
        // makes the Foo class executeable, but Foo$Bar is 
        // not. So if Foo$Bar is returned, asserScript will
        // fail. If Foo is returned, asserScript will not
        // fail.
        assertScript """
            class Foo {
                static class Bar{}
                static main(args){}
            }
        """
    }

    void testInnerClassDotThisUsage() {
        assertScript """
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
        """

        assertScript """
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
        """
    }

    void testImplicitThisPassingWithNamedArguments() {
        def oc = new MyOuterClass4028()
        assert oc.foo().propMap.size() == 2
    }

    void testThis0() {
        assertScript """
class A {
   static def field = 10
   void main (a) {
     new C ().r ()
   }

   class C {
      def r () {
        4.times {
          new B(it).u (it)
        }
      }
   }

   class B {
     def s
     B (s) { this.s = s}
     def u (i) { println i + s + field }
   }}"""
    }

    void testReferencedVariableInAIC() {
        assertScript """
            interface X{}
            
            final double delta = 0.1
            (0 ..< 1).collect { n ->
                new X () {
                    Double foo () {
                        delta
                    }
                }
            }
        """
        assertScript """
            interface X{}
            
            final double delta1 = 0.1
            final double delta2 = 0.1
            (0 ..< 1).collect { n ->
                new X () {
                    Double foo () {
                        delta1 + delta2
                    }
                }
            }
        """
    }

    // GROOVY-5989
    void testReferenceToOuterClassNestedInterface() {
        assertScript '''
            interface Koo { class Inner { } }

            class Usage implements Koo {
                static class MyInner extends Inner { }
            }

            assert new Usage() != null
        '''
    }

    // GROOVY-5679
    // GROOVY-5681
    void testEnclosingMethodIsSet() {
        new GroovyShell().evaluate '''import groovy.transform.ASTTest
        import static org.codehaus.groovy.control.CompilePhase.*
        import org.codehaus.groovy.ast.InnerClassNode
        import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.classgen.Verifier

        class A {
            int x

            /*@ASTTest(phase=SEMANTIC_ANALYSIS, value={
                def cce = lookup('inner')[0].expression
                def icn = cce.type
                assert icn instanceof InnerClassNode
                assert icn.enclosingMethod == node
            })
            A() { inner: new Runnable() { void run() {} } }

            @ASTTest(phase=SEMANTIC_ANALYSIS, value={
                def cce = lookup('inner')[0].expression
                def icn = cce.type
                assert icn instanceof InnerClassNode
                assert icn.enclosingMethod == node
            })
            void foo() { inner: new Runnable() { void run() {} } }*/

            @ASTTest(phase=CLASS_GENERATION, value={
                def initialExpression = node.parameters[0].getNodeMetaData(Verifier.INITIAL_EXPRESSION)
                assert initialExpression instanceof ConstructorCallExpression
                def icn = initialExpression.type
                assert icn instanceof InnerClassNode
                assert icn.enclosingMethod != null
                assert icn.enclosingMethod.name == 'bar'
                assert icn.enclosingMethod.parameters.length == 0 // ensure the enclosing method is bar(), not bar(Object)
            })
            void bar(action=new Runnable() { void run() { x = 123 }}) {
                action.run()
            }

        }
        def a = new A()
        a.bar()
        assert a.x == 123
        '''
    }

    void testThisReferenceForAICInOpenBlock() {
        // GROOVY-6810
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

        //GROOVY-4896
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

    void testAICextendingAbstractInnerClass() {
        //GROOVY-5582
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

    void testNestedPropertyHandling() {
        // GROOVY-6831
        assertScript '''
            class Outer {
                private static List items = []
                void add() { items.add('Outer') }
                static class Nested {
                    void add() { items.add('Nested') }
                    static class NestedNested {
                        void add() { items.add('NestedNested') }
                        void set() { items = ['Overridden'] }
                    }
                }
            }
            new Outer().add()
            new Outer.Nested().add()
            new Outer.Nested.NestedNested().add()
            assert Outer.items == ["Outer", "Nested", "NestedNested"]
            new Outer.Nested.NestedNested().set()
            assert Outer.items == ["Overridden"]
        '''
    }

    void testInnerClassOfInterfaceIsStatic() {
        //GROOVY-7312
        assertScript '''
            import java.lang.reflect.Modifier
            interface Baz {
                class Pls {}
            }

            assert Modifier.isStatic(Baz.Pls.modifiers)
        '''
    }

    void testInnerClassOfInterfaceIsStaticVariant() {
        //GROOVY-7312
        assertScript '''
            import java.lang.reflect.Modifier
            import groovy.transform.ASTTest
            import org.codehaus.groovy.control.CompilePhase
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

    //GROOVY-8914
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
}

class Parent8914 {
    static class Nested {}
}

class Outer8914 {
    static class Nested extends Parent8914.Nested {}
}

class MyOuterClass4028 {
    def foo() {
        new MyInnerClass4028(fName: 'Roshan', lName: 'Dawrani')
    }
    class MyInnerClass4028 {
        Map propMap
        def MyInnerClass4028(Map propMap) {
            this.propMap = propMap
        }
    }
}

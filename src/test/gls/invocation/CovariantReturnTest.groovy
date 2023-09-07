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
package gls.invocation

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class CovariantReturnTest {

    @Test
    void testCovariantReturn() {
        assertScript '''
            class A {
                Object foo() { 1 }
            }
            class B extends A {
                String foo() { "2" }
            }

            b = new B()
            assert b.foo() == "2"
            assert B.declaredMethods.count{ it.name=="foo" } == 2
        '''
    }

    @Test
    void testCovariantReturnOverwritingAbstractMethod() {
        assertScript '''
            abstract class Numeric {
                abstract Numeric eval()
            }
            class Rational extends Numeric {
                Rational eval() { this }
            }

            assert Rational.declaredMethods.count{ it.name=="eval" } == 2
        '''
    }

    @Test
    void testCovariantReturnOverwritingAnObjectMethod() {
        def err = shouldFail '''
            class C {
                Long toString() { 333L }
                String hashCode() { "hash" }
            }
        '''
        assert err.message.contains('The return type of java.lang.Long toString() in C is incompatible with java.lang.String in java.lang.Object')
    }

    @Test
    void testCovariantOverwritingMethodWithPrimitives() {
        assertScript '''
            class B {
                Object foo(boolean b) { b }
            }
            class C extends B {
                String foo(boolean b) { ""+super.foo(b) }
            }

            c = new C()
            assert c.foo(true) == "true"
            assert c.foo(false) == "false"
        '''
    }

    @Test
    void testCovariantOverwritingMethodWithInterface() {
        assertScript '''
            interface A {
                List foo()
                A baz()
            }
            interface B extends A {
                ArrayList foo()
                B baz()
            }
            class C implements B {
                ArrayList foo() {}
                C baz() {}
            }

            c = new C()
            c.foo()
            c.baz()
        '''
    }

    @Test
    void testCovariantOverwritingMethodWithInterfaceAndInheritance() {
        assertScript '''
            interface A {
                List foo()
                List bar()
                A baz()
            }
            interface B extends A {
                ArrayList foo()
            }
            class MyArrayList extends ArrayList { }
            class C implements B {
                MyArrayList foo() {}
                MyArrayList bar() {}
                C baz() {}
            }

            c = new C()
            c.foo()
            c.bar()
            c.baz()
        '''
    }

    // GROOVY-2582
    @Test
    void testCovariantMethodFromParentOverwritingMethodFromInterfaceInCurrentClass() {
        assertScript '''
            interface A {
                def foo()
            }
            class B {
                String foo() { "" }
            }
            class C extends B implements A {
            }

            c = new C()
            assert c.foo() == ""
        '''

        // basically the same as above, but with an example from an error report
        // Properties has a method "String getProperty(String)", this class
        // is also a GroovyObject, meaning a "Object getProperty(String)" method
        // should be implemented. But this method should not be the usual automatically
        // added getProperty, but a bridge to the getProperty method provided by Properties
        assertScript '''
            class Configuration extends java.util.Properties {
            }

            assert Configuration.declaredMethods.count{ it.name=="getProperty" } == 1
            def conf = new Configuration()
            conf.setProperty("a","b")
            // the following assert would fail if standard getProperty method was added by the compiler
            assert conf.getProperty("a") == "b"
        '''

        assertScript '''
            class A {}
            class B extends A {}
            interface Contract {
                A method()
            }
            abstract class C implements Contract {
            }
            class D extends C {
                B method(String foo = 'default') { new B() }
            }

            assert new D().method() instanceof B
        '''
    }

    // GROOVY-3229
    @Test
    void testImplementedInterfacesNotInfluencing() {
        // some methods from Appendable were not correctly recognized
        // as already being overridden (PrintWriter<Writer<Appendable)
        assertScript '''
            class IndentWriter extends PrintWriter {
                IndentWriter(Writer w) {
                    super(w, true)
                }
            }

            new IndentWriter(new StringWriter())
        '''
    }

    @Test
    void testCovariantReturnFromGenericsInterface() {
        assertScript '''
            class Task implements java.util.concurrent.Callable<List> {
                List call() throws Exception {
                    [ 42 ]
                }
            }

            assert new Task().call() == [42]
        '''
    }

    // GROOVY-7849
    @Test
    void testCovariantArrayReturnType1() {
        assertScript '''
            interface Base {}

            interface Derived extends Base {}

            interface I {
                Base[] foo()
            }

            class C implements I {
                Derived[] foo() { null }
            }
            new C().foo()
        '''
    }

    // GROOVY-7185
    @Test
    void testCovariantArrayReturnType2() {
        assertScript '''
            interface A<T> {
                T[] process();
            }

            class B implements A<String> {
                @Override
                public String[] process() {
                    ['foo']
                }
            }

            class C extends B {
                @Override
                String[] process() {
                    super.process()
                }
            }
            assert new C().process()[0] == 'foo'
        '''
    }

    // GROOVY-7495
    @Test
    void testCovariantReturnFromIndirectInterface() {
        assertScript '''
            interface Item {
            }
            interface DerivedItem extends Item {
            }
            interface Base {
                Item getItem()
            }
            class BaseImpl implements Base {
                Item getItem() { null }
            }
            interface First extends Base {
                DerivedItem getItem()
            }
            class FirstImpl extends BaseImpl implements First {
                DerivedItem getItem() { new DerivedItem(){} }
            }
            interface Second extends First {
            }
            class SecondImpl extends FirstImpl implements Second {
            }

            assert new SecondImpl().item instanceof DerivedItem
        '''

        assertScript '''
            interface A {
                B foo()
            }
            interface B {
            }
            interface A2 extends A {
                B2 foo()
            }
            interface B2 extends B {
            }
            class AA implements A {
                BB foo() { new BB() }
            }
            class AA_A2 extends AA implements A2 {
                BB_B2 foo() { new BB_B2() }
            }
            class BB implements B {
            }
            class BB_B2 extends BB implements B2 {
            }

            assert new AA_A2().foo() instanceof BB_B2
        '''
    }

    // GROOVY-4410
    @Test
    void testCovariantReturnFromMultipleInterfaces() {
        def config = new org.codehaus.groovy.control.CompilerConfiguration(
            jointCompilationOptions: [memStub: true],
            targetDirectory: File.createTempDir()
        )

        def parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'G4410Producer1.java')
            a.write '''
                public interface G4410Producer1 {
                    Object gimme(String[] array);
                }
            '''
            def b = new File(parentDir, 'G4410Producer2.java')
            b.write '''
                public interface G4410Producer2<T> {
                    T gimme(String[] array);
                }
            '''
            def c = new File(parentDir, 'G4410JavaStringProducer.java')
            c.write '''
                public class G4410JavaStringProducer implements G4410Producer1 {
                    public String gimme(String[] array) {
                        return "Hello World";
                    }
                }
            '''
            def d = new File(parentDir, 'G4410GroovyStringProducers.groovy')
            d.write '''
                class HackProducer1 implements G4410Producer1 {
                    Object gimme(String[] array) {
                        "Hello World"
                    }
                }

                class StringProducer1 implements G4410Producer1 {
                    String gimme(String[] array) {
                        "Hello World"
                    }
                }

                class HackProducer2 implements G4410Producer2<Object> {
                    Object gimme(String[] array) {
                        "Hello World"
                    }
                }

                class StringProducer2 implements G4410Producer2<String> {
                    String gimme(String[] array) {
                        "Hello World"
                    }
                }
            '''
            def e = new File(parentDir, 'Main.groovy')
            e.write '''
                def sp1 = new StringProducer1()
                assert sp1.gimme(null) == "Hello World"

                def jsp = new G4410JavaStringProducer()
                assert jsp.gimme(null) == "Hello World"

                def hp1 = new HackProducer1()
                assert hp1.gimme(null) == "Hello World"

                def sp2 = new StringProducer2()
                assert sp2.gimme(null) == "Hello World"

                def hp2 = new HackProducer2()
                assert hp2.gimme(null) == "Hello World"
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d, e)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    @Test
    void testCovariantVoidReturnOverridingObjectType() {
        def err = shouldFail '''
            class B {
                def foo() {}
            }
            class C extends B {
                void foo() {}
            }
        '''
        assert err.message.contains('The return type of void foo() in C is incompatible with java.lang.Object in B')
    }

    @Test
    void testPrimitiveObjectMix() {
        // Object overridden by primitive
        shouldFail '''
            class B { def foo() {} }
            class C extends B {
                boolean foo() {}
            }
        '''
        // primitive overridden by Object
        shouldFail '''
            class B { boolean foo() {} }
            class C extends B {
                Object foo() {}
            }
        '''
        // primitive overriding different primitive
        shouldFail '''
            class B { boolean foo() {} }
            class C extends B {
                int foo() {}
            }
        '''
    }

    // GROOVY-6330
    @Test
    void testCovariantMethodGenerics() {
        def err = shouldFail '''
            class StringIterator implements Iterator<String> {
                boolean hasNext() { false }
                def next() { null }
                void remove() {}
            }
        '''
        assert err.message.contains('The return type of java.lang.Object next() in StringIterator is incompatible with java.lang.String in java.util.Iterator')

        err = shouldFail '''
            class StringIterator implements Iterator<String> {
                boolean hasNext() { false }
                CharSequence next() { null }
                void remove() {}
            }
        '''
        assert err.message.contains('The return type of java.lang.CharSequence next() in StringIterator is incompatible with java.lang.String in java.util.Iterator')

        assertScript '''
            class StringIterator implements Iterator<CharSequence> {
                boolean hasNext() { false }
                String next() { 'dummy' }
                void remove() {}
            }

            assert new StringIterator().next() == 'dummy'
        '''
    }
}

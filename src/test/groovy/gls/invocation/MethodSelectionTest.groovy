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

final class MethodSelectionTest extends gls.CompilableTestSupport {

    /**
     * This test ensures Groovy can choose a method based on interfaces.
     * Choosing such an interface should not be hidden by subclasses.
     */
    void testMostSpecificInterface() {
        assertScript '''
            interface A{}
            interface B extends A{}
            class C implements B,A{}
            class D extends C{}
            class E implements B{}

            def m(A a){1}
            def m(B b){2}

            assert m(new D()) == 2
            assert m(new C()) == 2
            assert m(new E()) == 2
        '''
        assertScript '''
            class A implements I1 {}
            class B extends A implements I2 {}
            class C extends B implements I3 {}
            interface I1 {}
            interface I2 extends I1 {}
            interface I3 extends I2 {}

            def foo(bar) {0}
            def foo(I1 bar) {1}
            def foo(I2 bar) {2}
            def foo(I3 bar) {3}

            assert foo(new A())==1
            assert foo(new B())==2
            assert foo(new C())==3
        '''
    }

    void testMostGeneralForNull() {
        // we use the same signatures with different method orders,
        // because we want to catch method ordering bugs
        assertScript '''
          def m(String x){1}
          def m(Integer x){2}
          assert m(null) == 1

          def n(Integer x){2}
          def n(String x){1}
          assert n(null) == 1
        '''
        // Exception defines Throwable and String versions, which are both equal
        shouldFail '''
            new Exception(null)
        '''
        shouldFail '''
            class A {
                public A(String a){}
                public A(Throwable t){}
                public A(){this(null)}
            }
            new A()
        '''
        shouldFail '''
            class A{}
            class B{}
            def m(A a){}
            def m(B b){}
            m(null)
        '''
        shouldFail '''
            class A extends Exception {
                public A(){super(null)}
            }
            new A()
        '''
    }

    void testMethodSelectionException() {
        assertScript '''
            import org.codehaus.groovy.runtime.metaclass.MethodSelectionException as MSE

            def foo(int x) {}
            def foo(Number x) {}

            try {
              foo()
              assert false
            } catch (MSE mse) {
              assert mse.message.indexOf("foo()") >0
              assert mse.message.indexOf("#foo(int)") >0
              assert mse.message.indexOf("#foo(java.lang.Number)") >0
            }
        '''
    }

    // GROOVY-2719
    void testMethodSelectionWithInterfaceVargsMethod() {
        assertScript '''
            public class Thing {}
            public class Subthing extends Thing {}

            def foo(Thing i) {1}
            def foo(Runnable[] l) {2}

            assert foo(new Subthing())==1
        '''
    }

    // GROOVY-2698
    void testComplexInterfaceInheritance() {
        assertScript '''
            import javax.swing.*

            interface ISub extends Action {}

            class Super extends AbstractAction {
            void actionPerformed(java.awt.event.ActionEvent e){}
            }
            class Sub extends AbstractAction implements ISub {
            void actionPerformed(java.awt.event.ActionEvent e){}
            }

            static String foo(Action x) { "super" }
            static String foo(ISub x) { "sub" }

            def result = [new Super(), new Sub()].collect { foo(it) }

            assert ["super","sub"] == result
        '''
    }

    void testNullUsageForPrimitivesWithExplicitNull() {
        [byte,int,short,float,double,boolean,char].each { type ->
            assertScript """
                def foo($type x) {}

                boolean catched = false
                try {
                  foo(null)
                } catch (MissingMethodException mme) {
                  catched = true
                }
                def txt = "expected to see a MissingMethodEception when using foo(null) "+
                          "to call foo($type), but no exception was thrown"
                assert catched, txt
            """
        }
    }

    void testNullUsageForPrimitivesWithImplicitNull() {
        [byte,int,short,float,double,boolean,char].each { type ->
            assertScript """
                def foo($type x) {}

                boolean catched = false
                try {
                  foo()
                } catch (MissingMethodException mme) {
                  catched = true
                }
                def txt = "expected to see a MissingMethodEception when using foo(null) "+
                          "to call foo($type), but no exception was thrown"
                assert catched, txt
            """
        }
    }

    void testNullUsageForPrimitivesAndOverloading() {
        [byte,int,short,float,double].each { type ->
            assertScript """
                def foo(String x){1}
                def foo($type x) {2}

                def txt = "foo($type) was called where foo(String) should have been called"
                assert foo(null)==1, txt
            """
        }
    }

    void testPrivateMethodSelectionFromClosure(){
        assertScript '''
            class I1 {
                private foo() {1}
                def bar(){
                  def x = "foo"
                  return {this."\$x"()}
                }
            }
            class I2 extends I1 {
                def foo(){2}
            }
            def i = new I2()
            assert i.bar()() == 1
        '''
    }

    void testCachingForNullAndPrimitive(){
        assertScript '''
            boolean shaky(boolean defaultValue) {false}
            shaky(false)
            try {
                shaky(null)
                assert false : "method caching allowed null for primitive"
            } catch (MissingMethodException mme){
                assert true
            }
        '''
    }

    // GROOVY-3977
    void testBDandBIToFloatAutoConversionInMethodSelection() {
        assertScript '''
            class Foo3977 {
                double myDouble1
                Double myDouble2
                float myFloat1
                Float myFloat2
            }

            def foo = new Foo3977()

            // double examples were working already but float ones were not
            // adding both here just to make sure the inconsistency doesn't creep in again
            foo.setMyDouble1 1.0
            assert foo.getMyDouble1() == 1.0

            foo.setMyDouble1 new BigInteger('1')
            assert foo.getMyDouble1() == 1.0

            foo.setMyDouble2 1.0
            assert foo.getMyDouble2() == 1.0

            foo.setMyDouble2 new BigInteger('1')
            assert foo.getMyDouble2() == 1.0

            foo.setMyFloat1 1.0
            assert foo.getMyFloat1() == 1.0

            foo.setMyFloat1 new BigInteger('1')
            assert foo.getMyFloat1() == 1.0

            foo.setMyFloat2 1.0
            assert foo.getMyFloat2() == 1.0

            foo.setMyFloat2 new BigInteger('1')
            assert foo.getMyFloat2() == 1.0
        '''
    }

    void testCallWithExendedBigDecimal() {
        assertScript '''
            BigDecimal f (BigDecimal x) {
                return x
            }

            public class ExtendedBigDecimal extends java.math.BigDecimal {
                public ExtendedBigDecimal (int i) {
                    super (i)
                }
            }

            assert f(new ExtendedBigDecimal(1)) == 1
        '''
    }

    // GROOVY-6431
    void testBigDecAndBigIntSubClass() {
        assertScript'''
            class MyDecimal extends BigDecimal {
                public MyDecimal(String s) {super(s)}
            }
            class MyInteger extends BigInteger {
                public MyInteger(String s) {super(s)}
            }
            class Expression {
                public int takeNumber(Number a) {return 1}
                public int takeBigDecimal(BigDecimal a) {return 2}
                public int takeBigInteger(BigInteger a) {return 3}
            }

            Expression exp = new Expression();
            assert 1 == exp.takeNumber(new MyInteger("3"))
            assert 2 == exp.takeBigDecimal(new MyDecimal("3.0"))
            assert 3 == exp.takeBigInteger(new MyInteger("3"))
        '''
    }

    void testVargsClass() {
        assertScript '''
            interface Parent {}
            interface Child extends Parent {}

            class Child1 implements Child { }
            def a = new Child1()

            class Child2 implements Child { }
            def b = new Child2()

            def foo(Parent... values) {
                assert values.class == Parent[]
            }
            foo(a, b)
        '''
    }

    // GROOVY-5812
    void testDirectMethodCall() {
        assertScript '''
            private String[] getStringArrayDirectly() { ["string_00", "string_01"] }
            private String[] getStringArrayIndirectlyWithType(String[] stringarray) { stringarray }
            private String[] getStringArrayIndirectlyWithoutType(stringarray) { stringarray }

            public int getStringArrayDirectly_Length() { getStringArrayDirectly().length }
            public int getStringArrayIndirectlyWithType_Length() { getStringArrayIndirectlyWithType(getStringArrayDirectly()).length }
            public int getStringArrayIndirectlyWithoutType_Length() { getStringArrayIndirectlyWithoutType(getStringArrayDirectly()).length }

            assert getStringArrayDirectly_Length() == getStringArrayIndirectlyWithoutType_Length()
            assert getStringArrayDirectly_Length() == getStringArrayIndirectlyWithType_Length()
            assert getStringArrayIndirectlyWithType_Length() == getStringArrayIndirectlyWithoutType_Length()
        '''
    }

    // GROOVY-6189, GROOVY-9852
    void testSAMs() {
        // simple direct case
        assertScript '''
            interface MySAM {
                def someMethod()
            }
            def foo(MySAM sam) {sam.someMethod()}
            assert foo {1} == 1
        '''

        // overloads with classes implemented by Closure
        [
            'groovy.lang.Closure'            : 'not',
            'groovy.lang.GroovyCallable'     : 'not',
            'groovy.lang.GroovyObject'       : 'not',
            'groovy.lang.GroovyObjectSupport': 'not',

            'java.lang.Object'               : 'sam',
            'java.lang.Runnable'             : 'not',
            'java.lang.Cloneable'            : 'not',
            'java.io.Serializable'           : 'not',
            'java.util.concurrent.Callable'  : 'not',
        ].each { type, which ->
            assertScript """
                interface MySAM {
                    def someMethod()
                }
                def foo($type ref) { 'not' }
                def foo(MySAM sam) { sam.someMethod() }
                assert foo { 'sam' } == '$which' : '$type'
            """
        }
    }
}

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

import gls.CompilableTestSupport

class CovariantReturnTest extends CompilableTestSupport {

    void testCovariantReturn() {
        assertScript """
            class A {
                Object foo() {1}
            }
            class B extends A{
                String foo(){"2"}
            }
            def b = new B();
            assert b.foo()=="2"
            assert B.declaredMethods.findAll{it.name=="foo"}.size()==2
        """
    }

    void testCovariantReturnOverwritingAbstractMethod() {
        assertScript """
            abstract class Numeric {
                abstract Numeric eval();
            }
            class Rational extends Numeric {
                Rational eval() {this}
            }
            assert Rational.declaredMethods.findAll{it.name=="eval"}.size()==2
        """
    }

    void testCovariantReturnOverwritingObjectMethod() {
        shouldNotCompile """
            class X {
                Long toString() { 333L }
                String hashCode() { "hash" }
            }
        """
    }

    void testCovariantOverwritingMethodWithPrimitives() {
        assertScript """
            class Base {
                Object foo(boolean i) {i}
            }
            class Child extends Base {
                String foo(boolean i) {""+super.foo(i)}
            }
            def x = new Child()
            assert x.foo(true) == "true"
            assert x.foo(false) == "false"
        """
    }

    void testCovariantOverwritingMethodWithInterface() {
        assertScript """
            interface Base {
                List foo()
                Base baz()
            }
            interface Child extends Base {
                ArrayList foo()
                Child baz()
            }
            class GroovyChildImpl implements Child {
                ArrayList foo() {}
                GroovyChildImpl baz() {}
            }
            def x = new GroovyChildImpl()
            x.foo()
            x.baz()
        """
    }

    void testCovariantOverwritingMethodWithInterfaceAndInheritance() {
        assertScript """
            interface Base {
                List foo()
                List bar()
            Base baz()
            }
            interface Child extends Base {
                ArrayList foo()
            }
            class MyArrayList extends ArrayList { }
            class GroovyChildImpl implements Child {
                MyArrayList foo() {}
                MyArrayList bar() {}
                GroovyChildImpl baz() {}
            }
            def x = new GroovyChildImpl()
            x.foo()
            x.bar()
            x.baz()
        """
    }

    void testCovariantMethodFromParentOverwritingMethodFromInterfaceInCurrentclass() {
        assertScript """
            interface I {
                def foo()
            }
            class A {
                String foo(){""}
            }
            class B extends A implements I{}
            def b = new B()
            assert b.foo() == ""
        """

        // basically the same as above, but with an example
        // from an error report (GROOVY-2582)
        // Properties has a method "String getProperty(String)", this class
        // is also a GroovyObject, meaning a "Object getProperty(String)" method
        // should be implemented. But this method should not be the usual automatically
        // added getProperty, but a bridge to the getProperty method provided by Properties
        assertScript """
            class Configuration extends java.util.Properties {}
            assert Configuration.declaredMethods.findAll{it.name=="getProperty"}.size() == 1
            def conf = new Configuration()
            conf.setProperty("a","b")
            // the following assert would fail if standard getProperty method was added
            // by the compiler
            assert conf.getProperty("a") == "b"
        """

        assertScript """
            class A {}
            class B extends A {}
            interface Contract {
                A method()
            }
            abstract class AbstractContract implements Contract {}
            class ContractImpl extends AbstractContract {
                B method(String foo='default') { new B() }
            }
            assert new ContractImpl().method() instanceof B
        """
    }

    void testImplementedInterfacesNotInfluencing() {
        // in GROOVY-3229 some methods from Appendable were not correctly recognized
        // as already being overridden (PrintWriter<Writer<Appenable)
        shouldCompile """
            class IndentWriter extends java.io.PrintWriter {
               public IndentWriter(Writer w)  { super(w, true) }
            }
        """
    }

    void testCovariantMethodReturnTypeFromGenericsInterface() {
        shouldCompile """
            interface MyCallable<T> {
                T myCall() throws Exception;
            }
            class Task implements MyCallable<List> {
                List myCall() throws Exception {
                    return [ 42 ]
                }
            }
        """
    }

    //GROOVY-7495
    void testCovariantMerhodReturnTypeFromParentInterface() {
        shouldCompile """
            interface Item {}
            interface DerivedItem extends Item {}

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
                DerivedItem getItem() { null }
            }

            interface Second extends First {}
            class SecondImpl extends FirstImpl implements Second {}
        """
        shouldCompile """
            interface A {
               B foo()
            }
            interface B {}
            interface A2 extends A {
               B2 foo()
            }
            interface B2 extends B {}

            class AA implements A {
               BB foo() { return new BB() }
            }
            class AA2 extends AA implements A2 {
               BB2 foo() { return new BB2() }
            }
            class BB implements B {}
            class BB2 extends BB implements B2 {}
        """
    }

    void testCovariantParameterType() {
        assertScript """
            class BaseA implements Comparable<BaseA> {
                int index
                public int compareTo(BaseA a){
                    return index <=> a.index
                }
            }

            def a = new BaseA(index:1)
            def b = new BaseA(index:2)
            assert a < b
        """
    }

    void testBridgeMethodExistsOnImplementingClass() {
        shouldCompile """
            interface IBar<T>{
                int testMethod(T obj)
            }
            class Bar implements IBar<Date>{
                int testMethod(Date dt){}
                int testMethod(Object obj){}
            }
        """
    }

    void testCovariantVoidReturnTypeOverridingObjectType() {
        shouldNotCompile """
            class BaseClass {
                def foo() {}
            }
            class DerivedClass extends BaseClass {
                void foo() {}
            }
        """
    }
    
    void testPrimitiveObjectMix() {
        //Object overridden by primitive
        shouldNotCompile """
            class BaseClass{ def foo(){} }
            class DerivedClass extends BaseClass {
                boolean foo(){}
            }
        """
        //primitive overridden by Object
        shouldNotCompile """
            class BaseClass{ boolean foo(){} }
            class DerivedClass extends BaseClass {
                Object foo(){}
            }
        """
        //primitive overriding different primitive
        shouldNotCompile """
            class BaseClass{ boolean foo(){} }
            class DerivedClass extends BaseClass {
                int foo(){}
            }
        """
    }

    void testCovariantMethodGenerics_groovy6330() {
        shouldNotCompile """
            class StringIterator implements Iterator<String> {
                void remove() { }
                boolean hasNext() { false }
                def next() { 'dummy' }
            }
        """
        shouldNotCompile """
            class StringIterator implements Iterator<String> {
                void remove() { }
                boolean hasNext() { false }
                CharSequence next() { null }
            }
        """
        assertScript """
            class StringIterator implements Iterator<CharSequence> {
                void remove() { }
                boolean hasNext() { false }
                String next() { 'dummy' }
            }
            assert new StringIterator().next() == 'dummy'
        """
    }

    void testCovariantParameter() {
        assertScript """
          interface Interface<SomeType> {
             public int handle(long a, SomeType x);
          }
          
          class InterfaceImpl
          implements Interface<String> {
              public int handle(long a, String something) {
                  return 1
              }
          }
          
          InterfaceImpl test = new InterfaceImpl()
          assert test.handle(5, "hi") == 1
        """
    }

}

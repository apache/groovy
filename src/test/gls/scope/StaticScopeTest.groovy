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
package gls.scope

import gls.CompilableTestSupport

class StaticScopeTest extends CompilableTestSupport {

    void testNormalStaticScopeInScript() {
        shouldNotCompile """
        static foo() {
            foo = 1
        }
        """

        shouldCompile """
        static foo() {
            def foo=1
        }
        """
    }

    void testStaticImportInclass() {
        assertScript """
        import static java.lang.Math.*
        class B {
            static main(args) { assert cos(2 * PI) == 1.0 }
        }
        """
    }

    void testStaticMethodInConstructor() {
        assertScript """
        class B {
            def instanceVariable = 0
            static classVariable = 0
            B() { instanceVariable = method(); init() }
            static int method() { 1 }
            static int init() { classVariable = 2 }
        }
        assert new B().instanceVariable == 1
        assert B.classVariable == 2
        """
    }

    void testStaticMethodInSpecialConstructorCall() {
        assertScript """
        class A {
            def instanceVariable = 0
            A(x) { instanceVariable = x }
            A(x, y) { this( method(x + y) ) }
            static int method(x) { 2 * x }
        }
        assert new A(2).instanceVariable == 2
        assert new A(2, 5).instanceVariable == 14
        """
        assertScript """
        class B {
            def instanceVariable = 0
            B(x) { instanceVariable = x }
        }
        class C extends B {
            C(x, y) { super( method(x + y) ) }
            static int method(x) { 2 * x }
        }
        assert new B(2).instanceVariable == 2
        assert new C(2, 5).instanceVariable == 14
        """
    }

    void testStaticImportProperty() {
        assertScript """
        import static A.*
        class B {
            static main(args) { assert temperature == 42 }
        }
        class A { static temperature = 42 }
        """
    }

    void testNormalStaticScopeInClass() {
        assertScript """
        class A {
            static i
            static foo() {
                i=1
            }
        }
        A.foo()
        assert A.i == 1
        """

        shouldNotCompile """
        class A {
            def i
            static foo() {
                i=1
            }
        }
        """
    }

    void testClosureInStaticScope() {
        shouldCompile """
        5.times { foo=2 }
        """

        shouldCompile """
        5.times { foo=it }
        """
    }

    void testScriptMethodCall() {
        assertScript """
        import static java.util.Calendar.getInstance as now
        def now = now().time
        assert now.class == Date
        """

        // TODO: why does in-lining of now variable from above break? GROOVY-1809 issue?
        shouldCompile """
        import static java.util.Calendar.getInstance as now
        assert now().time.class == Date
        """

        shouldCompile """
        import static java.lang.Math.*
        cos(cos(cos(PI)))
        """
    }

    void testFullyQualifiedClassName() {
        assertScript """
        static foo() {java.lang.Integer}
        assert foo() == java.lang.Integer
        """

        shouldNotCompile """
        static foo() { java.lang.JavaOrGroovyThatsTheQuestion }
        """

        shouldCompile """
        foo() { java.lang.JavaOrGroovyThatsTheQuestion }
        """
    }

    void testStaticPropertyInit() {
        // GROOVY-1910
        assertScript """
        class Foo {
             static p1 = 1
             static p2 = p1
        }
        assert Foo.p2 == Foo.p1
        assert Foo.p1 == 1
        """

        // should not compile for mistyped name
        shouldNotCompile """
        class Foo {
            static p1 = 1
            static p2 = x1
        }
        assert Foo.p2 == Foo.p1
        assert Foo.p1 == 1
        """
    }

    void testSpecialConstructorAccess() {
        shouldCompile """
        class A{ A(x){} }
        class B extends A {
            B(x) { super(x) }
        }
        """

        shouldCompile """
        class A{ A(x){} }
        class B extends A {
            B(x) { super(x.something) }
        }
        """

        shouldNotCompile """
        class A{ A(x){} }
        class B extends A {
            B(x) { super(nonExistingParameter) }
        }
        """

        shouldNotCompile """
        class A{ A(x){} }
        class B extends A {
            def doNotAccessDynamicFieldsOrProperties
            B(x) { super(doNotAccessDynamicFieldsOrProperties) }
        }
        """

        shouldCompile """
        class A{ A(x){} }
        class B extends A {
            static allowUsageOfStaticPropertiesAndFields
            B(x) { super(allowUsageOfStaticPropertiesAndFields) }
        }
        """
    }
    
    void testStaticMethodAccessingDynamicField() {
        shouldFail MissingMethodException, """
            class A {
                def x = { }
                static foo() { x() }
            } 
            A.foo()
        """
    }
    
    void testStaticThisWithClass() {
        assertScript """
            static foo(){this}
            assert foo() instanceof Class
            assert foo() != Class
        """
        
        assertScript """
            static foo(){this.class}
            assert foo() == Class
        """
    }
    
    void testConstructorParameterDefault() {
        shouldNotCompile """
            class Child {
                Child(nr=map.B) {super(nr)}
            }
        """
        shouldNotCompile """
            class Parent {
                def map = [A:1, B:2, C:3]
                def nr
                Parent(nr) {this.nr = nr}
            }
            class Child extends Parent {
                Child(nr=map.B) {super(nr)}
            }
        """
    }
}

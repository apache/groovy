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
package groovy

import gls.CompilableTestSupport

final class AbstractClassAndInterfaceTest extends CompilableTestSupport {

    void testInterface() {
        def shell = new GroovyShell()
        def text = '''
            interface A {
                void methodOne(Object o)
                Object methodTwo()
            }

            class B implements A {
                void methodOne(Object o){assert true}
                Object methodTwo(){
                    assert true
                    methodOne(null)
                    return new Object()
                }
            }

            def b = new B()
            return b.methodTwo()
        '''
        def retVal = shell.evaluate(text)
        assert retVal.class == Object
    }

    void testClassImplementingAnInterfaceButMissesMethod() {
        shouldNotCompile '''
            interface A {
                void methodOne(Object o)
                Object methodTwo()
            }

            class B implements A {
                void methodOne(Object o){assert true}
            }

            def b = new B()
            return b.methodTwo()
        '''

        shouldNotCompile '''
            interface A {
                Object methodTwo()
            }
            interface B extends A{
                void methodOne(Object o)
            }

            class C implements A {
                void methodOne(Object o){assert true}
            }

            def b = new C()
            return b.methodTwo()
        '''
    }

    void testClassImplementingNestedInterfaceShouldContainMethodsFromSuperInterfaces() {
        shouldNotCompile '''
            interface A { def a() }
            interface B extends A { def b() }
            class BImpl implements B {
                def b(){ println 'foo' }
            }
            new BImpl().b()
        '''
    }

    void testAbstractClass() {
        def shell = new GroovyShell()
        def text = '''
            abstract class A {
                abstract void methodOne(Object o)
                Object methodTwo(){
                    assert true
                    methodOne(null)
                    return new Object()
                }
            }

            class B extends A {
                void methodOne(Object o){assert true}
            }

            def b = new B()
            return b.methodTwo()
        '''
        def retVal = shell.evaluate(text)
        assert retVal.class == Object
    }

    void testClassExtendingAnAbstractClassButMissesMethod() {
        shouldNotCompile '''
            abstract class A {
                abstract void methodOne(Object o)
                Object methodTwo(){
                    assert true
                    methodOne(null)
                    return new Object()
                }
                abstract void MethodThree()
            }

            abstract class B extends A {
                void methodOne(Object o){assert true}
            }

            class C extends B{}

            def b = new C()
            return b.methodTwo()
        '''

       shouldNotCompile '''
            abstract class A {
                abstract void methodOne(Object o)
                Object methodTwo(){
                    assert true
                    methodOne(null)
                    return new Object()
                }
                abstract void MethodThree()
            }

            class B extends A {
                void methodOne(Object o){assert true}
            }

            def b = new B()
            return b.methodTwo()
        '''
    }

    void testInterfaceAbstractClassCombination() {
        def shell = new GroovyShell()
        def text = '''
            interface A {
                void methodOne()
            }

            abstract class B implements A{
                abstract void methodTwo()
            }

            class C extends B {
                void methodOne(){assert true}
                void methodTwo(){
                  methodOne()
                }
            }
            def c = new C()
            c.methodTwo()
        '''
        shell.evaluate(text)

        shouldNotCompile '''
            interface A {
                void methodOne()
            }

            abstract class B implements A{
                abstract void methodTwo()
            }

            class C extends B {}
            def c = new c()
            c.methodTwo()
        '''
    }

    void testDefaultModifiersForInterfaces() {
        def shell = new GroovyShell()
        def text = '''
            import java.lang.reflect.Modifier

            interface A {
                def foo
            }

            def fields = A.class.declaredFields
            assert fields.length==1
            assert fields[0].name == 'foo'
            assert Modifier.isPublic (fields[0].modifiers)
            assert Modifier.isStatic (fields[0].modifiers)
            assert Modifier.isFinal  (fields[0].modifiers)
        '''
        shell.evaluate(text)
    }

    void testAccessToInterfaceField() {
        def shell = new GroovyShell()
        def text = '''
            interface A {
                def foo=1
            }
            class B implements A {
              def foo(){foo}
            }
            assert new B().foo()==1
       '''
       shell.evaluate(text)
    }

    void testImplementsDuplicateInterface() {
        shouldCompile '''
            interface I {}
            class C implements I {}
        '''
        shouldNotCompile '''
            interface I {}
            class C implements I, I {}
        '''
    }

    void testDefaultMethodParamsNotAllowedInInterface() {
        shouldCompile '''
            interface Foo {
                def doit(String string, int i)
            }
        '''
        shouldNotCompile '''
            interface Foo {
                def doit(String string = 'Groovy', int i)
            }
        '''
    }

    void testClassImplementsItselfCreatingACycle() {
        shouldNotCompile '''
            package p1
            class XXX implements XXX {}
        '''
        shouldNotCompile '''
            class YYY implements YYY {}
        '''
    }

    void testAbstractClassWithPrivateAbstractMethod() {
        def msg = shouldNotCompile '''
            abstract class X {
                private abstract void y()
            }
        '''
        assert msg.contains("The method 'y' must not be private as it is declared abstract in class 'X'")
    }

    void testAbstractClassWithPrivateAbstractMethods() {
        def msg = shouldNotCompile '''
            abstract class X {
                private abstract void y()
                private abstract void z()
            }
        '''
        assert msg.contains("The method 'y' must not be private as it is declared abstract in class 'X'")
        assert msg.contains("The method 'z' must not be private as it is declared abstract in class 'X'")
    }

    void testAbstractNestedClassWithPrivateAbstractMethod() {
        def msg = shouldNotCompile '''
            class Z {
                abstract class X {
                    private abstract void y()
                }
            }
        '''
        assert msg.contains("The method 'y' must not be private as it is declared abstract in class 'Z\$X'")
    }

    void testClassWithPrivateAbstractMethod() {
        def msg = shouldNotCompile '''
            class X {
                private abstract void y()
            }
        '''
        assert !msg.contains("The method 'y' must not be private as it is declared abstract in class 'X'")
        assert msg.contains("Can't have an abstract method in a non-abstract class. The class 'X' must be declared abstract or the method 'void y()' must be implemented.")
    }

    void testEnumWithPrivateAbstractMethod() {
        def msg = shouldNotCompile '''
            enum X {
                CONST {
                    private void y() { }
                }

                private abstract void y()
            }
        '''
        assert msg.contains("The method 'y' must not be private as it is declared abstract in enum 'X'")
    }

    void testInterfaceWithPrivateAbstractMethod() {
        def msg = shouldNotCompile '''
            interface X {
                private abstract void y()
            }
        '''
        assert msg.contains("The method 'y' must be public as it is declared abstract in interface 'X'")
    }
}

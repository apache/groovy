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
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy8283 {

    @Test
    void testReadFieldPropertyShadowing() {
        def shell = new GroovyShell()
        shell.parse '''package p
            class A {}
            class B {}
            class C {
                protected A foo = new A()
                A getFoo() { return foo }
            }
            class D extends C {
                protected B foo = new B()
            }
        '''
        assertScript shell, '''import p.*
            class E extends D {
                void test() {
                    assert foo.class == B
                    assert this.foo.class == B
                    assert this.@foo.class == B
                    assert this.getFoo().getClass() == A

                    def that = new E()
                    assert that.foo.class == B
                    assert that.@foo.class == B
                    assert that.getFoo().getClass() == A
                }
            }

            new E().test()
            assert new E().foo.class == A // not the field from this perspective
        '''
    }

    @Test
    void testReadFieldPropertyShadowing2() {
        def shell = new GroovyShell()
        shell.parse '''package p
            class A {}
            class B {}
            class C {
                protected A foo = new A()
                A getFoo() { return foo }
            }
            class D extends C {
                protected B foo = new B()
            }
        '''
        assertScript shell, '''import p.*
            class E extends D {
                @groovy.transform.ASTTest(phase=org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION, value={
                    def typeof = { label -> lookup(label)[0].getExpression().getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE).toString(false) }

                    assert typeof('implicit'   ) == 'p.B'
                    assert typeof('explicit'   ) == 'p.B'
                    assert typeof('attribute'  ) == 'p.B'
                    assert typeof('methodCall' ) == 'p.A'

                    assert typeof('property'   ) == 'p.B'
                    assert typeof('attribute2' ) == 'p.B'
                    assert typeof('methodCall2') == 'p.A'
                })
                @groovy.transform.TypeChecked
                void test() {
                  implicit:
                    def a = foo
                  explicit:
                    def b = this.foo
                  attribute:
                    def c = this.@foo
                  methodCall:
                    def d = this.getFoo()

                    def that = new E()
                  property:
                    def x = that.foo
                  attribute2:
                    def y = that.@foo
                  methodCall2:
                    def z = that.getFoo()
                }
            }

            @groovy.transform.TypeChecked
            void test() {
                @groovy.transform.ASTTest(phase=org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION, value={
                    def type = node.getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE)
                    assert type.toString(false) == 'p.A'
                })
                def a = new E().foo
            }
        '''
    }

    @Test
    void testReadFieldPropertyShadowing3() {
        def shell = GroovyShell.withConfig {
            ast(groovy.transform.CompileStatic)
        }
        shell.parse '''package p
            class A {}
            class B {}
            class C {
                protected A foo = new A()
                A getFoo() { return foo }
            }
            class D extends C {
                protected B foo = new B()
            }
        '''
        assertScript shell, '''import p.*
            class E extends D {
                void test() {
                    assert foo.class == B
                    assert this.foo.class == B
                    assert this.@foo.class == B
                    assert this.getFoo().getClass() == A

                    def that = new E()
                    assert that.foo.class == B
                    assert that.@foo.class == B
                    assert that.getFoo().getClass() == A
                }
            }

            new E().test()
            assert new E().foo.class == A // not the field from this perspective
        '''
    }

    @Test
    void testWriteFieldPropertyShadowing() {
        def shell = new GroovyShell()
        shell.parse '''package p
            class A {}
            class B {}
            class C {
                boolean setter
                protected A foo = new A()
                A getFooA() { return this.@foo }
                void setFoo(A a) { setter = true; this.@foo = a }
            }
            class D extends C {
                protected B foo = new B() // hides A#foo; should hide A#setFoo in subclasses
                B getFooB() { return this.@foo }
            }
        '''
        assertScript shell, '''import p.*
            class E extends D {
                void test1() {
                    foo = null
                    assert !setter
                    assert fooA != null
                    assert fooB == null
                }
                void test2() {
                    this.foo = null
                    assert !setter
                    assert fooA != null
                    assert fooB == null
                }
                void test3() {
                    this.@foo = null
                    assert !setter
                    assert fooA != null
                    assert fooB == null
                }
                void test4() {
                    this.setFoo(null)
                    assert setter
                    assert fooA == null
                    assert fooB != null
                }
                void test5() {
                    def that = new E()
                    that.foo = null
                    assert !that.setter
                    assert that.fooA != null
                    assert that.fooB == null

                    that = new E()
                    that.@foo = null
                    assert !that.setter
                    assert that.fooA != null
                    assert that.fooB == null

                    that = new E()
                    that.setFoo(null)
                    assert that.setter
                    assert that.fooA == null
                    assert that.fooB != null
                }
            }

            new E().test1()
            new E().test2()
            new E().test3()
            new E().test4()
            new E().test5()

            def e = new E()
            e.foo = null // not the field from this perspective
            assert e.setter
            assert e.fooA == null
            assert e.fooB != null
        '''
    }
}

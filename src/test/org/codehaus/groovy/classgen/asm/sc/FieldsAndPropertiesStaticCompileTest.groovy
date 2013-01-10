/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.FieldsAndPropertiesSTCTest

@Mixin(StaticCompilationTestSupport)
class FieldsAndPropertiesStaticCompileTest extends FieldsAndPropertiesSTCTest {
    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }
    
    void testMapGetAt() {
        assertScript '''
            Map map = [a: 1, b:2]
            String key = 'b'
            assert map['a'] == 1
            assert map[key] == 2
        '''
    }

    void testGetAtFromStaticMap() {
        assertScript '''
            class Foo {
                public static Map CLASSES = [:]
            }
            String foo = 'key'
            Foo.CLASSES[foo]
        '''
    }

    // GROOVY-5561
    void testShouldNotThrowAccessForbidden() {
        assertScript '''
        class Test {
            def foo() {
                def bar = createBar()
                bar.foo
            }

            Bar createBar() { new Bar() }
        }
        class Bar {
            List<String> foo = ['1','2']
        }
        assert new Test().foo() == ['1','2']
        '''
    }

    // GROOVY-5579
    void testUseSetterAndNotSetProperty() {
        assertScript '''
                Date d = new Date()
                d.time = 1

                assert d.time == 1
                '''
        assert astTrees.values().any {
            it.toString().contains 'INVOKEVIRTUAL java/util/Date.setTime (J)V'
        }
    }

    void testUseDirectWriteFieldFromWithinClass() {
        assertScript '''
            class A {
                int x
                A() {
                    x = 5
                }
            }
            new A()
        '''
        // one PUTFIELD in constructor + one PUTFIELD in setX
        assert (astTrees['A'][1] =~ 'PUTFIELD A.x').collect().size() == 2
    }

    void testUseDirectWriteFieldFromWithinClassWithPrivateField() {
        assertScript '''
            class A {
                private int x
                A() {
                    x = 5
                }
            }
            new A()
        '''
        // one PUTFIELD in constructor
        assert (astTrees['A'][1] =~ 'PUTFIELD A.x').collect().size() == 1
    }

    void testUseDirectWriteFieldFromWithinClassWithProtectedField() {
        assertScript '''
            class A {
                protected int x
                A() {
                    x = 5
                }
            }
            new A()
        '''
        // one PUTFIELD in constructor
        assert (astTrees['A'][1] =~ 'PUTFIELD A.x').collect().size() == 1
    }

    void testUseDirectWriteFieldAccess() {
        assertScript '''
                class A {
                        boolean setterCalled = false

                        protected int x
                        public void setX(int a) {
                            setterCalled = true
                            x = a
                        }
                }
                class B extends A {
                    void directAccess() {
                        this.@x = 2
                    }
                }
                B b = new B()
                b.directAccess()
                assert b.isSetterCalled() == false
                assert b.x == 2
            '''
        assert astTrees['B'][1].contains('PUTFIELD A.x')
    }

    void testUseDirectWriteStaticFieldAccess() {
        assertScript '''
            class A {
                    static boolean setterCalled = false

                    static protected int x
                    public static void setX(int a) {
                        setterCalled = true
                        x = a
                    }
            }
            class B extends A {
                static void directAccess() {
                    this.@x = 2
                }
            }
            B.directAccess()
            assert B.isSetterCalled() == false
            assert B.x == 2
                '''
        assert astTrees['B'][1].contains('PUTSTATIC A.x')
    }

    void testUseSetterFieldAccess() {
        assertScript '''
                class A {
                        boolean setterCalled = false

                        protected int x
                        public void setX(int a) {
                            setterCalled = true
                            x = a
                        }
                }
                class B extends A {
                    void setterAccess() {
                        this.x = 2
                    }
                }
                B b = new B()
                b.setterAccess()
                assert b.isSetterCalled() == true
                assert b.x == 2
            '''
        assert astTrees['B'][1].contains('INVOKEVIRTUAL A.setX')
    }

    void testDirectReadFieldFromSameClass() {
        assertScript '''
            class A {
                int x
                public int getXX() {
                    x // should do direct access
                }
            }
            A a = new A()
            assert a.getX() == a.getXX()
        '''
        // one GETFIELD in getX() + one GETFIELD in getXX
        assert (astTrees['A'][1] =~ 'GETFIELD A.x').collect().size() == 2
    }

    void testDirectFieldFromSuperClassShouldUseGetter() {
        assertScript '''
            class A {
                int x
            }
            class B extends A {
                public int getXX() { x }
            }
            B a = new B()
            assert a.getX() == a.getXX()
        '''
        // no GETFIELD in getXX
        assert (astTrees['B'][1] =~ 'GETFIELD A.x').collect().size() == 0
        // getX in getXX
        assert (astTrees['B'][1] =~ 'INVOKEVIRTUAL A.getX').collect().size() == 1
    }

    void testUseDirectReadFieldAccess() {
        assertScript '''
                class A {
                        boolean getterCalled = false

                        protected int x
                        public int getX() {
                            getterCalled = true
                            x
                        }
                }
                class B extends A {
                    void m() {
                        this.@x
                    }
                }
                B b = new B()
                b.m()
                assert b.isGetterCalled() == false
            '''
        assert astTrees['B'][1].contains('GETFIELD A.x')
    }

    void testUseGetterFieldAccess() {
        assertScript '''
                    class A {
                            boolean getterCalled = false

                            protected int x
                            public int getX() {
                                getterCalled = true
                                x
                            }
                    }
                    class B extends A {
                        void usingGetter() {
                            this.x
                        }
                    }
                    B b = new B()
                    b.usingGetter()
                    assert b.isGetterCalled() == true
                '''
        assert astTrees['B'][1].contains('INVOKEVIRTUAL A.getX')
    }

    void testUseAttributeExternal() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            A a = new A()
            a.@x = 100
            assert a.x == 100
            assert a.isSetterCalled() == false
        '''
    }
    void testUseAttributeExternalSafe() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            A a = new A()
            a?.@x = 100
            assert a.x == 100
            assert a.isSetterCalled() == false
        '''
    }
    void testUseAttributeExternalSafeWithNull() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            A a = null
            a?.@x = 100
        '''
    }
    void testUseGetterExternal() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            A a = new A()
            a.x = 100
            assert a.x == 100
            assert a.isSetterCalled() == true
        '''
    }

    void testUseAttributeExternalSpread() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<A> a = [new A(), new A()]
            a*.@x = 100
          println a[0].x == 100
          println a[0].isSetterCalled() == false
        '''
    }

    void testUseAttributeExternalSpreadSafeWithNull() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<A> a = [new A(), null]
            a*.@x = 100
            assert a[0].x == 100
            assert a[0].isSetterCalled() == false
            assert a[1] == null
        '''
    }

    void testUseAttributeExternalSpreadUsingSetter() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<A> a = [new A(), new A()]
            a*.x = 100
            assert a[0].x == 100
            assert a[0].isSetterCalled() == true
        '''
    }

    void testUseAttributeExternalSpreadSafeWithNullUsingSetter() {
        assertScript '''
            class A {
                boolean setterCalled = false
                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<A> a = [new A(), null]
            a*.x = 100
            assert a[0].x == 100
            assert a[0].isSetterCalled() == true
            assert a[1] == null
        '''
    }

    // GROOVY-5649
    void testShouldNotThrowStackOverflowUsingThis() {
        new GroovyShell().evaluate '''class HaveOption {

          private String helpOption;


          @groovy.transform.CompileStatic
          public void setHelpOption(String helpOption) {
            this.helpOption = helpOption
          }

        }
        def o = new HaveOption()
        o.setHelpOption 'foo'
        assert o.helpOption
        '''
    }
    void testShouldNotThrowStackOverflow() {
        new GroovyShell().evaluate '''class HaveOption {

          private String helpOption;


          @groovy.transform.CompileStatic
          public void setHelpOption(String ho) {
            helpOption = ho
          }

        }
        def o = new HaveOption()
        o.setHelpOption 'foo'
        assert o.helpOption
        '''
    }

}

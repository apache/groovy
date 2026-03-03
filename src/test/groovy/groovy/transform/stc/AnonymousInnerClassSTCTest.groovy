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
package groovy.transform.stc

import org.junit.jupiter.api.Test

/**
 * Unit tests for static type checking : anonymous inner classes.
 */
class AnonymousInnerClassSTCTest extends StaticTypeCheckingTestCase {

    // GROOVY-5565
    @Test
    void testShouldNotThrowNPE() {
        assertScript '''
            Serializable s = new Serializable() { List things = [] }
            assert s.things.size() == 0
        '''
        assertScript '''
            Serializable s = new Serializable() { List things = [1] }
            assert s.things.size() == 1
        '''
    }

    @Test
    void testAssignmentOfAICFromInterface() {
        assertScript '''
            Runnable r = new Runnable() {
                public void run() { println 'ok' }
            }
            r.run()
        '''
    }

    @Test
    void testAssignmentOfAICFromAbstractClass() {
        assertScript '''
            abstract class Foo { abstract String item() }
            Foo f = new Foo() {
                String item() { 'ok' }
            }
            assert f.item() == 'ok'
        '''
    }

    @Test
    void testAssignmentOfAICFromAbstractClassAndInterface() {
        assertScript '''
            abstract class Foo  implements Runnable { abstract String item() }
            Foo f = new Foo() {
                String item() { 'ok' }
                void run() {}
            }
            assert f.item() == 'ok'
            f.run()
        '''
    }

    @Test
    void testCallMethodUsingAIC() {
        assertScript '''
            abstract class Foo { abstract String item() }
            boolean valid(Foo foo) {
                foo.item() == 'ok'
            }
            def f = new Foo() {
                String item() { 'ok' }
            }
            assert valid(f)
        '''
    }

    @Test
    void testCallMethodUsingAICImplementingInterface() {
        assertScript '''
            abstract class Foo implements Runnable { abstract String item() }
            boolean valid(Foo foo) {
                foo.item() == 'ok'
            }
            def f = new Foo() {
                String item() { 'ok' }
                void run() {}
            }
            assert valid(f)
            f.run()
        '''
    }

    @Test
    void testAICReferencingOuterMethod() {
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

    // GROOVY-6882
    @Test
    void testAICReferencingOuterMethodOverride() {
        assertScript '''
            class B {
                def m() { 'B' }
            }

            class C extends B {
                @Override
                def m() { 'C' }

                void test() {
                    def aic = new Runnable() {
                        void run() {
                            assert m() == 'C' // Cannot choose between [C#m(), B#m()]
                        }
                    }
                    aic.run()

                    assert m() == 'C'
                }
            }

            new C().test()
        '''
    }

    // GROOVY-5566
    @Test
    void testAICReferencingOuterLocalVariable() {
        assertScript '''
            def foo() {
                List things = []
                Serializable s = new Serializable() {
                  def size() {
                    things.size()
                  }
                }
                s.size()
            }
        '''
    }

    @Test
    void testAICInAICInStaticMethod() {
        assertScript '''
            class A {
                public static foo() {
                    return new Object() {
                        public String toString() {
                            return new Object() {
                                public String toString() {
                                    "ii"
                                }
                            }.toString()+" i"
                        }
                    }.toString()
                }
            }
            assert A.foo() == "ii i"
        '''
    }

    // GROOVY-6904
    @Test
    void testAICInClosure() {
        assertScript '''
            interface A {
                def m()
            }
            class C {
                def bar(Closure<? extends A> closure) {
                    closure().m()
                }
                def foo() {
                    bar { ->
                        return new A() {
                            def m() { p }
                        }
                    }
                }
                final String p = 'p'
            }

            assert new C().foo() == 'p'
        '''
    }

    @Test
    void testAICWithGenerics() {
        assertScript '''
            Comparator<Integer> comp = new Comparator<Integer>(){
                @Override
                int compare(Integer o1, Integer o2) {
                    return 0
                }
            }
        '''
    }

    // GROOVY-5728
    @Test
    void testPrivateConstructorAndPublicStaticFactory() {
        assertScript '''
            abstract class A {
                private A() { }
                abstract answer()
                static A create() {
                    return new A() { // IllegalAccessError when A$1 calls private constructor
                        def answer() { 42 }
                    }
                }
            }

            assert A.create().answer() == 42
        '''
    }

    @Test
    void testPrivateFieldAccess() {
        assertScript '''
            class C {
                private int x
                void test() {
                    def aic = new Runnable() {
                        void run() { x = 666 }
                    }
                    aic.run()
                    assert x == 666
                }
            }

            new C().test()
        '''
    }

    // GROOVY-7994
    @Test
    void testOuterPropertyAccess() {
        String other = '''
            class Other {
                public final String text
                String toString() { text }
                Other(             ) { text = "" }
                Other(Object object) { text = "Object:$object" }
                Other(String string) { text = "String:$string" }
                static String foo(Object object) { "Object:$object" }
                static String foo(String string) { "String:$string" }
                       String bar(Object object) { "Object:$object" }
                       String bar(String string) { "String:$string" }
            }
        '''
        assertScript other + '''
            class Outer {
                String getP() { 'x' }
                String test() {
                    [ new Other(p), new Other(getP()) ].join('|')
                }
            }

            String result = new Outer().test()
            assert result == 'String:x|String:x'
        '''
        assertScript other + '''
            class Outer {
                String getP() { 'x' }
                class Inner {
                    String test() { // unqualified "p" should resolve to outer property
                        [ new Other(p), new Other(Outer.this.p), new Other(getP()) ].join('|')
                    }
                }
            }

            String result = new Outer.Inner(new Outer()).test()
            assert result == 'String:x|String:x|String:x'
        '''
        assertScript other + '''
            class Outer {
                String getP() { 'x' }
                String test() {
                    new Object() {
                        String toString() {
                            [ new Other(p), new Other(Outer.this.p), new Other(getP()) ].join('|')
                        }
                    }
                }
            }

            String result = new Outer().test()
            assert result == 'String:x|String:x|String:x'
        '''
        assertScript other + '''
            class Outer {
                String p = 'x'
                String test() {
                    new Object() {
                        String toString() {
                            [ new Other(p), new Other(getP()) ].join('|')
                        }
                    }
                }
            }

            String result = new Outer().test()
            assert result == 'String:x|String:x'
        '''
        assertScript other + '''
            class Outer {
                String getP() { 'x' }
                String test() {
                    new Object() {
                        String toString() {
                            [ Other.foo(p), Other.foo(Outer.this.p), Other.foo(getP()) ].join('|')
                        }
                    }
                }
            }

            String result = new Outer().test()
            assert result == 'String:x|String:x|String:x'
        '''
        assertScript other + '''
            class Outer {
                String getP() { 'x' }
                String test() {
                    new Object() {
                        String toString() {
                            [ new Other().bar(p), new Other().bar(getP()) ].join('|')
                        }
                    }
                }
            }

            String result = new Outer().test()
            assert result == 'String:x|String:x'
        '''
        assertScript other + '''
            class Outer {
                String getP() { 'x' }
                String test() {
                    new Object() {
                        String toString() {
                            new Other().with { [ bar(p), bar(getP()) ].join('|') }
                        }
                    }
                }
            }

            String result = new Outer().test()
            assert result == 'String:x|String:x'
        '''
    }
}

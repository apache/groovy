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
package org.codehaus.groovy.transform

import groovy.test.GroovyShellTestCase

/**
 * Tests for the {@code @Newify} AST transform.
 */
class NewifyTransformTest extends GroovyShellTestCase {

    void testNewify() {
        def main = evaluate("""
              @Newify() class Main {
                  def field1 = Integer.new(42)
                  @Newify(Integer)
                  def field2 = Integer(43)
              }
              new Main()
        """)

        assertEquals main.field1, 42
        assertEquals main.field2, 43
    }

    void testNewifyCompileStatic() {
        def main = evaluate("""
            @groovy.transform.CompileStatic
            @Newify() class Main {
                def field1 = Integer.new(42)
                @Newify(Integer)
                def field2 = Integer(43)
            }
            new Main()
        """)
        assertEquals main.field1, 42
        assertEquals main.field2, 43
    }

    void testClassLevelNewification() {
        evaluate """
            @Newify class Rubyesque {
                static main(args) {
                    assert Integer.new(40) == 40
                }
            }
        """

        evaluate """
            @Newify(Integer) class Pythonesque {
                static main(args) {
                    assert Integer(41) == 41
                }
            }
        """
    }

    void testMethodLevelNewification() {
        evaluate """
            class Rubyesque {
                static main(args) {
                    foo()
                }
                @Newify static foo() {
                    assert Integer.new(42) == 42
                }
            }
        """

        evaluate """
            class Pythonesque {
                static main(args) {
                    foo()
                }
                @Newify(Integer) static foo() {
                    assert Integer(43) == 43
                }
            }
        """
    }

    void testNewificationInProperties() {
        evaluate """
            class Rubyesque {
                @Newify static main(args) {
                    assert Integer.new(44).class == Integer
                }
            }
        """

        evaluate """
            class Pythonesque {
                @Newify(Integer) static main(args) {
                    assert Integer(45).class == Integer
                }
            }
        """
    }

    void testNewificationLocalVariables_GROOVY6421() {
        evaluate """
            @Newify foo() {
                def x = Integer.new(42)
                x
            }
            assert foo() == 42
        """
    }

    void testNewificationClosureExpression_GROOVY6434() {
        evaluate """
            @Newify([String])
            String test1() {
                return String("ABC")
            }
            @Newify([String])
            String test2() {
                return { -> String("ABC") }.call()
            }
            assert "ABC"==test1()
            assert "ABC"==test2()
        """
    }

    void testNewificationUsingClassesWithinScript() {
        evaluate """
            import groovy.transform.Immutable
            abstract class Tree {}
            @Immutable class Branch extends Tree { Tree left, right }
            @Immutable class Leaf extends Tree { int val }
            @Newify([Branch, Leaf])
            def t = Branch(Leaf(1), Branch(Branch(Leaf(2), Leaf(3)), Leaf(4)))
            assert t.toString() == 'Branch(Leaf(1), Branch(Branch(Leaf(2), Leaf(3)), Leaf(4)))'
        """
    }

    void testNewifyInnerClassNode_Groovy6438() {
        def test = evaluate '''
            @Newify String test() {
              new Object() { def x() { String.new('ABC') } }.x()
            }
            test()
        '''
        assert test == 'ABC'
    }

    void testNewifyClosureCompileStatic_Groovy7758() {
        assertScript '''
            class A {
                String foo() { 'abc' }
            }

            @groovy.transform.CompileStatic
            @Newify
            String test(A arg) {
                Closure<String> cl = { A it -> it.foo() }
                cl.call(arg)
            }

            assert test(new A()) == 'abc'
        '''
    }

    void testNewifyTransformPreservesSafeMethodCall_Groovy8203() {
        assertScript '''
            @Newify(A)
            class Z {
                def foo() {
                    def a
                    a?.get('b')
                }
                class A {}
            }

            assert !new Z().foo()
        '''
    }

    // GROOVY-8245
    void testDeclarationWhenAutoIsFalse() {
        assertScript '''
            class Foo {
                static int answer = 7
                Foo() {
                    answer = 42
                }
            }
            @Newify(auto=false, value=Foo)
            class Bar {
                static {
                    Foo foo = Foo()
                }
                static void method() {}
            }
            assert Foo.answer == 7
            Bar.method()
            assert Foo.answer == 42
        '''
    }

    // GROOVY-8249
    void testLocalVariableDeclResolvesClass() {
        assertScript '''
            class A {
                final int id
                A(int id) { this.id = id + 10 }
            }
            class Foo {
                static String test() {
                    @Newify(String)
                    String answer = String('bar')
                    answer
                }
                static int test2() {
                    @Newify(A)
                    int answer = A(32).id
                    answer
                } 
            }
            assert Foo.test() == 'bar'
            assert Foo.test2() == 42
        '''
    }
}
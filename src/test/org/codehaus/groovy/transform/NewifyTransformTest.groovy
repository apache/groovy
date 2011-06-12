/*
 * Copyright 2008-2011 the original author or authors.
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
package org.codehaus.groovy.transform

/**
 * @author Paul King
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
}
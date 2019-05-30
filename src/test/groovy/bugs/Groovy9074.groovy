/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package groovy.bugs

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationUnit

import static org.codehaus.groovy.control.Phases.CLASS_GENERATION

@CompileStatic
final class Groovy9074 extends GroovyTestCase {

    void _FIXME_testWildcardCapture() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Main {
                private static Collection<?> c = new ArrayList<String>()
                static main(args) {
                    c.add(new Object())
                }
            }
        '''

        // TODO: This is just a sample message; Java produces this for the equivalent code.
        assert err =~ / The method add\(capture#1-of \?\) in the type Collection<capture#1-of \?> is not applicable for the arguments \(Object\)/
    }

    void _FIXME_testWildcardExtends() {
        def err = shouldFail '''
            import java.awt.Canvas
            abstract class Shape {
              abstract void draw(Canvas c)
            }
            class Circle extends Shape {
              private int x, y, radius
              @Override void draw(Canvas c) {}
            }
            class Rectangle extends Shape {
              private int x, y, width, height
              @Override void draw(Canvas c) {}
            }

            @groovy.transform.CompileStatic
            void addRectangle(List<? extends Shape> shapes) {
              shapes.add(0, new Rectangle()) // TODO: compile-time error!
            }
        '''

        // TODO: This is just a sample message; Java produces this for the equivalent code.
        assert err =~ / The method add(capture#1-of \?) in the type List<capture#1-of \?> is not applicable for the arguments \(Rectangle\)/
    }

    void testWildcardSuper() {
        assertScript '''
            import java.awt.Canvas
            abstract class Shape {
              abstract void draw(Canvas c)
            }
            class Circle extends Shape {
              private int x, y, radius
              @Override void draw(Canvas c) {}
            }
            class Rectangle extends Shape {
              private int x, y, width, height
              @Override void draw(Canvas c) {}
            }

            @groovy.transform.CompileStatic
            void addRectangle(List<? super Shape> shapes) {
              shapes.add(0, new Rectangle())
            }

            List<Shape> list = []
            addRectangle(list)

            assert list.size() == 1
            assert list.get(0) instanceof Rectangle
        '''
    }

    void testWildcardExtends2() {
        new CompilationUnit().with {
            addSource 'Main.groovy', '''
                class Factory {
                  def <T> T make(Class<T> type, ... args) {}
                }

                @groovy.transform.CompileStatic
                void test(Factory fact, Rule rule) {
                  Type bean = fact.make(rule.type)
                }
            '''

            addSource 'Rule.groovy', '''
                class Rule {
                  Class<? extends Type> getType() {
                  }
                }
            '''

            addSource 'Type.groovy', '''
                interface Type {
                }
            '''

            compile CLASS_GENERATION
        }
    }

    void _FIXME_testWildcardSuper2() {
        new CompilationUnit().with {
            addSource 'Main.groovy', '''
                class Factory {
                  def <T> T make(Class<T> type, ... args) {}
                }

                @groovy.transform.CompileStatic
                void test(Factory fact, Rule rule) {
                  Type bean = fact.make(rule.type) // can't assign "? super Type" to "Type"
                }
            '''

            addSource 'Rule.groovy', '''
                class Rule {
                  Class<? super Type> getType() {
                  }
                }
            '''

            addSource 'Type.groovy', '''
                interface Type {
                }
            '''

            def err = shouldFail {
                compile CLASS_GENERATION
            }
            assert err =~ "cannot convert from capture#1-of ? super Type to Type"
        }
    }
}

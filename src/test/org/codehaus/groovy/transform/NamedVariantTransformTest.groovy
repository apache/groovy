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
 * Tests for the {@code @NamedVariant} transformation.
 */
class NamedVariantTransformTest extends GroovyShellTestCase {

    void testNamedParam() {
        assertScript '''
            import groovy.transform.*

            class Animal {
                String type
                String name
            }

            @ToString(includeNames=true, includeFields=true)
            class Color {
                Integer r, g
                private Integer b
                Integer setB(Integer b) { this.b = b }
            }

            @NamedVariant
            String foo(a, @NamedParam String b2, @NamedDelegate Color shade, int c, @NamedParam(required=true) d, @NamedDelegate Animal pet) {
              "$a $b2 $c $d ${pet.type?.toUpperCase()}:$pet.name $shade"
            }

            def result = foo(b2: 'b param', g: 12, b: 42, r: 12, 'foo', 42, d:true, type: 'Dog', name: 'Rover')
            assert result == 'foo b param 42 true DOG:Rover Color(r:12, g:12, b:42)'
        '''
    }

    void testNamedParamWithRename() {
        assertScript '''
            import groovy.transform.*

            @ToString(includeNames=true)
            class Color {
                Integer r, g, b
            }

            @NamedVariant
            String m(@NamedDelegate Color color, @NamedParam('a') int alpha) {
                return [color, alpha].join(' ')
            }

            assert m(r:1, g:2, b:3, a: 0) == 'Color(r:1, g:2, b:3) 0'
        '''
    }

    void testNamedParamConstructor() {
        assertScript """
            import groovy.transform.*

            @ToString(includeNames=true, includeFields=true)
            class Color {
                @NamedVariant
                Color(@NamedParam int r, @NamedParam int g, @NamedParam int b) {
                  this.r = r
                  this.g = g
                  this.b = b
                }
                private int r, g, b
            }

            assert new Color(r: 10, g: 20, b: 30).toString() == 'Color(r:10, g:20, b:30)'
        """
    }

    void testNamedParamConstructorVisibility() {
        assertScript """
            import groovy.transform.*
            import static groovy.transform.options.Visibility.*

            class Color {
                private int r, g, b

                @VisibilityOptions(PUBLIC)
                @NamedVariant
                private Color(@NamedParam int r, @NamedParam int g, @NamedParam int b) {
                  this.r = r
                  this.g = g
                  this.b = b
                }
            }

            def pubCons = Color.constructors
            assert pubCons.size() == 1
            assert pubCons[0].parameterTypes[0] == Map
        """
    }

    void testNamedParamInnerClass() {
        assertScript '''
            import groovy.transform.*

            class Foo {
                int adjust
                @ToString(includeNames = true)
                class Bar {
                    @NamedVariant
                    Bar(@NamedParam int x, @NamedParam int y) {
                        this.x = x + adjust
                        this.y = y + adjust
                    }
                    int x, y
                    @NamedVariant
                    def update(@NamedParam int x, @NamedParam int y) {
                        this.x = x + adjust
                        this.y = y + adjust
                    }
                }
                def makeBar() {
                    new Bar(x: 0, y: 0)
                }
            }

            def b = new Foo(adjust: 1).makeBar()
            assert b.toString() == 'Foo$Bar(x:1, y:1)'
            b.update(10, 10)
            assert b.toString() == 'Foo$Bar(x:11, y:11)'
            b.update(x:15, y:25)
            assert b.toString() == 'Foo$Bar(x:16, y:26)'
        '''
    }

    void testGeneratedMethodsSkipped() {
        assertScript '''
            import groovy.transform.*
            import static org.codehaus.groovy.transform.NamedVariantTransformTest.*

            @NamedVariant
            def baz(@NamedDelegate Storm st, @NamedDelegate Switch sw) { st.front + sw.back }
            assert baz(front: 'Hello', back: 'World') == 'HelloWorld'
        '''
    }

    static class Storm { String front }
    static class Switch { String back }
}

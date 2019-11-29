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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for the {@code @NamedVariant} transformation.
 */
@CompileStatic
final class NamedVariantTransformTest {

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testGeneratedMethodsSkipped() {
        assertScript '''
            import groovy.transform.*
            import static org.codehaus.groovy.transform.NamedVariantTransformTest.*

            @NamedVariant
            def baz(@NamedDelegate Storm storm_, @NamedDelegate Switch switch_) { storm_.front + switch_.back }
            assert baz(front: 'Hello', back: 'World') == 'HelloWorld'
        '''
    }

    @Test // GROOVY-9183
    void testNamedDelegateWithPrimitiveValues() {
        assertScript '''
            import groovy.transform.*

            class Color {
                int r, g, b
            }

            @NamedVariant
            Color makeColor(@NamedDelegate Color color) {
                color
            }

            def color = makeColor(r: 128, g: 128)
            assert color.r == 128
            assert color.g == 128
            assert color.b == 0
        '''
    }

    @Test // GROOVY-9183
    void testNamedDelegateWithPropertyDefaults() {
        assertScript '''
            import groovy.transform.*

            class RowMapper {
                final Settings settings

                @NamedVariant
                RowMapper(Settings settings) {
                    this.settings = settings
                }

                @NamedVariant
                static RowMapper parse(@NamedDelegate Settings settings, Reader reader) {
                    // settings missing the initializer values from properties not passed as named arguments
                    new RowMapper(settings).parseImpl(reader)
                }

                private RowMapper parseImpl(Reader source) {
                    // do work here
                    return this
                }

                @Immutable
                static class Settings {
                    String separator = ','
                    Boolean headers = true
                    Integer headersRow = 0
                    Integer firstDataRow = 1
                }
            }

            def mapper = RowMapper.parse(separator: '\t', new StringReader(''))

            assert mapper.settings.headers == true
            assert mapper.settings.headersRow == 0
            assert mapper.settings.firstDataRow == 1
        '''
    }

    static class Storm { String front }
    static class Switch { String back }
}

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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code @Field} AST transform.
 */
final class FieldTransformTest {

    private final GroovyShell shell = new GroovyShell(new CompilerConfiguration().
        addCompilationCustomizers(new ImportCustomizer().tap { addImports('groovy.transform.Field') })
    )

    @Test
    void testInstanceField() {
        assertScript shell, '''
            @Field List awe = [1, 2, 3]
            def awesum() { awe.sum() }
            assert awesum() == 6
            assert this.awe instanceof List
            assert this.class.getDeclaredField('awe').type.name == 'java.util.List'
        '''
    }

    @Test
    void testStaticFieldFromScript() {
        assertScript shell, '''
            @Field static List awe = [1, 2, 3]
            def awesum() { awe.sum() + this.class.awe.sum() }
            assert awesum() == 12
            assert this.class.awe instanceof List
        '''
    }

    @Test
    void testStaticFieldFromMethod() {
        assertScript shell, '''
            @Field static String exer = 'exercise'
            static exersize() { exer.size() }
            assert exersize() == 8
        '''
    }

    @Test
    void testFieldInitialization() {
        def src = '''
            @Field public pepsi = [1, 2, 3]
        '''
        def gcs = new GroovyCodeSource(src, 'foo', 'bar')
        def cls = shell.parseClass(gcs)

        assert cls.getDeclaredConstructor().newInstance().pepsi.max() == 3
    }

    @Test
    void testStaticFieldInitialization() {
        def src = '''
            @Field public static ad = [1, 2, 3]
            assert ad.min() == 1
        '''
        def gcs = new GroovyCodeSource(src, 'foo', 'bar')
        def cls = shell.parseClass(gcs)
        assert cls.ad.min() == 1
    }

    @Test
    void testFieldTypes() {
        assertScript shell, '''
            @Field int one
            @Field int two = 2
            @Field Integer three = 3
            this.one = 1
            assert this.one + this.two + this.three == 6
        '''
    }

    @Test
    void testNotAllowedInScriptMethods() {
        shouldFail shell, '''
            def method() {
                @Field int one
            }
        '''
    }

    @Test
    void testNotAllowedForClassFields() {
        shouldFail shell, '''
            class Inner {
                @Field int one
            }
        '''
    }

    @Test
    void testNotAllowedForScriptInnerClassFields() {
        shouldFail shell, '''
            class Inner {
                @Field int one
            }
            println Inner.class.name
        '''
    }

    @Test
    void testNotAllowedInClassMethods() {
        // currently two error messages!
        shouldFail shell, '''
            class Inner {
                def bar() {
                    @Field int one
                }
            }
        '''
    }

    @Test
    void testNotAllowedInScriptInnerClassMethods() {
        // currently two error messages!
        shouldFail shell, '''
            class Inner {
                def bar() {
                    @Field int one
                }
            }
            println Inner.class.name
        '''
    }

    @Test
    void testFieldShouldBeAccessibleFromClosure() {
        assertScript shell, '''
            @Field int x
            def closure = { x = 1; x }
            assert closure() == 1
        '''
    }

    @Test // GROOVY-4700
    void testFieldShouldBeAccessibleFromClosure2() {
        assertScript shell, '''
            @Field xxx = 3
            foo = {
                xxx + 1
            }
            assert foo() == 4
        '''
    }

    @Test // GROOVY-5207
    void testFieldShouldBeAccessibleFromClosure3() {
        assertScript shell, '''
            @Field xxx = [:]
            @Field static yyy = [:]
            [1, 2, 3].each {
                xxx[it] = it
                yyy[it] = it
            }
            assert xxx == [(1):1, (2):2, (3):3]
            assert yyy == xxx
        '''
    }

    @Test // GROOVY-9554
    void testFieldShouldBeAccessibleFromClosure4() {
        assertScript shell, '''
            @Field String abc
            binding.variables.clear()
            abc = 'abc'
            assert !binding.hasVariable('abc')
            ['D','E','F'].each {
                abc += it
            }
            assert !binding.hasVariable('abc')
            assert this.@abc == 'abcDEF'
            assert abc == 'abcDEF'
        '''
    }

    @Test
    void testStaticFieldShouldBeAccessibleFromClosure() {
        assertScript shell, '''
            @Field static int x
            x = 10
            def closure = { x * 2 }
            assert closure() == 20
        '''
    }

    @Test
    void testAnnotationsOnFieldShouldBeSet() {
        assertScript shell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.FIELD)
            public @interface Awesome {
            }

            @Awesome @Field def test

            class Doh {
                @Awesome String test
            }

            assert Doh.getDeclaredField('test').getAnnotations().size() == 1
            assert this.class.getDeclaredField('test').getAnnotations().size() == 1
        '''
    }

    @Test // GROOVY-6112
    void testGroovyTransformsShouldTransferToFields() {
        assertScript shell, '''
            @Lazy @Field foo = 'foo'
            @Field @Lazy bar = 'bar'
            @Field baz = 'baz'

            assert this.@$foo == null
            assert this.@$bar == null
            assert this.@baz == 'baz'
            assert foo + bar + baz == 'foobarbaz'
        '''
    }

    @Test // GROOVY-8112
    void testAnonymousInnerClassReferencesToField() {
        assertScript shell, '''
            @Field
            StringBuilder logger = new StringBuilder()
            logger.append('a')
            ['b'].each {
                logger.append(it)
                new Object() {
                    String toString() {
                        logger.append('c')
                        ['d'].each { logger.append(it) }
                    }
                }.toString()
            }
            Closure c = { logger.append('e') }
            c()
            // control: worked previously, make sure we didn't break
            def method() {
                logger.append('f')
                ['g'].each {
                    logger.append(it)
                    new Object() {
                        String toString() {
                            logger.append('h')
                        }
                    }.toString()
                }
            }
            method()
            assert logger.toString() == 'abcdefgh'
        '''
    }

    @Test // GROOVY-11567
    void testAnonymousInnerClassReferencesToField2() {
        assertScript shell, '''
            @Field String foo = 'bar'
            assert({ ->
                new Object() {
                    String toString() { foo + 'baz' }
                }
            }.call().toString() == 'barbaz')
        '''
    }

    @Test // GROOVY-8430
    void testFieldTransformWithFinalField() {
        assertScript shell, '''
            @Field final foo = 14
            @Field final bar = foo * 2
            @Field baz = foo + bar

            assert foo + bar == 42
            assert baz == 42
            def setters = getClass().methods.findAll{ it.name.startsWith('set') }.name
            assert setters.intersect(['setBar', 'setFoo', 'setBaz']) == ['setBaz']
        '''
    }

    @Test // GROOVY-8430 in conjunction with @Option
    void testFieldTransformWithFinalFieldAndOption() {
        shouldFail '''
            import groovy.cli.OptionField
            @OptionField final String first
        '''
    }

    @Test // GROOVY-10516
    void testFieldOnFullyQualifiedType() {
        assertScript shell, '''
            @Field java.util.List list
        '''
    }
}

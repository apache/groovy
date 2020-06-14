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

import gls.CompilableTestSupport

/**
 * Tests for the {@code @Field} transformation
 */
class FieldTransformTest extends CompilableTestSupport {

    void testInstanceField() {
        assertScript """
            @groovy.transform.Field List awe = [1, 2, 3]
            def awesum() { awe.sum() }
            assert awesum() == 6
            assert this.awe instanceof List
            assert this.class.getDeclaredField('awe').type.name == 'java.util.List'
        """
    }

    void testStaticFieldFromScript() {
        assertScript """
            import groovy.transform.*
            @Field static List awe = [1, 2, 3]
            def awesum() { awe.sum() + this.class.awe.sum() }
            assert awesum() == 12
            assert this.class.awe instanceof List
        """
    }

    void testStaticFieldFromMethod() {
        assertScript """
            import groovy.transform.*
            @Field static String exer = 'exercise'
            static exersize() { exer.size() }
            assert exersize() == 8
        """
    }

    void testFieldInitialization() {
        assertScript """
            def scriptText = '''
                import groovy.transform.*
                @Field public pepsi = [1, 2, 3]
            '''
            def gcs = new GroovyCodeSource(scriptText, 'foo', 'bar')
            def klass = new GroovyShell().parseClass(gcs)
            assert klass.newInstance().pepsi.max() == 3
        """
    }

    void testStaticFieldInitialization() {
        assertScript """
            def scriptText = '''
                import groovy.transform.*
                @Field public static ad = [1, 2, 3]
                assert ad.min() == 1
            '''
            def gcs = new GroovyCodeSource(scriptText, 'foo', 'bar')
            def klass = new GroovyShell().parseClass(gcs)
            assert klass.ad.min() == 1
        """
    }

    void testFieldTypes() {
        assertScript """
            import groovy.transform.*
            @Field int one
            @Field int two = 2
            @Field Integer three = 3
            this.one = 1
            assert this.one + this.two + this.three == 6
        """
    }

    void testNotAllowedInScriptMethods() {
        shouldNotCompile """
            import groovy.transform.*
            def method() {
                @Field int one
            }
        """
    }

    void testNotAllowedForClassFields() {
        shouldNotCompile """
            import groovy.transform.*
            class Inner {
                @Field int one
            }
        """
    }

    void testNotAllowedForScriptInnerClassFields() {
        shouldNotCompile """
            import groovy.transform.*
            class Inner {
                @Field int one
            }
            println Inner.class.name
        """
    }

    void testNotAllowedInClassMethods() {
        // currently two error messages!
        shouldNotCompile """
            import groovy.transform.*
            class Inner {
                def bar() {
                    @Field int one
                }
            }
        """
    }

    void testNotAllowedInScriptInnerClassMethods() {
        // currently two error messages!
        shouldNotCompile """
            import groovy.transform.*
            class Inner {
                def bar() {
                    @Field int one
                }
            }
            println Inner.class.name
        """
    }

    void testFieldShouldBeAccessibleFromClosure() {
        assertScript """
            import groovy.transform.Field
            @Field int x
            def closure = { x = 1; x }
            assert closure() == 1
        """
    }

    void testFieldShouldBeAccessibleFromClosureWithoutAssignment() {
        // GROOVY-4700
        assertScript """import groovy.transform.Field
            @Field xxx = 3
            foo = {
                xxx + 1
            }
            assert foo() == 4
        """
    }

    void testFieldShouldBeAccessibleFromClosureForExternalClosures() {
        // GROOVY-5207
        assertScript """
            import groovy.transform.Field
            @Field xxx = [:]
            @Field static yyy = [:]
            [1, 2, 3].each {
                xxx[it] = it
                yyy[it] = it
            }
            assert xxx == [(1):1, (2):2, (3):3]
            assert yyy == xxx
        """
    }

    void testStaticFieldShouldBeAccessibleFromClosure() {
        assertScript """
            import groovy.transform.Field
            @Field static int x
            x = 10
            def closure = { x * 2 }
            assert closure() == 20
        """
    }

    void testAnnotationsOnFieldShouldBeSet() {
        assertScript """
            import groovy.transform.Field
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

        """
    }

    void testGroovyTransformsShouldTransferToFields() {
        // GROOVY-6112
        assertScript '''
            import groovy.transform.Field

            @Lazy @Field foo = 'foo'
            @Field @Lazy bar = 'bar'
            @Field baz = 'baz'

            assert this.@$foo == null
            assert this.@$bar == null
            assert this.@baz == 'baz'
            assert foo + bar + baz == 'foobarbaz'
        '''
    }

    void testAnonymousInnerClassReferencesToField() {
        // GROOVY-8112
        assertScript '''
            @groovy.transform.Field
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

    void testClosureReferencesToField() {
        // GROOVY-9554
        assertScript '''
            @groovy.transform.Field String abc
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

    void testFieldTransformWithFinalField() {
        // GROOVY-8430
        assertScript '''
            import groovy.transform.Field

            @Field final foo = 14
            @Field final bar = foo * 2
            @Field baz = foo + bar

            assert foo + bar == 42
            assert baz == 42
            def setters = getClass().methods.findAll{ it.name.startsWith('set') }.name
            assert setters.intersect(['setBar', 'setFoo', 'setBaz']) == ['setBaz']
        '''
    }

    void testFieldTransformWithFinalFieldAndOption() {
        // GROOVY-8430 in conjunction with @Option
        shouldNotCompile '''
            import groovy.cli.OptionField
            @OptionField final String first
        '''
    }
}

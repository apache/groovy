/*
 * Copyright 2008-2010 the original author or authors.
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

import gls.CompilableTestSupport

/**
 * @author Paul King
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

}
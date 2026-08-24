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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unit tests for static type checking : type checking mode.
 */
class TypeCheckingModeTest extends StaticTypeCheckingTestCase {

    @Test
    void testShouldThrowErrorBecauseTypeCheckingIsOn() {
        shouldFailWithMessages '''
            int foo() { 'foo' }
            1
        ''', 'Cannot return value of type java.lang.String for method returning int'
    }

    @Test
    void testShouldNotThrowErrorBecauseTypeCheckingIsOff() {
        assertScript '''
            @groovy.transform.TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
            int foo() { 'foo' }
            1
        '''
    }

    @Test
    void testShouldNotThrowErrorBecauseTypeCheckingIsOffUsingImports() {
        assertScript '''
            import groovy.transform.TypeChecked
            import static groovy.transform.TypeCheckingMode.*
            @TypeChecked(SKIP)
            int foo() { 'foo' }
            1
        '''
    }

    @Test
    void testShouldThrowErrorBecauseTypeCheckingIsOnIntoClass() {
        shouldFailWithMessages '''
            class A {
                int foo() { 'foo' }
            }
            1
        ''', 'Cannot return value of type java.lang.String for method returning int'
    }

    @Test
    void testShouldNotThrowErrorBecauseTypeCheckingIsOffIntoClass() {
        assertScript '''
            @groovy.transform.TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
            class A {
                int foo() { 'foo' }
            }
            1
        '''
    }

    @Test
    void testShouldNotThrowErrorBecauseTypeCheckingIsOff2IntoClass() {
        assertScript '''
            class A {
                @groovy.transform.TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
                int foo() { 'foo' }
            }
            1
        '''
    }

    @Test
    void testShouldNotThrowErrorBecauseTypeCheckingIsOff3IntoClass() {
        assertScript '''
            @groovy.transform.TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
            class A {
                int foo() { 'foo' }
            }
            try {
                new A().foo()
            } catch (e) {
                // silent
            }
        '''
    }

    // GROOVY-5884
    @Test
    void testShouldSkipTypeCheckingInConstructor() {
        assertScript '''
            class GenericsApocalypse {
                @groovy.transform.TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
                GenericsApocalypse() {
                    int x = 'string'
                }
            }
            try {
                new GenericsApocalypse()
            } catch (org.codehaus.groovy.runtime.typehandling.GroovyCastException e) {
                // catch a runtime exception instead of a compile-time one
            }
        '''
    }

    @Test
    void testSkipAndAnonymousInnerClass() {
        new GroovyShell().evaluate '''import groovy.transform.TypeChecked
            public interface HibernateCallback<T> {
                T doInHibernate()
            }

            @TypeChecked
            class Enclosing {
                @TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
                def shouldBeSkipped(Closure callable) {
                    new HibernateCallback() {
                        @Override
                        def doInHibernate() {
                            callable(1+new Date()) // should pass because we're in a skipped section
                        }}
                }
            }

            new Enclosing().shouldBeSkipped {
                println 'This is ok'
            }
        '''
    }

    // GROOVY-12292
    @Test
    void testTypeCheckedMethodInTypeCheckedSkipClass() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                import groovy.transform.TypeChecked
                import static groovy.transform.TypeCheckingMode.SKIP

                @TypeChecked(SKIP)
                class C {
                    @TypeChecked
                    def m() {
                        "".toStrings()
                    }
                }
                new C()
            '''
        }
        assert err.message.contains('Cannot find matching method java.lang.String#toStrings()')
    }

    // GROOVY-12292: @CompileStatic(SKIP) opts out of static compilation only;
    // it does not exempt a method from an enclosing class's type checking
    @Test
    void testCompileStaticSkipMethodInTypeCheckedClassIsStillTypeChecked() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                import groovy.transform.CompileStatic
                import groovy.transform.TypeChecked
                import static groovy.transform.TypeCheckingMode.SKIP

                @TypeChecked
                class C {
                    @CompileStatic(SKIP)
                    def m() {
                        "".toStrings()
                    }
                }
                new C()
            '''
        }
        assert err.message.contains('Cannot find matching method java.lang.String#toStrings()')
    }
}

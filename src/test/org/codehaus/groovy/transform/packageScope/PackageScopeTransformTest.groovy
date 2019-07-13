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
package org.codehaus.groovy.transform.packageScope

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class PackageScopeTransformTest {

    @Test
    void testPackageScope1() {
        assertScript '''
            import groovy.transform.PackageScope
            import static java.lang.reflect.Modifier.*

            class A {
                String x
            }
            @PackageScope class B {
                String x
            }

            assert isPublic(A.modifiers)
            assert !isPublic(B.modifiers) && !isPrivate(B.modifiers) && !isProtected(B.modifiers)

            assert isPublic(A.getDeclaredConstructor().modifiers)
            assert isPublic(B.getDeclaredConstructor().modifiers)

            assert isPrivate(A.getDeclaredField('x').modifiers)
            assert isPrivate(A.getDeclaredField('x').modifiers)

            assert isPublic(A.getDeclaredMethod('getX').modifiers)
            assert isPublic(B.getDeclaredMethod('getX').modifiers)

            assert isPublic(A.getDeclaredMethod('setX', String).modifiers)
            assert isPublic(B.getDeclaredMethod('setX', String).modifiers)
        '''
    }

    @Test
    void testPackageScope2() {
        assertScript '''
            import groovy.transform.PackageScope
            import static java.lang.reflect.Modifier.*
            import static groovy.test.GroovyAssert.shouldFail

            class C {
                @PackageScope C() {}
                @PackageScope String x
                @PackageScope def method() {}
            }

            boolean isPackagePrivate(modifiers) {
                !isPublic(modifiers) && !isPrivate(modifiers) && !isProtected(modifiers)
            }

            assert isPublic(C.modifiers)

            assert isPackagePrivate(C.getDeclaredConstructor().modifiers)

            assert isPackagePrivate(C.getDeclaredField('x').modifiers)

            assert isPackagePrivate(C.getDeclaredMethod('method').modifiers)

            shouldFail(NoSuchMethodException) {
                C.getDeclaredMethod('getX')
                C.getDeclaredMethod('setX', String)
            }
        '''
    }

    @Test
    void testPackageScope3() {
        assertScript '''
            import groovy.transform.PackageScope
            import static java.lang.reflect.Modifier.*
            import static groovy.test.GroovyAssert.shouldFail
            import static groovy.transform.PackageScopeTarget.*

            @PackageScope(FIELDS) class C {
                C() {}
                String x
                def method() {}
            }

            boolean isPackagePrivate(modifiers) {
                !isPublic(modifiers) && !isPrivate(modifiers) && !isProtected(modifiers)
            }

            assert isPublic(C.modifiers)

            assert isPublic(C.getDeclaredConstructor().modifiers)

            assert isPackagePrivate(C.getDeclaredField('x').modifiers)

            assert isPublic(C.getDeclaredMethod('method').modifiers)

            shouldFail(NoSuchMethodException) {
                C.getDeclaredMethod('getX')
                C.getDeclaredMethod('setX', String)
            }
        '''
    }

    @Test
    void testPackageScope4() {
        assertScript '''
            import groovy.transform.PackageScope
            import static java.lang.reflect.Modifier.*
            import static groovy.test.GroovyAssert.shouldFail
            import static groovy.transform.PackageScopeTarget.*

            @PackageScope(METHODS) class C {
                C() {}
                String x
                def method() {}
            }

            boolean isPackagePrivate(modifiers) {
                !isPublic(modifiers) && !isPrivate(modifiers) && !isProtected(modifiers)
            }

            assert isPublic(C.modifiers)

            assert isPublic(C.getDeclaredConstructor().modifiers)

            assert isPrivate(C.getDeclaredField('x').modifiers)
            assert isPublic(C.getDeclaredMethod('getX').modifiers)
            assert isPublic(C.getDeclaredMethod('setX', String).modifiers)

            assert isPackagePrivate(C.getDeclaredMethod('method').modifiers)
        '''
    }

    @Test
    void testPackageScope5() {
        assertScript '''
            import groovy.transform.PackageScope
            import static java.lang.reflect.Modifier.*
            import static groovy.test.GroovyAssert.shouldFail
            import static groovy.transform.PackageScopeTarget.*

            @PackageScope([CLASS, CONSTRUCTORS, METHODS]) class C {
                C() {}
                String x
                def method() {}
                static class D {}
            }

            boolean isPackagePrivate(modifiers) {
                !isPublic(modifiers) && !isPrivate(modifiers) && !isProtected(modifiers)
            }

            assert isPackagePrivate(C.modifiers)

            assert isPackagePrivate(C.getDeclaredConstructor().modifiers)

            assert isPrivate(C.getDeclaredField('x').modifiers)
            assert isPublic(C.getDeclaredMethod('getX').modifiers)
            assert isPublic(C.getDeclaredMethod('setX', String).modifiers)

            assert isPackagePrivate(C.getDeclaredMethod('method').modifiers)

            assert isPublic(C.getDeclaredClasses()[0].modifiers) // not transitive
        '''
    }

    @Test // GROOVY-9043
    void testStaticFieldAccessFromInnerClassCS() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.PackageScope

            @CompileStatic
            class Test {
                @PackageScope static final String S = 'S'
                protected static final String T = 'T'
                private static final String U = 'U'
                static class Inner {
                    String method() {
                        S + T + U
                    }
                }
            }

            assert new Test.Inner().method() == 'STU'
        '''
    }
}

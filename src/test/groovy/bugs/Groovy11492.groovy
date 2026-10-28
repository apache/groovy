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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy11492 {

    @Test
    void testArrayField() {
        assertScript '''
            import groovy.transform.*
            import static java.lang.reflect.Modifier.*

            boolean isPackagePrivate(int mods) {
                !isPublic(mods) && !isPrivate(mods) && !isProtected(mods)
            }

            class C {
                public Object[] array0 = {}
                protected Object[] array1 = {null}
                @PackageScope Object[][] array2 = {{},{""}}
                private final java.lang.Object[][][] array3 = {{{}}}
            }

            assert isPublic        (C.getDeclaredField('array0').getModifiers())
            assert isProtected     (C.getDeclaredField('array1').getModifiers())
            assert isPackagePrivate(C.getDeclaredField('array2').getModifiers())
            assert isPrivate       (C.getDeclaredField('array3').getModifiers())
            assert isFinal         (C.getDeclaredField('array3').getModifiers())

            new C().with {
                assert array0.length == 0
                assert array1.length == 1
                assert array2.length == 2
                assert array3.length == 1

                assert array1[0] == null
                assert array2[0].length == 0
                assert array2[1].length == 1
                assert array2[1][0] != null
                assert array3[0].length == 1
                assert array3[0][0].length == 0
            }
        '''
    }

    @Test
    void testArrayProperty() {
        assertScript '''
            class C {
                String[] strings = {'foo','bar','baz'}
            }

            new C().with {
                assert getStrings().length == 3
                assert strings.length == 3
                assert strings[1] == 'bar'
            }
        '''
    }

    @Test
    void testArrayParameter() {
        assertScript '''
            class C {
                def m(int[] ints = {0,1,2}) {
                    ints
                }
            }

            def x = new C().m()
            assert x instanceof int[] && x.length == 3
            assert x[0] == 0 && x[1] == 1 && x[2] == 2
        '''
    }

    @Test
    void testArrayVariables() {
        assertScript '''
            int[] a = {1}, b = {2}
            Object[] c = { {} } // closure

            assert a instanceof int[]
            assert b instanceof int[]
            assert c instanceof Object[]

            assert a.length == 1 && a[0] == 1
            assert b.length == 1 && b[0] == 2
            assert c.length == 1 && c[0] instanceof Closure
        '''
    }

    @Test
    void testArrayPromotion() {
        assertScript '''
            int[] a = 1
            String[] b = 2
            Closure[] c = { x -> x }

            assert a.length == 1 && a[0] == 1
            assert b.length == 1 && b[0] == '2'
            assert c.length == 1 && c[0].call(3) == 3
        '''
    }
}

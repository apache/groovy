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
package org.codehaus.groovy.vmplugin.v8

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class PluginDefaultGroovyMethodsTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        ast(groovy.transform.CompileStatic)
    }

    // GROOVY-7611
    @Test
    void testOptionalAsBoolean() {
        assertScript shell, '''
            boolean m() {
                assert Optional.of('foo')
                assert !Optional.empty()
                assert !Optional.ofNullable(null)

                def x = Optional.empty() ? 1 : -1
                assert x == -1

                x = Optional.ofNullable(null) ? 1 : -1
                assert x == -1

                Optional.empty()
            }
            assert !m()
        '''
    }

    @Test
    void testStreamToList() {
        assertScript shell, '''
            def list = [1, 2, 3]
            assert list == list.stream().toList()
        '''
    }

    @Test
    void testStreamToSet() {
        assertScript shell, '''
            def set = [1, 2, 3] as Set
            assert set.sort() == set.stream().toSet().sort()
        '''
    }

    @Test
    void testBaseStreamToList() {
        assertScript shell, '''
            def list = [1, 2, 3]
            assert list == Arrays.stream(list as int[]).toList()
        '''
    }

    @Test
    void testBaseStreamToSet() {
        assertScript shell, '''
            def set = [1, 2, 3] as Set
            assert set.sort() == Arrays.stream(set as int[]).toSet().sort()
        '''
    }

    @Test
    void testObjectArrayToStream() {
        assertScript shell, '''
            def array = ["Hello", "World"] as Object[]
            assert array == array.stream().toArray()
        '''

        assertScript shell, '''
            def array = ["Hello", "World"] as String[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testIntArrayToStream() {
        assertScript shell, '''
            def array = [1, 2] as int[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testLongArrayToStream() {
        assertScript shell, '''
            def array = [1, 2] as long[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testDoubleArrayToStream() {
        assertScript shell, '''
            def array = [1, 2] as double[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testCharArrayToStream() {
        assertScript shell, '''
            def array = [65, 66] as char[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testByteArrayToStream() {
        assertScript shell, '''
            def array = [65, 66] as byte[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testShortArrayToStream() {
        assertScript shell, '''
            def array = [65, 66] as short[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testBooleanArrayToStream() {
        assertScript shell, '''
            def array = [true, false] as boolean[]
            assert array == array.stream().toArray()
        '''
    }

    @Test
    void testFloatArrayToStream() {
        assertScript shell, '''
            def array = [65, 66] as float[]
            assert array == array.stream().toArray()
        '''
    }
}

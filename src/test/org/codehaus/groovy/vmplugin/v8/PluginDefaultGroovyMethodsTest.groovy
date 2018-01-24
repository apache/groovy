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

import groovy.transform.stc.StaticTypeCheckingTestCase

class PluginDefaultGroovyMethodsTest extends StaticTypeCheckingTestCase {

    // GROOVY-7611
    void testOptionalAsBoolean() {
        assertScript '''
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

    void testStreamToList() {
        assertScript '''
            def list = [1, 2, 3]
            assert list == list.stream().toList() 
        '''
    }

    void testStreamToSet() {
        assertScript '''
            def set = [1, 2, 3] as Set
            assert set.sort() == set.stream().toSet().sort()
        '''
    }

    void testBaseStreamToList() {
        assertScript '''
            def list = [1, 2, 3]
            assert list == Arrays.stream(list as int[]).toList()
        '''
    }

    void testBaseStreamToSet() {
        assertScript '''
            def set = [1, 2, 3] as Set
            assert set.sort() == Arrays.stream(set as int[]).toSet().sort()
        '''
    }

    void testObjectArrayToStream() {
        assertScript '''
            def array = ["Hello", "World"] as Object[]
            assert array == array.stream().toArray()
        '''

        assertScript '''
            def array = ["Hello", "World"] as String[]
            assert array == array.stream().toArray()
        '''
    }

    void testIntArrayToStream() {
        assertScript '''
            def array = [1, 2] as int[]
            assert array == array.stream().toArray()
        '''
    }

    void testLongArrayToStream() {
        assertScript '''
            def array = [1, 2] as long[]
            assert array == array.stream().toArray()
        '''
    }

    void testDoubleArrayToStream() {
        assertScript '''
            def array = [1, 2] as double[]
            assert array == array.stream().toArray()
        '''
    }

    void testCharArrayToStream() {
        assertScript '''
            def array = [65, 66] as char[]
            assert array == array.stream().toArray()
        '''
    }

    void testByteArrayToStream() {
        assertScript '''
            def array = [65, 66] as byte[]
            assert array == array.stream().toArray()
        '''
    }

    void testShortArrayToStream() {
        assertScript '''
            def array = [65, 66] as short[]
            assert array == array.stream().toArray()
        '''
    }

    void testBooleanArrayToStream() {
        assertScript '''
            def array = [true, false] as boolean[]
            assert array == array.stream().toArray()
        '''
    }

    void testFloatArrayToStream() {
        assertScript '''
            def array = [65, 66] as float[]
            assert array == array.stream().toArray()
        '''
    }
}

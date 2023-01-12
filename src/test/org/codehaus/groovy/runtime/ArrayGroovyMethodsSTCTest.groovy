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
package org.codehaus.groovy.runtime

import groovy.transform.stc.StaticTypeCheckingTestCase

/**
 * STC Tests for ArrayGroovyMethods
 */
class ArrayGroovyMethodsSTCTest extends StaticTypeCheckingTestCase {

    void testAnyForBooleanArray() {
        assertScript '''
        boolean[] array = [true]
        assert array.any{ it.booleanValue() }
        '''
    }

    void testAnyForByteArray() {
        assertScript '''
        byte[] array = [0, 1, 2]
        assert array.any{ 1 == it.byteValue() }
        '''
    }

    void testAnyForCharArray() {
        assertScript '''
        char[] array = 'abc'.chars
        assert array.any{ 'c' == it.charValue() }
        '''
    }

    void testAnyForShortArray() {
        assertScript '''
        short[] array = [0, 1, 2]
        assert array.any{ 2 == it.shortValue() }
        '''
    }

    void testAnyForIntArray() {
        assertScript '''
        int[] array = [0, 1, 2]
        assert array.any{ 1 == it.intValue() }
        '''
    }

    void testAnyForLongArray() {
        assertScript '''
        long[] array = [0L, 1L, 2L]
        assert array.any{ 0L == it.longValue() }
        '''
    }

    void testAnyForFloatArray() {
        assertScript '''
        float[] array = [0.0f, 1.0f, 2.0f]
        assert array.any{ 0.0f == it.floatValue() }
        '''
    }

    void testAnyForDoubleArray() {
        assertScript '''
        double[] array = [0.0d, 1.0d, 2.0d]
        assert array.any{ 0.0d == it.doubleValue() }
        '''
    }

}

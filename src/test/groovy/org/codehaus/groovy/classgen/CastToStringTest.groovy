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
package org.codehaus.groovy.classgen

import groovy.test.GroovyShellTestCase

class CastToStringTest extends GroovyShellTestCase {
    void testNormalAndPrimitiveArrayHandling_Groovy7853() {
        assertScript '''
            byte[] bytes = "hello".bytes
            char[] chars = "hello".chars
            boolean[] flags = [true, false]
            long[] nums = [34L, 45L]
            String[] pets = ['cat', 'dog']

            String convert(byte[] data) { data }
            String convert(char[] data) { data }
            String convert(boolean[] data) { data }
            String convert(long[] data) { data }
            String convert(String[] data) { data }

            assert bytes.toString() == convert(bytes)
            assert chars.toString() == convert(chars)
            assert flags.toString() == convert(flags)
            assert nums.toString() == convert(nums)
            assert pets.toString() == convert(pets)
        '''
    }
}
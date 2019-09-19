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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy7291Bug extends GroovyTestCase {

    void testPrimitiveDouble() {
        assertScript '''
            double a
            def b = {
               assert a.class == Double
               a = a + 1
            }
            b()
            assert a == 1.0d
        '''
    }

    void testDouble() {
        shouldFail(NullPointerException,
        '''
            Double a;
            def b = {
               a = a + 1;
            }
            b()
        ''')
    }

    void testPrimitiveDeclarationHasDefaultValueInClosure() {
        assertScript '''
            boolean z
            byte b
            char c
            short s
            int i
            long j
            float f
            double d
            def cl = {
                assert z == false && z.class == Boolean
                assert b == 0 && b.class == Byte
                assert c == '\u0000' && c.class == Character
                assert s == 0 && s.class == Short
                assert i == 0 && i.class == Integer
                assert j == 0L && j.class == Long
                assert f == 0.0f && f.class == Float
                assert d == 0.0d && d.class == Double
            }
            cl()
        '''
    }

    void testWrapperDeclarationIsNullInClosure() {
        assertScript '''
            Boolean z
            Byte b
            Character c
            Short s
            Integer i
            Long j
            Float f
            Double d
            def cl = {
                assert z == null
                assert b == null
                assert c == null
                assert s == null
                assert i == null
                assert j == null
                assert f == null
                assert d == null
            }
            cl()
        '''
    }

}

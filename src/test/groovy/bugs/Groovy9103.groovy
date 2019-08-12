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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

// TODO: add JVM option `--illegal-access=deny` when all warnings fixed
final class Groovy9103 {

    @Test
    void testProperties() {
        String str = ''
        assert str.properties
    }

    @Test
    void testBigIntegerMultiply() {
        assert 2G * 1
    }

    @Test
    void testClone() {
        assertScript '''
            def broadcastSeq(Object value) {
                value.clone()
            }

            assert broadcastSeq(new Tuple1('abc'))
        '''
    }

    @Test
    void testClone2() {
        assertScript '''
            class Value {
                @Override
                public Value clone() {
                    return new Value()
                }
            }
            def broadcastSeq(Object value) {
                value.clone()
            }

            assert broadcastSeq(new Value())
        '''
    }

    @Test
    void testClone3() {
        Object obj = new Tuple1('abc')
        assert obj.clone()
    }
}

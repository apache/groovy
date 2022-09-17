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
import static groovy.test.GroovyAssert.shouldFail

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
        assert obj.clone().getClass() === Tuple1.class
    }

    @Test
    void testClone4() {
        assertScript '''
            int[] nums = [1, 2, 3]
            int[] cloned = nums.clone()
            assert nums == cloned
        '''
    }

    @Test // GROOVY-10747
    void testClone5() {
        ['Object', 'Dolly'].each { typeName ->
            assertScript """
                class Dolly implements Cloneable {
                    String name

                    @Override
                    public ${typeName} clone() {
                        return super.clone()
                    }
                }

                def dolly = new Dolly(name: "The Sheep")
                def cloned = dolly.clone()
                assert cloned instanceof Dolly
            """
        }
    }

    @Test // GROOVY-10747
    void testClone6() {
        shouldFail(CloneNotSupportedException, '''
            class Dolly {
                String name
            }

            def dolly = new Dolly(name: "The Sheep")
            dolly.clone()
        ''')
    }

    @Test
    void testClone7() {
        ['Object', 'Dolly'].each { typeName ->
            assertScript """
                import org.codehaus.groovy.runtime.InvokerHelper
                class Dolly implements Cloneable {
                    String name

                    @Override
                    public ${typeName} clone() {
                        return super.clone()
                    }
                }

                def dolly = new Dolly(name: "The Sheep")
                def cloned = InvokerHelper.invokeMethod(dolly, 'clone', [] as Object[])
                assert cloned instanceof Dolly
            """
        }
    }

    @Test
    void testClone8() {
        shouldFail(CloneNotSupportedException, '''
            import org.codehaus.groovy.runtime.InvokerHelper
            class Dolly {
                String name
            }

            def dolly = new Dolly(name: "The Sheep")
            InvokerHelper.invokeMethod(dolly, 'clone', [] as Object[])
        ''')
    }
}

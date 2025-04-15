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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy11272 {
    @Test
    void testSerialVersionUID_1() {
        assertScript '''
            def c = {->}
            def serialVersionUIDField = c.class.getDeclaredField('serialVersionUID')
            assert null != serialVersionUIDField
        '''
    }

    @Test
    void testSerialVersionUID_2() {
        assertScript '''
            class C {
                static test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        def c = {p -> "Hello, $p"}
                        it.writeObject(c)
                    }
                    out.toByteArray()
                }
            }
            def serializedClosureBytes = C.test()
            new ByteArrayInputStream(serializedClosureBytes).withObjectInputStream(this.class.classLoader) {
                def c = it.readObject()
                assert c.call('Daniel') == 'Hello, Daniel'
            }
        '''
    }

    @Test
    void testSerialVersionUID_3() {
        assertScript '''
            class C {
                static test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        def greeting = "Hello"
                        def c = {p -> "$greeting, $p"}
                        it.writeObject(c)
                    }
                    out.toByteArray()
                }
            }
            def serializedClosureBytes = C.test()
            new ByteArrayInputStream(serializedClosureBytes).withObjectInputStream(this.class.classLoader) {
                def c = it.readObject()
                assert c.call('Daniel') == 'Hello, Daniel'
            }
        '''
    }

    @Test
    void testSerialVersionUID_1_CS() {
        assertScript '''
            @groovy.transform.CompileStatic
            def test() {
                def c = {->}
                def serialVersionUIDField = c.class.getDeclaredField('serialVersionUID')
                assert null != serialVersionUIDField
            }
            test()
        '''
    }

    @Test
    void testSerialVersionUID_2_CS() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        def c = {p -> "Hello, $p"}
                        it.writeObject(c)
                    }
                    out.toByteArray()
                }
            }
            def serializedClosureBytes = C.test()
            new ByteArrayInputStream(serializedClosureBytes).withObjectInputStream(this.class.classLoader) {
                def c = it.readObject()
                assert c.call('Daniel') == 'Hello, Daniel'
            }
        '''
    }

    @Test
    void testSerialVersionUID_3_CS() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static test() {
                    def out = new ByteArrayOutputStream()
                    out.withObjectOutputStream {
                        def greeting = "Hello"
                        def c = {p -> "$greeting, $p"}
                        it.writeObject(c)
                    }
                    out.toByteArray()
                }
            }
            def serializedClosureBytes = C.test()
            new ByteArrayInputStream(serializedClosureBytes).withObjectInputStream(this.class.classLoader) {
                def c = it.readObject()
                assert c.call('Daniel') == 'Hello, Daniel'
            }
        '''
    }
}

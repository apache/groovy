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

final class Groovy11986 {

    // get(Serializable) is the GORM entity-by-ID lookup shape; it must not be
    // registered as the genericGetMethod, otherwise dynamic property access on
    // an instance hijacks propertyMissing(String).
    @Test
    void testInheritedGetSerializableDoesNotShadowPropertyMissing() {
        assertScript '''
            trait T {
                Object get(Serializable id) { "by-id:$id".toString() }
                Object propertyMissing(String name) { "missing:$name".toString() }
            }
            class C implements T {
                String title
            }

            def c = new C(title: 'hello')
            assert c.title == 'hello'        // bean property still wins
            assert c.foo == 'missing:foo'    // dynamic access falls through to propertyMissing
            assert c.get('foo') == 'by-id:foo' // direct call still hits get(Serializable)
        '''
    }

    @Test
    void testInheritedGetObjectDoesNotShadowPropertyMissing() {
        assertScript '''
            class Base {
                Object get(Object key) { "by-key:$key".toString() }
            }
            class C extends Base {
                Object propertyMissing(String name) { "missing:$name".toString() }
            }

            def c = new C()
            assert c.foo == 'missing:foo'
            assert c.get('foo') == 'by-key:foo'
        '''
    }

    // Sanity: a real get(String) is still picked up as the generic getter.
    @Test
    void testGetStringStillRegistersAsGenericGetter() {
        assertScript '''
            class C {
                def get(String name) { "value:$name".toString() }
            }

            def c = new C()
            assert c.foo == 'value:foo'
        '''
    }

    // Sanity: get(String) on a class that also inherits get(Serializable) wins
    // as the generic getter (closer parameter match).
    @Test
    void testGetStringWinsOverInheritedGetSerializable() {
        assertScript '''
            trait T {
                Object get(Serializable id) { "by-id:$id".toString() }
            }
            class C implements T {
                Object get(String name) { "by-name:$name".toString() }
            }

            def c = new C()
            assert c.foo == 'by-name:foo'
        '''
    }
}

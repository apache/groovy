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

final class Groovy8283 {
    @Test
    void testFieldPropertyShadowing() {
        assertScript '''
            class A {}
            class B {}
            class C {
                protected A foo = new A()
                A getFoo() { return foo }
            }
            class D extends C {
                protected B foo = new B() // hides A#foo; should hide A#getFoo() in subclasses
            }
            class E extends D {
                void test() {
                    assert foo.class == B
                    assert this.foo.class == B
                    assert this.@foo.class == B
                    assert this.getFoo().getClass() == A

                    def that = new E()
                    assert that.foo.class == B
                    assert that.@foo.class == B
                    assert that.getFoo().getClass() == A
                }
            }

            new E().test()
            assert new E().foo.class == A // not the field from this perspective
        '''
    }
}

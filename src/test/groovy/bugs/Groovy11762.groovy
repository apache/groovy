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

final class Groovy11762 {

    @Test
    void testReadFieldPropertyShadowing() {
        def shell = new GroovyShell()
        shell.parse '''package p
            abstract class A {
                Closure<String> getFoo() { return { -> 'A' } }
            }
            class B extends A {
                protected Closure<String> foo = { -> 'B' }
            }
        '''
        assertScript shell, '''import p.*
            class C extends B {
                void test() {
                    assert foo() == 'B'
                    assert this.foo() == 'B'
                    assert this.@foo() == 'B'
                    assert this.getFoo()() == 'A'

                    def that = new C()
                    assert that.foo() == 'B'
                    assert that.@foo() == 'B'
                    assert that.getFoo()() == 'A'
                }
            }

            new C().test()
            assert new C().foo() == 'A' // not the field from this perspective
        '''
    }
}

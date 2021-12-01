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

final class Groovy4035 {

    @Test
    void testSuperCallInsideAnAIC() {
        assertScript '''
            class Foo {
                def bar(baz) {
                    'Foo:' + baz
                }
            }

            def aic = new Foo() {
                def bar(baz) {
                    'AIC:' + super.bar(baz)
                }
            }

            String result = aic.bar('42')
            assert result == 'AIC:Foo:42'
        '''
    }

    @Test
    void testSuperCallInsideANormalInnerClass() {
        assertScript '''
            class Foo {
                def bar(baz) {
                    'Foo:' + baz
                }
            }

            class C {
                class D extends Foo {
                    def bar(baz) {
                        'C$D:' + super.bar(baz)
                    }
                }
            }

            String result = new C.D(new C()).bar('42')
            assert result == 'C$D:Foo:42'
        '''
    }
}

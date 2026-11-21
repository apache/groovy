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

import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy7947 {

    @Test
    void testPlusPlusThis() {
        assertScript '''
            void check(x) {
                assert x == 1
            }
            def next() {
                return 1
            }

            check(++this)
        '''
    }

    @Test
    void testThisPlusPlus() {
        assertScript '''
            void check(x) {
                assert x == this
            }
            def next() {
                return 1
            }

            check(this++)
        '''
    }

    @Test
    void testSuperPlusPlus() {
        assertScript '''
            void check(x) {
                assert x == this
            }
            def next() {
                return 1
            }

            check(super++)
        '''
    }

    @Test
    void testPlusPlusSuper() {
        assertScript '''
            void check(x) {
                assert x == 1
            }
            def next() {
                return 1
            }

            check(++super)
        '''
    }

    @Test
    void testPlusPlusSuper2() {
        def shell = GroovyShell.withConfig {
            imports {
                star 'groovy.transform'
            }
        }
        assertScript shell, '''
            @InheritConstructors
            abstract class SuperClass extends Script {
                def next() {
                    return 2
                }
            }

            null
        '''
        assertScript shell, '''
            @BaseScript SuperClass superClass

            void check(x) {
                assert x == 1
            }
            def next() {
                return 1
            }

            check(++super) // runs like this.next()
        '''
    }

    @Test
    void testPlusPlusSuperCS() {
        def shell = GroovyShell.withConfig {
            ast CompileStatic
        }
        def err = shouldFail shell, '''
            def next() {
                return 1
            }

            ++super
        '''
        assert err =~ /Cannot find matching method groovy.lang.Script#next/
    }
}

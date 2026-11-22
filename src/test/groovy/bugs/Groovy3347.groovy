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

/**
 * GROOVY-3347: applying the same mixin to a class and its subclass
 * before any instances were created used to overflow the stack in
 * {@code toString}. The original attachment now succeeds.
 */
final class Groovy3347 {

    @Test
    void testMixinBeforeInstanceCreationDoesNotOverflow() {
        // exact scenario from the ticket attachment (mixinBug.groovy)
        assertScript '''
            class A {
                String toString() {
                    "A"
                }
            }

            class B extends A {
                String toString() {
                    "B"+super.toString()
                }
            }

            class XCategory {
                static String toString(A a) {
                    "X" + a
                }
            }

            A.metaClass.mixin XCategory
            B.metaClass.mixin XCategory

            def a = new A()
            def b = new B()

            assert a.toString() == "XA"
            assert b.toString() == "XBA"
        '''
    }

    @Test
    void testMixinOnSubclassAloneDoesNotOverflow() {
        // comment 1 on the ticket: mixin only the superclass, then construct B
        assertScript '''
            class A {
                String toString() { "A" }
            }
            class B extends A {
                String toString() { "B"+super.toString() }
            }
            class XCategory {
                static String toString(A a) { "X" + a }
            }

            A.metaClass.mixin XCategory
            def b = new B()
            def result = b.toString()
            // historically this call StackOverflowError'd; it must complete
            assert result instanceof String
            assert result.contains('B')
            assert result.contains('A')
        '''
    }
}

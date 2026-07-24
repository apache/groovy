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
import static groovy.test.GroovyAssert.shouldFail

/**
 * A generic abstract method implemented with a covariant (narrowed) return type is
 * only recognised as implemented by {@code ClassCompletionVerifier} once the class
 * declaring the override has been through {@code Verifier}, which adds the bridge
 * method whose erased descriptor matches the abstract method's. Since the
 * GROOVY-10687 fix, a top-level class whose inner class contains a closure is
 * generated after its nest members — which can place it after its own subclass, so
 * the subclass is verified before the bridge exists and compilation fails with
 * "Abstract method 'T someValue()' is not implemented but a method of the same name
 * but different return type is defined" (GROOVY-12188). These tests pin that the
 * check recognises a covariant override independent of class generation order.
 */
final class Groovy12188 {

    // the reporter's example: the nest host with an inner-class closure sorts after its subclass
    @Test
    void testCovariantOverrideWithNestedClassClosure() {
        assertScript '''
            abstract class ProviderSpec<T> {
                abstract T someValue()
            }

            abstract class CollectionPropertySpec<C extends Collection<String>> extends ProviderSpec<C> {
                C someValue() { return null }

                static class Nested<T> {
                    void trigger(Optional<T> p) { p.map { it } }
                }
            }

            class DefaultListPropertyTest extends CollectionPropertySpec<List<String>> {
            }

            assert new DefaultListPropertyTest().someValue() == null
        '''
    }

    // no ordering can help here: the nest host must follow its member for the
    // NestMembers attribute but precede it for the bridge-based abstract check
    @Test
    void testCovariantOverrideWithNestMemberSubclass() {
        assertScript '''
            abstract class ProviderSpec<T> {
                abstract T someValue()
            }

            abstract class CollectionPropertySpec<C extends Collection<String>> extends ProviderSpec<C> {
                C someValue() { return null }

                static class Inner extends CollectionPropertySpec<List<String>> {
                    def c = { it }
                }
            }

            assert new CollectionPropertySpec.Inner().someValue() == null
        '''
    }

    // a same-name method whose return type is not covariant must still be rejected
    @Test
    void testNonCovariantReturnTypeStillRejected() {
        def err = shouldFail '''
            abstract class A<T extends Number> {
                abstract T someValue()
            }

            class B extends A<Integer> {
                String someValue() { return null }

                static class Nested {
                    def c = { it }
                }
            }
        '''
        assert err.message.contains('someValue')
    }
}

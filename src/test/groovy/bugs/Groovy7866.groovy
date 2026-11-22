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

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-7866: with ExpandoMetaClass enabled globally, a static closure
 * that mentioned an instance method used to invoke that CachedMethod and
 * throw IllegalArgumentException: "object is not an instance of declaring
 * class". The instance method must not be selected in a static context.
 */
final class Groovy7866 {

    @Test
    void testStaticClosureDoesNotInvokeInstanceMethodWithExpandoMetaClass() {
        try {
            def err = shouldFail(MissingMethodException, '''
                ExpandoMetaClass.enableGlobally()
                class Foo {
                    void attributes(Map attrs) {}
                    static stuff = {
                        attributes foo: "bar"
                    }
                }
                Foo.stuff.call()
            ''')
            assert err.message.contains('static method: attributes')
            assert err.message.contains('Foo')
        } finally {
            ExpandoMetaClass.disableGlobally()
        }
    }

    @Test
    void testStaticClosureDoesNotInvokeTraitInstanceMethodWithExpandoMetaClass() {
        try {
            def err = shouldFail(MissingMethodException, '''
                ExpandoMetaClass.enableGlobally()
                trait Attributes {
                    void attributes(Map attrs) {}
                }
                class Foo implements Attributes {
                    String attributes
                    static stuff = {
                        attributes foo: "bar"
                    }
                }
                new Foo().attributes(foo: 'bar')
                Foo.stuff.call()
            ''')
            assert err.message.contains('static method: attributes')
            assert err.message.contains('Foo')
        } finally {
            ExpandoMetaClass.disableGlobally()
        }
    }
}

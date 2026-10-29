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
package org.apache.groovy.groovyfx

import org.apache.groovy.groovyfx.factory.NodeFactory
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * The factory is reflective, so its construction policy can be checked on
 * plain classes without JavaFX.
 */
final class NodeFactoryTest {

    static class TextOnly {
        String text
    }

    static class TextAndConstructor {
        String text
        TextAndConstructor() {}
        TextAndConstructor(String text) {
            throw new GroovyRuntimeException("constructor ran with $text")
        }
    }

    static class Plain {
    }

    private static Object create(Class beanClass, Object value) {
        new NodeFactory(beanClass).newInstance(null, 'node', value, [:])
    }

    @Test
    void textBecomesTheTextPropertyWhenNoConstructorAcceptsIt() {
        def plain = create(TextOnly, 'hello')
        assert plain.text == 'hello'
        def interpolated = create(TextOnly, "hel${'lo'}")
        assert interpolated.text == 'hello'
    }

    @Test
    void aConstructorThatAcceptsTextIsPreferredAndItsFailurePropagates() {
        def e = shouldFail(GroovyRuntimeException) { create(TextAndConstructor, 'boom') }
        assert e.message == 'constructor ran with boom'
    }

    @Test
    void aValueWithNoMatchingConstructorIsReported() {
        def e = shouldFail(GroovyRuntimeException) { create(Plain, 42) }
        assert e.message.contains('Could not find matching constructor')
        assert e.message.contains(Plain.name)
    }
}

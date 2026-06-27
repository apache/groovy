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
package groovy.lang

import org.codehaus.groovy.runtime.CurriedClosure
import org.junit.jupiter.api.Test

import java.io.InvalidObjectException

import static groovy.test.GroovyAssert.shouldFail

/**
 * A closure whose {@code owner}/{@code delegate}/{@code thisObject} references form a
 * cycle recurses indefinitely on invocation, exhausting the stack. Such a graph cannot
 * be produced by normal Groovy code, but it can be forged in a hand-crafted serialized
 * stream and used as a denial-of-service "gadget" in applications that deserialize
 * untrusted data. {@link Closure} rejects such graphs during deserialization; these
 * tests cover that rejection and confirm that legitimate closure serialization is
 * unaffected.
 */
final class ClosureSerializationCycleTest {

    private static void setClosureField(Closure target, String name, Object value) {
        def field = Closure.getDeclaredField(name)
        field.accessible = true
        field.set(target, value)
    }

    private static byte[] serialize(Object obj) {
        def out = new ByteArrayOutputStream()
        out.withObjectOutputStream { it.writeObject(obj) }
        out.toByteArray()
    }

    private static Object deserialize(byte[] bytes) {
        new ByteArrayInputStream(bytes).withObjectInputStream(ClosureSerializationCycleTest.classLoader) {
            it.readObject()
        }
    }

    @Test
    void testSelfReferentialClosureRejectedOnDeserialization() {
        def cc = new CurriedClosure({ a, b -> "$a-$b" }, 'x')
        setClosureField(cc, 'owner', cc)
        setClosureField(cc, 'delegate', cc)

        byte[] bytes = serialize(cc)

        def err = shouldFail(InvalidObjectException) { deserialize(bytes) }
        assert err.message.contains('cycle')
    }

    @Test
    void testMutualClosureCycleRejectedOnDeserialization() {
        // a two-closure cycle (A -> B -> A) — defeats a naive self-reference-only check.
        // Overwrite both owner and delegate so the graph is self-contained (the wrapped
        // base closures, whose thisObject is the non-serializable test instance, are orphaned).
        def a = new CurriedClosure({ p -> p }, 'x')
        def b = new CurriedClosure({ p -> p }, 'y')
        setClosureField(a, 'owner', b)
        setClosureField(a, 'delegate', b)
        setClosureField(b, 'owner', a)
        setClosureField(b, 'delegate', a)

        byte[] bytes = serialize(a)

        def err = shouldFail(InvalidObjectException) { deserialize(bytes) }
        assert err.message.contains('cycle')
    }

    @Test
    void testSelfReferentialComposedClosureRejectedOnDeserialization() {
        // ComposedClosure is the other built-in serializable gadget closure; it gets the
        // same readResolve cycle check as CurriedClosure
        byte[] bytes = Holder.serializeCyclicComposed()
        def err = shouldFail(InvalidObjectException) { deserialize(bytes) }
        assert err.message.contains('cycle')
    }

    @Test
    void testLegitimateComposedClosureRoundTrips() {
        byte[] bytes = Holder.serializeComposed()
        def c = deserialize(bytes)
        assert c.call(10) == 22 // (10 + 1) * 2
    }

    @Test
    void testLegitimateClosureRoundTrips() {
        // owner is the (non-closure) enclosing object; must deserialize and remain callable
        byte[] bytes = Holder.serializeGreeter()
        def c = deserialize(bytes)
        assert c.call('Daniel') == 'Hello, Daniel'
    }

    @Test
    void testLegitimateCurriedClosureRoundTrips() {
        // a curried closure's owner and delegate point at the *same* wrapped clone (a
        // shared reference / diamond, not a cycle) — it must pass validation and work
        byte[] bytes = Holder.serializeCurried()
        def c = deserialize(bytes)
        assert c.call('y') == 'x-y'
    }

    static class Holder {
        static byte[] serializeGreeter() {
            serialize({ p -> "Hello, $p" })
        }

        static byte[] serializeCurried() {
            def base = { a, b -> "$a-$b" }
            serialize(base.curry('x'))
        }

        static byte[] serializeComposed() {
            def inc = { b -> b + 1 }
            def twice = { a -> a * 2 }
            serialize(inc >> twice)
        }

        static byte[] serializeCyclicComposed() {
            // built in a static context so the wrapped closures' owner is the (serializable)
            // class rather than the test instance, then the owner/delegate are made self-referential
            def composed = ({ x -> x } >> { y -> y })
            setClosureField(composed, 'owner', composed)
            setClosureField(composed, 'delegate', composed)
            serialize(composed)
        }
    }
}

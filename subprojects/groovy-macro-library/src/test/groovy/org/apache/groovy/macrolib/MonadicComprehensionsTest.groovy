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
package org.apache.groovy.macrolib

import groovy.concurrent.Awaitable
import groovy.concurrent.DataflowVariable
import groovy.transform.Monadic
import org.apache.groovy.runtime.Comprehensions
import org.apache.groovy.runtime.async.AsyncSupport
import org.junit.jupiter.api.Test

import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Verifies the {@link Comprehensions} bind/map dispatcher delivers the
 * monadic-comprehension semantics across every carrier in the allow-list plus
 * structural, Closure-surface, and {@code @Monadic} participation.
 *
 * Each test writes the nested {@code Comprehensions.bind} chain directly &mdash;
 * the shape the {@code DO} macro emits:
 * <pre>
 *   DO(x in m1, y in f(x)) { body }
 *     ==&gt;  Comprehensions.bind(m1) { x -&gt; Comprehensions.bind(f(x)) { y -&gt; body } }
 * </pre>
 */
final class MonadicComprehensionsTest {

    @Test
    void optional_composes_and_short_circuits() {
        def sum = Comprehensions.bind(Optional.of(2)) { a ->
            Comprehensions.bind(Optional.of(3)) { b ->
                Optional.of(a + b)
            }
        }
        assertEquals(Optional.of(5), sum)

        def shorted = Comprehensions.bind(Optional.empty()) { a ->
            Comprehensions.bind(Optional.of(3)) { b -> Optional.of(a + b) }
        }
        assertEquals(Optional.empty(), shorted)
    }

    @Test
    void stream_cartesian_composition() {
        def result = Comprehensions.bind(Stream.of(1, 2)) { a ->
            Comprehensions.bind(Stream.of(10, 20)) { b ->
                Stream.of(a + b)
            }
        }
        assertEquals([11, 21, 12, 22], ((Stream) result).toList())
    }

    @Test
    void completableFuture_composes() {
        def fut = Comprehensions.bind(CompletableFuture.completedFuture(2)) { a ->
            Comprehensions.bind(CompletableFuture.completedFuture(3)) { b ->
                CompletableFuture.completedFuture(a + b)
            }
        }
        assertEquals(5, ((CompletableFuture) fut).get())
    }

    @Test
    void awaitable_composes_via_thenCompose() {
        // Awaitable.then is map; thenCompose is bind.
        def result = Comprehensions.bind(Awaitable.of(2)) { a ->
            Comprehensions.bind(Awaitable.of(3)) { b ->
                Awaitable.of(a + b)
            }
        }
        assertEquals(5, AsyncSupport.await((Awaitable) result))
    }

    @Test
    void dataflowVariable_composes() {
        def x = new DataflowVariable()
        def y = new DataflowVariable()
        x.bind(10)
        y.bind(5)
        def sum = Comprehensions.bind(x) { a ->
            Comprehensions.bind(y) { b ->
                Awaitable.of(a + b)
            }
        }
        assertEquals(15, AsyncSupport.await((Awaitable) sum))
    }

    @Test
    void structural_participation_no_annotation() {
        def boxed = Comprehensions.bind(new Box(2)) { a ->
            Comprehensions.bind(new Box(3)) { b ->
                new Box(a + b)
            }
        }
        assertEquals(5, ((Box) boxed).v)
    }

    @Test
    void closure_surface_participation() {
        // The bind method accepts a Closure rather than a Function.
        def boxed = Comprehensions.bind(new ClosureBox(2)) { a ->
            Comprehensions.bind(new ClosureBox(3)) { b ->
                new ClosureBox(a + b)
            }
        }
        assertEquals(5, ((ClosureBox) boxed).v)
    }

    @Test
    void monadic_annotation_with_name_overrides() {
        def res = Comprehensions.bind(new Res(2)) { a ->
            Comprehensions.bind(new Res(3)) { b ->
                new Res(a + b)
            }
        }
        assertEquals(new Res(5), res) // value equality, not identity
    }

    @Test
    void map_role_uses_map_name() {
        assertEquals(Optional.of(3), Comprehensions.map(Optional.of(2)) { it + 1 })
        def r = Comprehensions.map(new Res(2)) { it * 10 } // map -> 'transform'
        assertEquals(new Res(20), r)
    }

    @Test
    void monadic_carrier_has_value_equality() {
        assertEquals(new Res(5), new Res(5))
        assertEquals(new Res(5).hashCode(), new Res(5).hashCode())
        assertTrue(new Res(1) != new Res(2))
    }

    @Test
    void monadic_annotation_exposes_unit_member() {
        def a = Res.getAnnotation(Monadic)
        assertEquals('chain', a.bind())
        assertEquals('transform', a.map())
        assertEquals('unit', a.unit())
        assertEquals('', Monadic.getMethod('unit').defaultValue) // undeclared by default
    }

    @Test
    void non_participating_type_fails_with_a_precise_message() {
        def ex = assertThrows(IllegalArgumentException) {
            Comprehensions.bind(new Plain(1)) { a -> new Plain(a) }
        }
        assertTrue(ex.message.contains(Plain.name))
        assertTrue(ex.message.contains('does not participate'))
        assertTrue(ex.message.contains('@Monadic'))
    }
}

/** Structural carrier: conventional flatMap/map taking a java.util.function.Function. */
class Box {
    final Object v
    Box(Object v) { this.v = v }
    Box flatMap(Function f) { (Box) f.apply(v) }
    Box map(Function f) { new Box(f.apply(v)) }
    String toString() { "Box($v)" }
}

/** Structural carrier whose bind/map take a Closure rather than a Function. */
class ClosureBox {
    final Object v
    ClosureBox(Object v) { this.v = v }
    ClosureBox flatMap(Closure f) { (ClosureBox) f.call(v) }
    ClosureBox map(Closure f) { new ClosureBox(f.call(v)) }
    String toString() { "ClosureBox($v)" }
}

/**
 * Opt-in carrier with non-conventional method names declared via @Monadic, plus
 * the structural equality its laws need and a declared unit factory.
 */
@Monadic(bind = 'chain', map = 'transform', unit = 'unit')
class Res {
    final Object v
    Res(Object v) { this.v = v }
    static Res unit(Object a) { new Res(a) }
    Res chain(Function f) { (Res) f.apply(v) }
    Res transform(Function f) { new Res(f.apply(v)) }
    boolean equals(Object o) { o instanceof Res && this.v == ((Res) o).v }
    int hashCode() { v == null ? 0 : v.hashCode() }
    String toString() { "Res($v)" }
}

/** A type that does not participate at all (negative case). */
class Plain {
    final Object v
    Plain(Object v) { this.v = v }
}

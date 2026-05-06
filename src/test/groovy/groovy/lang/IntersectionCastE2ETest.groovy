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

import org.junit.jupiter.api.Test

/**
 * End-to-end tests for intersection-cast lambdas and method references
 * (GROOVY-11998 PR3).
 *
 * Verifies that {@code (R & S) lambda} and {@code (R & S) Type::method} produce
 * runtime instances that:
 * <ul>
 *   <li>Implement every component interface ({@code instanceof} succeeds)</li>
 *   <li>Are serialisable when the intersection contains
 *       {@link java.io.Serializable}</li>
 *   <li>Invoke the SAM correctly</li>
 * </ul>
 */
final class IntersectionCastE2ETest {

    @Test
    void 'static lambda cast to (Runnable & Serializable) implements both and runs'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic
            import java.io.Serializable

            @CompileStatic
            class T {
                static Runnable make() {
                    return (Runnable & Serializable) () -> {}
                }
            }

            def r = T.make()
            assert r instanceof Runnable
            assert r instanceof Serializable
            r.run() // does not throw
        ''')
    }

    @Test
    void 'static intersection lambda is serialisable round-trip'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic
            import java.io.Serializable
            import java.io.ByteArrayOutputStream
            import java.io.ByteArrayInputStream
            import java.io.ObjectOutputStream
            import java.io.ObjectInputStream

            @CompileStatic
            class T {
                static Runnable make() {
                    return (Runnable & Serializable) () -> { System.out.println("hi") }
                }
            }

            def r = T.make()
            def baos = new ByteArrayOutputStream()
            new ObjectOutputStream(baos).withCloseable { it.writeObject(r) }
            def bais = new ByteArrayInputStream(baos.toByteArray())
            def restored = null
            new ObjectInputStream(bais).withCloseable { restored = it.readObject() }
            assert restored instanceof Runnable
            assert restored instanceof Serializable
            restored.run() // round-trip executes
        ''')
    }

    @Test
    void 'static lambda cast to (Runnable & Cloneable) implements Cloneable marker'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic

            @CompileStatic
            class T {
                static Runnable make() {
                    return (Runnable & Cloneable) () -> {}
                }
            }

            def r = T.make()
            assert r instanceof Runnable
            assert r instanceof Cloneable
        ''')
    }

    @Test
    void 'static method reference cast to (Function & Serializable) is serialisable'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic
            import java.io.Serializable
            import java.util.function.Function
            import java.io.ByteArrayOutputStream
            import java.io.ByteArrayInputStream
            import java.io.ObjectOutputStream
            import java.io.ObjectInputStream

            @CompileStatic
            class T {
                static Function<String, Integer> make() {
                    return (Function<String, Integer> & Serializable) String::length
                }
            }

            Function<String, Integer> f = T.make()
            assert f instanceof Function
            assert f instanceof Serializable
            assert f.apply("hello") == 5

            def baos = new ByteArrayOutputStream()
            new ObjectOutputStream(baos).withCloseable { it.writeObject(f) }
            def bais = new ByteArrayInputStream(baos.toByteArray())
            Function<String, Integer> restored = null
            new ObjectInputStream(bais).withCloseable { restored = (Function<String, Integer>) it.readObject() }
            assert restored.apply("world") == 5
        ''')
    }

    @Test
    void 'static lambda with capturing variable cast to intersection works'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic
            import java.io.Serializable
            import java.util.function.Supplier

            @CompileStatic
            class T {
                static Supplier<String> make(String captured) {
                    return (Supplier<String> & Serializable) () -> captured
                }
            }

            def s = T.make("captured-value")
            assert s instanceof Supplier
            assert s instanceof Serializable
            assert s.get() == "captured-value"
        ''')
    }

    @Test
    void 'intersection lambda with three components includes all markers'() {
        def shell = new GroovyShell()
        shell.evaluate('''
            import groovy.transform.CompileStatic
            import java.io.Serializable

            @CompileStatic
            class T {
                static Runnable make() {
                    return (Runnable & Serializable & Cloneable) () -> {}
                }
            }

            def r = T.make()
            assert r instanceof Runnable
            assert r instanceof Serializable
            assert r instanceof Cloneable
        ''')
    }
}

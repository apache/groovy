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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.fail

/**
 * The {@code DO} macro under {@code @CompileStatic} with the
 * {@code groovy.typecheckers.MonadicChecker} extension: the extension
 * (a) types the generator closure parameter as the carrier element type so the
 * body type-checks, (b) restores the comprehension result type so downstream
 * use type-checks, and (c) rejects non-participating carriers at compile time.
 */
final class DoStaticTest {

    private static final String CS = "@groovy.transform.CompileStatic(extensions='groovy.typecheckers.MonadicChecker')"

    @Test
    void boundParameterIsTypedAsElementType_optional() {
        assertScript """
            $CS
            class C {
                static int run() {
                    def r = DO(a in Optional.of(2),
                               b in Optional.of(3)) { Optional.of(a.intValue() + b.intValue()) }
                    r.get()
                }
            }
            assert C.run() == 5
        """
    }

    @Test
    void dependentGeneratorTypeChecks() {
        assertScript """
            $CS
            class C {
                static int run() {
                    def r = DO(a in Optional.of(2),
                               b in Optional.of(a + 10)) { Optional.of(a + b) }
                    r.get()
                }
            }
            assert C.run() == 14
        """
    }

    @Test
    void streamResultTypeChecksDownstream() {
        assertScript """
            import java.util.stream.Stream
            $CS
            class C {
                static List<Integer> run() {
                    def r = DO(a in Stream.of(1, 2),
                               b in Stream.of(10, 20)) { Stream.of(a + b) }
                    r.toList()
                }
            }
            assert C.run() == [11, 21, 12, 22]
        """
    }

    @Test
    void awaitableUnderCompileStatic() {
        assertScript """
            import groovy.concurrent.Awaitable
            import static org.apache.groovy.runtime.async.AsyncSupport.await
            $CS
            class C {
                static int run() {
                    def r = DO(a in Awaitable.of(2),
                               b in Awaitable.of(3)) { Awaitable.of(a + b) }
                    await(r)
                }
            }
            assert C.run() == 5
        """
    }

    @Test
    void monadicAnnotatedTypeUnderCompileStatic() {
        assertScript """
            import groovy.transform.Monadic
            import java.util.function.Function

            @Monadic(bind = 'chain', map = 'transform')
            class Res {
                final Object v
                Res(Object v) { this.v = v }
                Res chain(Function f) { (Res) f.apply(v) }
                Res transform(Function f) { new Res(f.apply(v)) }
            }

            $CS
            class C {
                static Object run() {
                    def r = DO(a in new Res(2), b in new Res(3)) { new Res(a) }
                    r.v
                }
            }
            assert C.run() == 2
        """
    }

    @Test
    void rejectsNonParticipatingCarrierAtCompileTime() {
        assertCompileError("""
            $CS
            class C {
                static def run() {
                    DO(a in new Object()) { Optional.of(a) }
                }
            }
        """, 'does not participate in monadic comprehensions')
    }

    @Test
    void rejectsBareBodyAtCompileTime() {
        // Body returns a bare value; the dispatcher's erased (Object,Closure):Object
        // signature lets STC accept this, but Optional.flatMap would fail at runtime.
        assertCompileError("""
            $CS
            class C {
                static def run() {
                    DO(a in Optional.of(2)) { a + 1 }
                }
            }
        """, 'must yield java.util.Optional')
    }

    @Test
    void rejectsCrossCarrierBodyAtCompileTime() {
        // Outer carrier Optional, body produces a Stream — well-typed against the
        // erased dispatcher but rejected by Optional.flatMap at runtime.
        assertCompileError("""
            import java.util.stream.Stream
            $CS
            class C {
                static def run() {
                    DO(a in Optional.of(2)) { Stream.of(a) }
                }
            }
        """, 'Mixing carriers in a comprehension is not supported')
    }

    @Test
    void rejectsCrossCarrierInNestedDoAtCompileTime() {
        // The classic gotcha: outer Optional, inner Stream — the outer bind's
        // closure ends up yielding Stream, contradicting the receiver Optional.
        assertCompileError("""
            import java.util.stream.Stream
            $CS
            class C {
                static def run() {
                    DO(a in Optional.of(2),
                       b in Stream.of(a, a + 10)) { Stream.of(b) }
                }
            }
        """, 'Mixing carriers in a comprehension is not supported')
    }

    @Test
    void rejectsCarrierWithOnly2ArgFlatMapAtCompileTime() {
        // Regression: hasMethodNamed used to count any 'flatMap' regardless of
        // arity, so a 2-arg flatMap satisfied participation statically and
        // then failed at runtime in the dispatcher (which needs a single-arg
        // method). Now aligned with the runtime rule.
        assertCompileError("""
            class Box {
                final int v
                Box(int v) { this.v = v }
                Box flatMap(int extra, Closure c) { (Box) c.call(v + extra) }
            }
            $CS
            class C {
                static def run() {
                    DO(a in new Box(2)) { new Box(((Integer) a) + 1) }
                }
            }
        """, 'does not participate in monadic comprehensions')
    }

    @Test
    void monadicAnnotationOnSuperclassAcceptedAtCompileTime() {
        // Regression: the static @Monadic check used to look only at the type's
        // own annotations; the runtime walks superclasses. Without the walk,
        // a SubRes whose @Monadic lives on BaseRes would be rejected here yet
        // accepted at runtime.
        assertScript """
            import groovy.transform.Monadic

            @Monadic
            class BaseRes {
                final Object v
                BaseRes(Object v) { this.v = v }
                BaseRes flatMap(Closure c) { (BaseRes) c.call(v) }
                BaseRes map(Closure c) { new BaseRes(c.call(v)) }
            }
            class SubRes extends BaseRes {
                SubRes(Object v) { super(v) }
            }

            $CS
            class C {
                static Object run() {
                    def r = DO(a in new SubRes(2),
                               b in new SubRes(3)) { new SubRes(b) }
                    r.v
                }
            }
            assert C.run() == 3
        """
    }

    @Test
    void monadicAnnotationOnInterfaceAcceptedAtCompileTime() {
        // Regression: @Monadic declared on an interface was rejected by the
        // static check (which didn't walk interfaces); the runtime accepts it.
        assertScript """
            import groovy.transform.Monadic

            @Monadic
            interface IRes {
                IRes flatMap(Closure c)
                IRes map(Closure c)
            }
            class Holder implements IRes {
                final Object v
                Holder(Object v) { this.v = v }
                IRes flatMap(Closure c) { (IRes) c.call(v) }
                IRes map(Closure c) { new Holder(c.call(v)) }
            }

            $CS
            class C {
                static Object run() {
                    def r = DO(a in new Holder(2),
                               b in new Holder(3)) { new Holder(b) }
                    ((Holder) r).v
                }
            }
            assert C.run() == 3
        """
    }

    private static void assertCompileError(String script, String expectedMessage) {
        try {
            new GroovyShell().parse(script)
            fail("Expected a compilation error containing: $expectedMessage")
        } catch (MultipleCompilationErrorsException e) {
            assert e.message.contains(expectedMessage)
        }
    }
}

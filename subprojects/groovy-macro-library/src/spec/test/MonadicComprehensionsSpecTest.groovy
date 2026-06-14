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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Worked, inline-tested examples for the "Monadic comprehensions" specification
 * chapter (_monadic-comprehensions.adoc). Each tagged region is included into the
 * manual and run as part of the build.
 */
final class MonadicComprehensionsSpecTest {

    @Test
    void basic() {
        assertScript '''
        // tag::do_basic[]
        def result = DO(a in Optional.of(2),
                        b in Optional.of(3)) {
            Optional.of(a + b)
        }
        assert result.get() == 5
        // end::do_basic[]
        '''
    }

    @Test
    void shortCircuit() {
        assertScript '''
        // tag::do_shortcircuit[]
        def result = DO(a in Optional.empty(),
                        b in Optional.of(3)) {
            Optional.of(b)            // never reached
        }
        assert result.isEmpty()
        // end::do_shortcircuit[]
        '''
    }

    @Test
    void dependentGenerators() {
        assertScript '''
        // tag::do_dependent[]
        def result = DO(a in Optional.of(10),
                        b in Optional.of(a * 2)) {   // b's source depends on a
            Optional.of(a + b)
        }
        assert result.get() == 30
        // end::do_dependent[]
        '''
    }

    @Test
    void stream() {
        assertScript '''
        // tag::do_stream[]
        import java.util.stream.Stream

        def pairs = DO(x in Stream.of(1, 2),
                       y in Stream.of('a', 'b')) {
            Stream.of("$x$y".toString())
        }
        assert pairs.toList() == ['1a', '1b', '2a', '2b']
        // end::do_stream[]
        '''
    }

    @Test
    void awaitable() {
        assertScript '''
        // tag::do_awaitable[]
        import groovy.concurrent.Awaitable
        import static org.apache.groovy.runtime.async.AsyncSupport.await

        def total = DO(a in Awaitable.of(2),
                       b in Awaitable.of(40)) {
            Awaitable.of(a + b)
        }
        assert await(total) == 42
        // end::do_awaitable[]
        '''
    }

    @Test
    void monadicAnnotation() {
        assertScript '''
        // tag::do_monadic[]
        import groovy.transform.Monadic
        import java.util.function.Function

        @Monadic(bind = 'chain', map = 'transform', unit = 'of')
        class Result {
            final Object value
            Result(Object value) { this.value = value }
            static Result of(value) { new Result(value) }     // unit: not used by DO, declared for law tooling
            Result chain(Function f) { (Result) f.apply(value) }
            Result transform(Function f) { new Result(f.apply(value)) }
            boolean equals(o) { o instanceof Result && value == o.value }   // results compare by value...
            int hashCode() { value == null ? 0 : value.hashCode() }
        }

        def r = DO(a in Result.of(3),
                   b in Result.of(4)) {
            Result.of(a * b)
        }
        assert r == new Result(12)                            // ...so == means "same wrapped value"
        // end::do_monadic[]
        '''
    }

    @Test
    void compileStatic() {
        assertScript '''
        // tag::do_static[]
        import groovy.transform.CompileStatic

        @CompileStatic(extensions = 'groovy.typecheckers.MonadicChecker')
        class Calc {
            static int sum() {
                DO(a in Optional.of(2),
                   b in Optional.of(3)) {
                    Optional.of(a + b)
                }.get()
            }
        }
        assert Calc.sum() == 5
        // end::do_static[]
        '''
    }
}

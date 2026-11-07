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
package org.apache.groovy.ginq

import groovy.ginq.Ginq
import groovy.ginq.transform.GQ
import groovy.transform.CompileStatic
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Deferred execution of GINQ. Elements are pulled by a terminal operation.
 * A second {@code toList()} uses the list already materialized.
 */
@CompileStatic
final class GinqLazinessTest {

    private final List<Integer> seen = []

    @Test
    void dynamicWhereDoesNotPullUntilToListAndRereadsLocals() {
        assertScript '''
            int opened = 0
            int pulls = 0
            def gate = [
                iterator: {
                    opened++
                    def raw = [1, 2, 3].iterator()
                    [
                        hasNext: { raw.hasNext() },
                        next: { pulls++; raw.next() }
                    ] as Iterator
                }
            ] as Iterable
            def filtered = []
            int min = 0
            def query = GQ {
                from n in gate
                where (filtered << n) != null && n > min
                select n
            }
            assert filtered.isEmpty()
            assert pulls == 0
            min = 1
            // The local is bound when the pipeline is built, which is this expression.
            assert [1, 2, 3] == query.toList()
            assert filtered == [1, 2, 3]
            assert pulls == 3
            int after = pulls
            assert [1, 2, 3] == query.toList()
            assert pulls == after
        '''
    }

    @Test
    void staticQueryWaitsForTheTerminalAndKeepsTheCapturedLocal() {
        Gate gate = new Gate([1, 2, 3])
        int min = 0
        Queryable query = GQ {
            from n in gate
            where note(n) && n > min
            select n
        }
        assert seen.isEmpty()
        assert gate.opened == 0
        assert gate.pulls == 0
        min = 1
        // Same binding as a dynamic query: min was 0 when the expression ran.
        assert [1, 2, 3] == query.toList()
        assert seen == [1, 2, 3]
        assert gate.opened == 1
        assert gate.pulls == 3
        int after = gate.pulls
        assert [1, 2, 3] == query.toList()
        assert gate.pulls == after
    }

    @Test
    void gqlMaterializesImmediately() {
        Gate gate = new Gate([1, 2, 3])
        List values = GQL {
            from n in gate
            select n
        }
        assert values == [1, 2, 3]
        assert gate.pulls == 3
    }

    @Test
    void annotationReturnsAQueryableThatHasNotRun() {
        List<Integer> values = [1, 2, 3]
        Queryable query = aboveOne(values)
        assert seen.isEmpty()
        assert [2, 3] == query.toList()
        assert seen == [1, 2, 3]
    }

    @Test
    void fluentOperatorsWaitUntilTheTerminal() {
        Gate gate = new Gate([1, 2, 3, 4])
        int keys = 0
        def grouped = Ginq.from(gate)
                .where { Integer n -> n > 0 }
                .groupBy { Integer n ->
                    keys++
                    n % 2
                }
        assert gate.opened == 0
        assert keys == 0
        grouped.toList()
        assert gate.pulls == 4
        assert keys == 4

        Gate ordered = new Gate([3, 1, 2])
        int comparisons = 0
        def query = Ginq.from(ordered).orderBy { Integer n ->
            comparisons++
            n
        }.select { Integer n -> n * 10 }
        assert ordered.opened == 0
        assert comparisons == 0
        assert query.toList() == [10, 20, 30]
        assert ordered.pulls == 3
        assert comparisons > 0
    }

    private boolean note(int n) {
        seen.add(n)
        return true
    }

    @GQ
    private Queryable aboveOne(List<Integer> values) {
        from n in values
        where note(n) && n > 1
        select n
    }

    static final class Gate implements Iterable<Integer> {
        private final List<Integer> data
        int opened
        int pulls

        Gate(List<Integer> data) {
            this.data = data
        }

        @Override
        Iterator<Integer> iterator() {
            opened++
            Iterator<Integer> raw = data.iterator()
            return new Iterator<Integer>() {
                @Override
                boolean hasNext() {
                    return raw.hasNext()
                }

                @Override
                Integer next() {
                    pulls++
                    return raw.next()
                }
            }
        }
    }
}

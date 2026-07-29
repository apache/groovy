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
package org.codehaus.groovy.runtime

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * Tests the loop-control runtime protocol (GROOVY-12126): DGM iterator methods
 * marked with {@code @SupportsLoopControl} catch the {@link LoopControl}
 * signals around each per-element closure invocation. The signals are thrown
 * directly here; the compiler sugar (break/continue in closures) has its own
 * tests in gls.closures.LoopControlTest.
 */
final class LoopControlDGMTest {

    @Test
    void testEachBreak() {
        def seen = []
        [1, 2, 3, 4].each {
            if (it == 3) throw LoopControl.BREAK
            seen << it
        }
        assert seen == [1, 2]
    }

    @Test
    void testEachContinue() {
        def seen = []
        [1, 2, 3, 4].each {
            if (it % 2 == 0) throw LoopControl.CONTINUE
            seen << it
        }
        assert seen == [1, 3]
    }

    @Test
    void testEachIterator() {
        def seen = []
        [1, 2, 3].iterator().each {
            if (it == 2) throw LoopControl.BREAK
            seen << it
        }
        assert seen == [1]
    }

    @Test
    void testEachMap() {
        def seen = []
        [a: 1, b: 2, c: 3].each { k, v ->
            if (v == 2) throw LoopControl.CONTINUE
            if (v == 3) throw LoopControl.BREAK
            seen << k
        }
        assert seen == ['a']
    }

    @Test
    void testEachWithIndex() {
        def seen = []
        ['a', 'b', 'c', 'd'].eachWithIndex { item, i ->
            if (item == 'b') throw LoopControl.CONTINUE
            if (i == 3) throw LoopControl.BREAK
            seen << "$item$i".toString()
        }
        assert seen == ['a0', 'c2']
    }

    @Test
    void testEachWithIndexMap() {
        def seen = []
        [a: 1, b: 2, c: 3].eachWithIndex { entry, i ->
            if (i == 1) throw LoopControl.CONTINUE
            if (i == 2) throw LoopControl.BREAK
            seen << entry.key
        }
        assert seen == ['a']
    }

    @Test
    void testTimes() {
        def seen = []
        5.times {
            if (it == 1) throw LoopControl.CONTINUE
            if (it == 3) throw LoopControl.BREAK
            seen << it
        }
        assert seen == [0, 2]
    }

    @Test
    void testUptoDowntoStep() {
        def seen = []
        1.upto(5) {
            if (it == 4) throw LoopControl.BREAK
            seen << it
        }
        assert seen == [1, 2, 3]

        seen = []
        5.downto(1) {
            if (it == 4) throw LoopControl.CONTINUE
            if (it == 2) throw LoopControl.BREAK
            seen << it
        }
        assert seen == [5, 3]

        seen = []
        1.0.upto(3.0) {
            if (it == 2.0) throw LoopControl.BREAK
            seen << it
        }
        assert seen == [1.0]

        seen = []
        0.step(10, 2) {
            if (it == 6) throw LoopControl.BREAK
            seen << it
        }
        assert seen == [0, 2, 4]
    }

    @Test
    void testCollectBreakExcludesCurrentElement() {
        assert [1, 2, 3, 4].collect {
            if (it == 3) throw LoopControl.BREAK
            it * 10
        } == [10, 20]
    }

    @Test
    void testCollectContinueSkipsContribution() {
        assert [1, 2, 3, 4].collect {
            if (it % 2 == 0) throw LoopControl.CONTINUE
            it * 10
        } == [10, 30]
    }

    @Test
    void testCollectWithCollector() {
        assert [1, 2, 3].collect(new LinkedList()) {
            if (it == 3) throw LoopControl.BREAK
            it + 1
        } == [2, 3]
    }

    @Test
    void testCollectMap() {
        assert [a: 1, b: 2, c: 3].collect { k, v ->
            if (v == 1) throw LoopControl.CONTINUE
            if (v == 3) throw LoopControl.BREAK
            "$k$v".toString()
        } == ['b2']
    }

    @Test
    void testFindAll() {
        assert [1, 2, 3, 4, 5].findAll {
            if (it == 2) throw LoopControl.CONTINUE
            if (it == 4) throw LoopControl.BREAK
            it % 2 == 1
        } == [1, 3]
    }

    @Test
    void testFindAllMap() {
        assert [a: 1, b: 2, c: 3].findAll { k, v ->
            if (v == 2) throw LoopControl.BREAK
            true
        } == [a: 1]
    }

    @Test
    void testInjectBreakReturnsPriorAccumulator() {
        assert [1, 2, 3, 4].inject(0) { acc, it ->
            if (it == 3) throw LoopControl.BREAK
            acc + it
        } == 3
    }

    @Test
    void testInjectContinueKeepsPriorValue() {
        assert [1, 2, 3, 4].inject(0) { acc, it ->
            if (it % 2 == 0) throw LoopControl.CONTINUE
            acc + it
        } == 4
    }

    @Test
    void testInjectMap() {
        assert [a: 1, b: 2, c: 3].inject(0) { acc, entry ->
            if (entry.value == 2) throw LoopControl.CONTINUE
            acc + entry.value
        } == 4
    }

    @Test
    void testClosureDoneStillHonored() {
        def seen = []
        def c
        c = { seen << it; if (it == 1) c.directive = Closure.DONE }
        5.times(c)
        assert seen == [0, 1]

        def t
        t = { if (it == 2) t.directive = Closure.DONE; it * 10 }
        assert [1, 2, 3].collect([], t) == [10, 20]
    }

    @Test
    void testSignalEscapesUncooperativeMethod() {
        // 'with' is not a loop and does not cooperate; the signal must surface loudly
        def e = assertThrows(LoopControl) {
            'x'.with { throw LoopControl.BREAK }
        }
        assert e.message.contains('does not support loop control')
    }

    @Test
    void testSignalsCarryNoStackTrace() {
        assert LoopControl.BREAK.stackTrace.length == 0
        assert LoopControl.CONTINUE.stackTrace.length == 0
    }
}

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
package groovy

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class ForLoopTest {

    private int x

    @ParameterizedTest
    @ValueSource(strings=[
        'public','private','protected','abstract','static','native','strictfp','synchronized'
    ])
    void testFinalParameterInForLoopIsAllowed(String modifier) {
        // only 'final' should be allowed; other modifiers should be forbidden
        shouldFail """
            def collection = ["a", "b", "c", "d", "e"]
            for ($modifier String letter in collection) { }
        """

        shouldFail """
            def collection = ["a", "b", "c", "d", "e"]
            for (final $modifier String letter in collection) { }
        """

        assertScript '''
            def collection = ["a", "b", "c", "d", "e"]
            for (final String letter in collection) { }
            for (final String letter : collection) { }
            for (final letter in collection) { }
            for (final letter : collection) { }
        '''
    }

    @Test
    void testForEachInRange() {
        for (i in 0..9) {
            x = x + i
        }
        assert x == 45
    }

    @Test
    void testForEachInRangeWithType() {
        for (Integer i in 0..9) {
            assert i.getClass() == Integer
            x = x + i
        }
        assert x == 45
    }

    @Test
    void testForEachInRangeWithJdk15StyleAndType() {
        for (Integer i: 0..9) {
            assert i.getClass() == Integer
            x = x + i
        }
        assert x == 45
    }

    @Test
    void testForEachInList() {
        for (i in [0, 1, 2, 3, 4]) {
            x = x + i
        }
        assert x == 10
    }

    @Test
    void testForEachInArray() {
        for (i in new Integer[]{0, 1, 2, 3, 4}) {
            x = x + i
        }
        assert x == 10
    }

    @Test
    void testForEachInIntArray() {
        for (i in new int[]{1, 2, 3, 4, 5}) {
            x = x + i
        }
        assert x == 15
    }

    @Test
    void testForEachInString() {
        def list = []
        for (c in 'abc') {
            list.add(c)
        }
        assert list == ['a', 'b', 'c']
    }

    @Test
    void testForEachInVector() {
        def vector = new Vector()
        vector.addAll([1, 2, 3])

        def answer = []
        for (i in vector.elements()) {
            answer << i
        }
        assert answer == [1, 2, 3]
    }

    @Test
    void testClassicFor() {
        for (int i = 0; i < 10; i++) {
            x++
        }
        assert x == 10

        def list = [1, 2]
        x = 0
        for (Iterator i = list.iterator(); i.hasNext();) {
            x += i.next()
        }
        assert x == 3
    }

    @Test
    void testClassicForNested() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                x++
            }
        }
        assert x == 100
    }

    @Test
    void testClassicForWithContinue() {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) continue
            x += i
        }
        assert x == 25

        // same as before, but with label
        x = 0
        test:
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) continue test
            x += i
        }
        assert x == 25
    }

    // GROOVY-3898
    @Test
    void testClassicForWithMultiAssignment() {
        def result = ''
        for (def (int i, char c) = [0, 'x']; i < 3; ++i, c++) {
            result += c
        }
        assert result == 'xyz'

        result = 1
        for (def (i,j) = [0,0]; i < 3; {i++;j++}() ) { // odd
            result += i + j
        }
        assert result == 7 // 1 + 2 + 4
    }

    @Test
    void testClassicForWithEmptyInitializer() {
        def i = 0
        for (; i < 10; i++) {
            if (i % 2 == 0) continue
            x += i
        }
        assert x == 25
    }

    @Test
    void testClassicForWithEmptyStatementBody() {
        for (; x < 5; ++x) ;
        assert x == 5
    }

    @Test
    void testClassicForWithEverythingInitCondNextExpressionsEmpty() {
        int counter = 0
        for (;;) {
            counter += 1
            if (counter == 10) break
        }
        assert counter == 10, "The body of the for loop wasn't executed, it should have looped 10 times."
    }
}

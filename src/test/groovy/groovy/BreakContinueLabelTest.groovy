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

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.fail

final class BreakContinueLabelTest {

    @Test
    void testDeclareSimpleLabels() {
        label_1: print('foo')
        label_2:
        print('bar')
    }

    // GROOVY-7463
    @Test
    void testBreakLabelInIfStatement() {
        boolean flag = true
        label:
        if (flag) {
            print('foo')
            if (flag) {
                break label
                fail()
            }
            fail()
        } else {
            fail()
        }
        print('bar')
    }

    // GROOVY-7463
    @Test
    void testBreakLabelInIfStatement2() {
        int i = 0
        boolean flag = true
        label:
        if (flag) {
            print('foo')
            try {
                if (flag) {
                    print('bar')
                    break label
                    fail()
                }
                fail()
            } finally {
                i += 1
            }
            fail()
        }
        print('baz')
        assert i == 1
    }

    @Test
    void testBreakLabelInSimpleForLoop() {
        label: for (i in [1]) {
            break label;
            assert false
        }
    }

    @Test
    void testBreakLabelInNestedForLoop() {
        label: for (i in [1]) {
            for (j in [1]) {
                break label;
                assert false : 'did not break inner loop'
            }
            assert false : 'did not break outer loop'
        }
    }

    @Test
    void testBreakLabelInForLoopTryFinally() {
        int i = 0
        out:
        for (j in 1..2) {
            try {
                try {
                    break out
                    assert false
                } finally {
                    i += 10
                }
                assert false
            } finally {
                i += 1
            }
            assert false
        }
        assert i == 11
    }

    @Test
    void testBreakInNestedForLoop() {
        def reached = false
        for (i in [1]) {
            for (j in [1]) {
                break
                assert false : 'did not break inner loop'
            }
            reached = true
        }
        assert reached : 'must not break outer loop'
    }

    @Test
    void testBreakLabelInSimpleWhileLoop() {
        label_1: while (true) {
            break label_1
            assert false
        }
    }

    @Test
    void testBreakLabelInNestedWhileLoop() {
        def count = 0
        label: while (count < 1) {
            count += 1
            while (true) {
                break label
                assert false : 'did not break inner loop'
            }
            assert false : 'did not break outer loop'
        }
    }

    @Test
    void testBreakLabelInNestedMixedForAndWhileLoop() {
        def count = 0
        label_1: while (count < 1) {
            count += 1
            for (i in [1]) {
                break label_1
                assert false : 'did not break inner loop'
            }
            assert false : 'did not break outer loop'
        }
        label_2: for (i in [1]) {
            while (true) {
                break label_2
                assert false : 'did not break inner loop'
            }
            assert false : 'did not break outer loop'
        }
    }

    // GROOVY-11739
    @Test
    void testContinueWithinDoWhileLoop() {
        int i = 0
        do {
            i += 1
            if (i > 1000) break // prevent infinite loop
            continue // control should pass to condition
        } while (i < 100)

        assert i == 100
    }

    @Test
    void testContinueInNestedForLoop() {
        def log = ''
        for (i in [1,2]) {
            log += i
            for (j in [3,4]) {
                if (j==3) continue
                log += j
            }
        }
        assert log == '1424'
    }

    @Test
    void testContinueLabelInNestedForLoop() {
        def log = ''
        label: for (i in [1,2]) {
            log += i
            for (j in [3,4]) {
                if (j==4) continue label
                log += j
            }
            log += 'never reached'
        }
        assert log == '1323'
    }

    // GROOVY-3908
    @Test
    void testContinueOutsideOfLoop() {
        shouldFail '''
            continue
        '''
        shouldFail '''
            if (true) continue
        '''
        shouldFail '''
            xx: if (true) continue xx
        '''
        shouldFail '''
            switch (value) {
              case "foobar": continue
            }
        '''
    }

    @Test
    void testBreakToLastLabelSucceeds() {
        one:
        two:
        three:
        for (i in 1..2) {
            break three
            fail()
        }
    }

    @Test
    void testMultipleLabelSupport() {
        def visited = []
        label1:
        label2:
        label3:
        for (int i = 0; i < 9; i++) {
          visited << i
          if (i == 1) continue label1
          visited << 10 + i
          if (i == 3) continue label2
          visited << 100 + i
          if (i == 5) break label3
        }
        assert visited == [0, 10, 100, 1, 2, 12, 102, 3, 13, 4, 14, 104, 5, 15, 105]
    }

    // this is in accordance with Java; Spock Framework relies on this
    @Test
    void testLabelCanOccurMultipleTimesInSameScope() {
        one:
        for (i in 1..2) {
            break one
            fail()
        }
        one:
        for (i in 1..2) {
            break one
            fail()
        }
    }
}

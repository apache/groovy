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

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class BreakContinueLabelTest {

    @Test
    void testDeclareLabels() {
        assertScript '''
            label_1: print('foo')
            label_2:
            print('bar')
        '''
    }

    // GROOVY-7463
    @Test
    void testBreakLabelInIfStatement() {
        assertScript '''
            boolean flag = true
            label:
            if (flag) {
                print('foo')
                if (flag) {
                    break label
                    assert false
                }
                assert false
            } else {
                assert false
            }
            print('bar')
        '''
    }

    // GROOVY-7463
    @Test
    void testBreakLabelInIfStatement2() {
        assertScript '''
            int i = 0
            boolean flag = true
            label:
            if (flag) {
                print('foo')
                try {
                    if (flag) {
                        print('bar')
                        break label;
                        assert false
                    }
                    assert false
                } finally {
                    i += 1
                }
                assert false
            }
            print('baz')
            assert i == 1
        '''
    }

    @Test
    void testBreakLabelInSimpleForLoop() {
        assertScript '''
            label: for (i in [1]) {
                break label;
                assert false
            }
        '''
    }

    // GROOVY-7617
    @Test
    void testBreakLabelInSecondForLoop() {
        def err = shouldFail '''
            @groovy.transform.TimedInterrupt(1)
            void test() {
                first:
                for (i in [1]) {
                    break first // okay
                    assert false
                }
                for (j in [2]) {
                    break first // fail
                    assert false
                }
            }

            test()
        '''
        assert err =~ /cannot break to label 'first' from here/
    }

    @Test
    void testBreakLabelInNestedForLoop() {
        assertScript '''
            label: for (i in [1]) {
                for (j in [1]) {
                    break label;
                    assert false : 'did not break inner loop'
                }
                assert false : 'did not break outer loop'
            }
        '''
    }

    @Test
    void testBreakLabelInForLoopTryFinally() {
        assertScript '''
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
        '''
    }

    @Test
    void testBreakInNestedForLoop() {
        assertScript '''
            def reached = false
            for (i in [1]) {
                for (j in [1]) {
                    break
                    assert false : 'did not break inner loop'
                }
                reached = true
            }
            assert reached : 'must not break outer loop'
        '''
    }

    @Test
    void testBreakLabelInSimpleWhileLoop() {
        assertScript '''
            label: while (true) {
                break label;
                assert false
            }
        '''
    }

    @Test
    void testBreakLabelInNestedWhileLoop() {
        assertScript '''
            def count = 0
            label: while (count < 1) {
                count += 1
                while (true) {
                    break label
                    assert false : 'did not break inner loop'
                }
                assert false : 'did not break outer loop'
            }
        '''
    }

    @Test
    void testBreakLabelInNestedMixedForAndWhileLoop() {
        assertScript '''
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
        '''
    }

    // GROOVY-6844
    @Test
    void testBreakLabelInForLoopWithinBlockStatement() {
        assertScript '''
            int i = 0
            out: {
                i += 1
                for (j in 1..3) {
                    try {
                        i += 10
                        break out
                        assert false : 'did not break try properly'
                    } finally {
                        i += 100
                    }
                    assert false : 'did not break loop properly'
                }
                assert false : 'did not break block properly'
            }
            assert i == 111
        '''
    }

    // GROOVY-11739
    @Test
    void testContinueWithinDoWhileLoop() {
        assertScript '''
            int i = 0
            do {
                i += 1
                if (i > 1000) break // prevent infinite loop
                continue // control should pass to condition
            } while (i < 100)

            assert i == 100
        '''
    }

    @Test
    void testContinueInNestedForLoop() {
        assertScript '''
            def log = ''
            for (i in [1,2]) {
                log += i
                for (j in [3,4]) {
                    if (j==3) continue
                    log += j
                }
            }
            assert log == '1424'
        '''
    }

    @Test
    void testContinueLabelInNestedForLoop() {
        assertScript '''
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
        '''
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
        assertScript '''
            one:
            two:
            three:
            for (i in 1..2) {
                break three
                assert false
            }
        '''
    }

    @Test
    void testMultipleLabelSupport() {
        assertScript '''
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
        '''
    }

    // this is in accordance with Java; Spock Framework relies on this
    @Test
    void testLabelCanOccurMultipleTimesInSameScope() {
        assertScript '''
            one:
            for (i in 1..2) {
                break one
                assert false
            }
            one:
            for (i in 1..2) {
                break one
                assert false
            }
        '''
    }
}

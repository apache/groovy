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

import gls.CompilableTestSupport

/**
 * todo: add BreakContinueLabelWithClosureTest (when break is used to return from a Closure)
 */
class BreakContinueLabelTest extends CompilableTestSupport {

    void testDeclareSimpleLabel() {
        label_1: assert true
        label_2:
        assert true
    }
    void testBreakLabelInSimpleForLoop() {
        def err = shouldFail '''\
            label_1: for (i in [1]) {
                break label_1
                assert false
            }
        '''
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 3, column 17\.\s+assert false\s+(.+)/
    }

    void testBreakLabelInNestedForLoop() {
        def err = shouldFail '''\
            label: for (i in [1]) {
                for (j in [1]){
                    break label
                    assert false, 'did not break inner loop'
                }
                assert false, 'did not break outer loop'
            }
        '''
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 4, column 21\.\s+assert false, 'did not break inner loop'\s+(.+)/
    }

    void testUnlabelledBreakInNestedForLoop() {
        def err = shouldFail '''\
            def reached = false
            for (i in [1]) {
                for (j in [1]){
                    break
                    assert false, 'did not break inner loop'
                }
                reached = true
            }
            assert reached, 'must not break outer loop'
        '''
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 5, column 21\.\s+assert false, 'did not break inner loop'\s+(.+)/
    }

    void testBreakLabelInSimpleWhileLoop() {
        def err = shouldFail '''\
            label_1: while (true) {
                break label_1
                assert false
            }
        '''
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 3, column 17\.\s+assert false\s+(.+)/
    }

    void testBreakLabelInNestedWhileLoop() {
        def err = shouldFail '''\
            def count = 0
            label: while (count < 1) {
                count++
                while (true){
                    break label
                    assert false, 'did not break inner loop'
                }
                assert false, 'did not break outer loop'
            }
        '''
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 6, column 21\.\s+assert false, 'did not break inner loop'\s+(.+)/
    }

    void testBreakLabelInNestedMixedForAndWhileLoop() {
        def err = shouldFail '''\
            def count = 0
            label_1: while (count < 1) {
                count++
                for (i in [1]){
                    break label_1
                    assert false, 'did not break inner loop'
                }
                assert false, 'did not break outer loop'
            }
            label_2: for (i in [1]) {
                while (true){
                    break label_2
                    assert false, 'did not break inner loop'
                }
                assert false, 'did not break outer loop'
            }
        '''
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 6, column 21\.\s+assert false, 'did not break inner loop'\s+(.+)/
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 13, column 21\.\s+assert false, 'did not break inner loop'\s+(.+)/
    }

    void testUnlabelledContinueInNestedForLoop() {
        def log = ''
        for (i in [1,2]) {
            log += i
            for (j in [3,4]){
                if (j==3) continue
                log += j
            }
        }
        assertEquals '1424',log
    }

    void testContinueLabelInNestedForLoop() {
        def log = ''
        label: for (i in [1,2]) {
            log += i
            for (j in [3,4]){
                if (j==4) continue label
                log += j
            }
            log += 'never reached'
        }
        assertEquals '1323',log
    }

    void testBreakToLastLabelSucceeds() {
        def err = shouldFail '''\
            one:
            two:
            three:
            for (i in 1..2) {
                break three
                assert false
            }
        '''
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 6, column 17\.\s+assert false\s+(.+)/
    }

    void testMultipleLabelSupport() {
        assertScript """
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
        """
    }

    // this is in accordance with Java; Spock Framework relies on this
    void testLabelCanOccurMultipleTimesInSameScope() {
        def err = shouldFail '''\
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
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 4, column 17\.\s+assert false\s+(.+)/
        assert err ==~ /(?s)(.+)\s+Unreachable statement found\s+@ line 9, column 17\.\s+assert false\s+(.+)/
    }
}

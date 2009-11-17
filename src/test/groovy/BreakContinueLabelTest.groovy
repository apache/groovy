package groovy

import gls.CompilableTestSupport

/**
 * todo: add BreakContinueLabelWithClosureTest (when break is used to return from a Closure)

 * @author Dierk Koenig
 */
class BreakContinueLabelTest extends CompilableTestSupport {

    void testDeclareSimpleLabel() {
        label_1: assert true
        label_2:
        assert true
    }
    void testBreakLabelInSimpleForLoop() {
        label_1: for (i in [1]) {
            break label_1
            assert false
        }
    }

    void testBreakLabelInNestedForLoop() {
        label: for (i in [1]) {
            for (j in [1]){
                break label
                assert false, 'did not break inner loop'
            }
            assert false, 'did not break outer loop'
        }
    }

    void testUnlabelledBreakInNestedForLoop() {
        def reached = false
        for (i in [1]) {
            for (j in [1]){
                break
                assert false, 'did not break inner loop'
            }
            reached = true
        }
        assert reached, 'must not break outer loop'
    }

    void testBreakLabelInSimpleWhileLoop() {
        label_1: while (true) {
            break label_1
            assert false
        }
    }

    void testBreakLabelInNestedWhileLoop() {
        def count = 0
        label: while (count < 1) {
            count++
            while (true){
                break label
                assert false, 'did not break inner loop'
            }
            assert false, 'did not break outer loop'
        }
    }

    void testBreakLabelInNestedMixedForAndWhileLoop() {
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
        one:
        two:
        three:
        for (i in 1..2) {
            break three
            fail()
        }
    }

    // documents a (minor) limitation of the current implementation compared to Java
    void testBreakToOtherThanLastLabelCausesSyntaxError() {
        shouldNotCompile """
one:
two:
three:
for (i in 1..2) {
  break one
}
            """
    }

    void testContinueToLastLabelSucceeds() {
        one:
        two:
        three:
        for (i in 1..2) {
            continue three
            fail()
        }
    }

    // documents a (minor) limitation of the current implementation compared to Java
    void testContinueToOtherThanLastLabelCausesSyntaxError() {
        shouldNotCompile """
one:
two:
three:
for (i in 1..2) {
  continue two
}
        """
    }

    // this is in accordance with Java; Spock Framework relies on this
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
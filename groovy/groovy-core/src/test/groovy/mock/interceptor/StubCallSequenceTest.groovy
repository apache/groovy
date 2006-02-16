package groovy.mock.interceptor

import junit.framework.AssertionFailedError

/**
    Testing Groovy Stub support for multiple calls to the Collaborator with
    demanding one or two methods multiple and and various ranges.
    @author Dierk Koenig
*/
class StubCallSequenceTest extends GroovyTestCase {

    StubFor stub

    void setUp() {
        stub = new StubFor(Collaborator.class)
    }

    void testUndemandedCallFailsEarly() {
        // no demand here
        stub.use {
            def caller = new Caller()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testOneDemandedTwoCalledFailsEarly() {
        stub.demand.one { 1 }
        stub.use {
            def caller = new Caller()
            caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }
    void testOneDemandedDefaultRange() {
        stub.demand.one(1..1) { 1 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
        }
    }
    void testOneDemandedExactRange() {
        stub.demand.one(2..2) { 1 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }
    void testOneDemandedRealRange() {
        stub.demand.one(1..2) { 1 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }
    void testOneDemandedOptionalRange() {
        stub.demand.one(0..2) { 1 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }
    void testTwoDemandedNoRange() {
        stub.demand.one() { 1 }
        stub.demand.two() { 2 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }
    void testTwoDemandedFirstRangeExploited() {
        stub.demand.one(1..2) { 1 }
        stub.demand.two() { 2 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }
    void testTwoDemandedFirstRangeNotExploited() {
        stub.demand.one(1..2) { 1 }
        stub.demand.two() { 2 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }
    void testTwoDemandedFirstOptionalOmitted() {
        stub.demand.one(0..2) { 1 }
        stub.demand.two() { 2 }
        stub.use {
            def caller = new Caller()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }
    void testMixedDemandedMinimumOutOfSequence() {
        stub.demand.one(0..1) { 1 }
        stub.demand.two() { 2 }
        stub.demand.one() { 1 }
        stub.demand.two(0..2) { 2 }
        stub.demand.one() { 1 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()

            assertEquals 2, caller.collaborateTwo()
            assertEquals 2, caller.collaborateTwo()
        }
        stub.expect.verify()
    }
    void testMixedDemandedMaximum() {
        stub.demand.one(0..1) { 1 }
        stub.demand.two() { 2 }
        stub.demand.one() { 1 }
        stub.demand.two(0..2) { 2 }
        stub.demand.one() { 1 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            assertEquals 1, caller.collaborateOne()

            // 2.times( assertEquals(2, caller.collaborateTwo()) ) // todo: why this not possible?
            assertEquals 2, caller.collaborateTwo()
            assertEquals 2, caller.collaborateTwo()

            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }
    void testMixedDemandedOutOfSequenceFailsEarly() {
        stub.demand.one(0..1) { 1 }
        stub.demand.two() { 2 }
        stub.demand.one() { 1 }
        stub.demand.two(0..2) { 2 }
        stub.demand.one() { 1 }
        shouldFail(AssertionFailedError.class) { // fails on verify
            stub.use {
                def caller = new Caller()
                assertEquals 1, caller.collaborateOne()
                assertEquals 2, caller.collaborateTwo()
                shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
            }
        }
    }
    void testRangeDemandedOutOfSequenceCalls() {
        stub.demand.one(0..3) { 1 }
        stub.demand.two(0..3) { 2 }
        stub.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
        stub.expect.verify()
    }

    void testUnreachedDemandFailsOnVerify() {
        stub.demand.one { 1 }
        // nothing used
        shouldFail(AssertionFailedError.class) { stub.expect.verify() }
    }


    void testRangeDemandedButNotExploitedFailsOnVerify() {
        stub.demand.one(2..4) { 1 }
        shouldFail(AssertionFailedError.class) { // fails on verify
            stub.use {
                def caller = new Caller()
                assertEquals 1, caller.collaborateOne()
            }
            stub.expect.verify()
        }
    }
    void testReversedRangesNotAllowed() {
        shouldFail(IllegalArgumentException.class) { stub.demand.one(1..0) { 1 } }
    }

}
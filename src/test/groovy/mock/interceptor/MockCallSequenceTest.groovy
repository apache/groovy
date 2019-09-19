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
package groovy.mock.interceptor

import groovy.test.GroovyTestCase
import junit.framework.AssertionFailedError

/**
 * Testing Groovy Mock support for multiple calls to the Collaborator with
 * demanding one or two methods multiple and and various ranges.
 */
class MockCallSequenceTest extends GroovyTestCase {

    MockFor mocker

    void setUp() {
        mocker = new MockFor(Collaborator.class)
    }

    void testUndemandedCallFailsEarly() {
        // no demand here
        mocker.use {
            def caller = new Caller()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testOneDemandedTwoCalledFailsEarly() {
        mocker.demand.one { 1 }
        mocker.use {
            def caller = new Caller()
            caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testOneDemandedDefaultRange() {
        mocker.demand.one(1..1) { 1 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
        }
    }

    void testOneDemandedExactRange() {
        mocker.demand.one(2..2) { 1 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testOneDemandedExactRangeShorthand() {
        mocker.demand.one(2) { 1 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testOneDemandedRealRange() {
        mocker.demand.one(1..2) { 1 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testOneDemandedOptionalRange() {
        mocker.demand.one(0..2) { 1 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testTwoDemandedNoRange() {
        mocker.demand.one() { 1 }
        mocker.demand.two() { 2 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }

    void testTwoDemandedFirstRangeExploited() {
        mocker.demand.one(1..2) { 1 }
        mocker.demand.two() { 2 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }

    void testTwoDemandedFirstRangeNotExploited() {
        mocker.demand.one(1..2) { 1 }
        mocker.demand.two() { 2 }
        mocker.use {
            def caller = new Caller()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }

    void testTwoDemandedFirstOptionalOmitted() {
        mocker.demand.one(0..2) { 1 }
        mocker.demand.two() { 2 }
        mocker.use {
            def caller = new Caller()
            assertEquals 2, caller.collaborateTwo()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
        }
    }

    void testMixedDemandedMinimum() {
        mocker.demand.one(0..1) { 1 }
        mocker.demand.two() { 2 }
        mocker.demand.one() { 1 }
        mocker.demand.two(0..2) { 2 }
        mocker.demand.one() { 1 }
        mocker.use {
            def caller = new Caller()
            assertEquals 2, caller.collaborateTwo()
            assertEquals 1, caller.collaborateOne()
            assertEquals 1, caller.collaborateOne()
            shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
            shouldFail(AssertionFailedError.class) { caller.collaborateOne() }
        }
    }

    void testMixedDemandedMaximum() {
        mocker.demand.one(0..1) { 1 }
        mocker.demand.two() { 2 }
        mocker.demand.one() { 1 }
        mocker.demand.two(0..2) { 2 }
        mocker.demand.one() { 1 }
        mocker.use {
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
        mocker.demand.one(0..1) { 1 }
        mocker.demand.two() { 2 }
        mocker.demand.one() { 1 }
        mocker.demand.two(0..2) { 2 }
        mocker.demand.one() { 1 }
        shouldFail(AssertionFailedError.class) { // fails on verify
            mocker.use {
                def caller = new Caller()
                assertEquals 1, caller.collaborateOne()
                assertEquals 2, caller.collaborateTwo()
                shouldFail(AssertionFailedError.class) { caller.collaborateTwo() }
            }
        }
    }

    void testRangeDemandedButNotExploitedFailsOnVerify() {
        mocker.demand.one(2..4) { 1 }
        shouldFail(AssertionFailedError.class) { // fails on verify
            mocker.use {
                def caller = new Caller()
                assertEquals 1, caller.collaborateOne()
            }
        }
    }

    void testIgnoreString() {
        mocker.demand.one() { 1 }
        mocker.ignore('two') { 2 }
        mocker.use {
            def caller = new Caller()
            assertEquals 2, caller.collaborateTwo()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
        }
    }

    void testIgnorePattern() {
        mocker.demand.one() { 1 }
        mocker.ignore('baz') { 99 }
        mocker.ignore(~'t.o') { 2 }
        mocker.use {
            def caller = new Caller()
            assertEquals 2, caller.collaborateTwo()
            assertEquals 1, caller.collaborateOne()
            assertEquals 2, caller.collaborateTwo()
        }
    }

    void testReversedRangesNotAllowed() {
        shouldFail(IllegalArgumentException.class) { mocker.demand.one(1..0) { 1 } }
    }

}
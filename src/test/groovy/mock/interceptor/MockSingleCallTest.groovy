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
 * Testing Groovy Mock support for single calls to the Collaborator with
 * no, one, multiple, or arbitrary arguments, exceptions and failures.
 */
class MockSingleCallTest extends GroovyTestCase {

    MockFor mocker

    void setUp() {
        mocker = new MockFor(Collaborator.class)
    }

    void testMockGetter() {
        mocker.demand.getFoo { "foo" }
        mocker.demand.getFoo { "foobar" }
        mocker.use {
            assertEquals "foo", new Caller().callFoo1()
            assertEquals "foobar", new Caller().callFoo2()
        }
    }

    void testMockSetter() {

        def result = null

        mocker.demand.setBar { result = it }
        mocker.demand.setBar { result = it }

        mocker.use {
            new Caller().setBar1()
            assertEquals result, "bar1"
            new Caller().setBar2()
            assertEquals result, "bar2"

        }
    }

    void testSingleCallNoArgs() {
        mocker.demand.one { 1 }
        mocker.use {
            assertEquals 1, new Caller().collaborateOne()
        }
    }

    void testSingleCallOneArg() {
        mocker.demand.one { arg -> return arg }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(2)
        }
    }

    void testSingleCallTwoArgs() {
        mocker.demand.one { one, two -> return one + two }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(1, 1)
        }
    }

    void testNoSingleCallTwoArgsWhenNoArgDemanded() {
        mocker.demand.one { 2 }
        mocker.use {
            shouldFail {
                assertEquals 2, new Caller().collaborateOne(1, 1)
            }
        }
    }

    void testSingleCallTwoArgsWhenArbitraryArgsDemanded() {
        mocker.demand.one { Object[] arg -> 2 }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(1, 1)
        }
    }

    void testSingleCallTwoArgsWhenDefaultArgsDemanded() {
        mocker.demand.one { one = null, two = null -> 2 }
        mocker.use {
            assertEquals 2, new Caller().collaborateOne(1, 1)
        }
    }

    void testVerifyFailsIfOneDemandedButNoneExcecuted() {
        mocker.demand.one { 1 }
        def msg = shouldFail(AssertionFailedError.class) {
            mocker.use {
                // no call
            }
        }
        /* This is a fragile test smell! We've changed the message text of the exception, and this test fails due to this assert.
           If we think this assert is important, we should extend AssertionFailedError and add the properties
           expectedRange and callCount to it. But I think the test is good enough with just checking for the thrown
           exception. */
        // assert msg =~ /0.*1..1.*never called/ 
    }

    void testFirstOptionalOmitted() {
        mocker.demand.one(0..1) { 1 }
        mocker.use {
            def caller = new Caller()
        }
        // Getting here means no exception, which is what we want to test.  (Fix for GROOVY-2309)
    }

    void testSingleCallExceptionDemanded() {
        mocker.demand.one { throw new IllegalArgumentException() }
        mocker.use {
//            shouldFail(IllegalArgumentException.class) {
            shouldFail { // todo: should fail with IllegalArgumentException instead of GroovyRuntimeException
                new Caller().collaborateOne()
            }
        }
    }

    void testSingleCallFailDemanded() {
        mocker.demand.one { fail 'just kidding' }
        mocker.use {
            shouldFail() { new Caller().collaborateOne() }
        }
    }

    void testJavaCall() {
        mocker = new MockFor(String.class)
        mocker.demand.toString { 'groovy' }
        mocker.use {
            assertEquals 'groovy', new Caller().collaborateJava()
        }
    }

}




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
package groovy.lang

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.fail
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertSame


/**
 * Tests the .tap method
 */
class TapMethodTest {

    @Test
    void testTapReturnsSelf() {
        def m1 = [:]
        def m2 = m1.tap{
            put("a",1)
        }
        assertSame(m1, m2, "Outgoing object of tap is not the same as the ingoing")
        assertEquals(m2, [a: 1], "Outgoing object of tap is changed")
    }

    @Test
    void testDelegateGetsFirstOpportunity() {
        def sb = new StringBuffer()

        sb.tap {
            // this should call append() on the
            // delegate not, the owner
            append 'some text'
        }

        assertEquals 'some text', sb.toString(), 'delegate had wrong value'
    }

    @Test
    void testOwnerGetsOpportunityIfDelegateCannotRespond() {
        def sb = new StringBuffer()

        def returnValue

        sb.tap {
            // this should call ownerMethod() on the owner
            returnValue = ownerMethod()
        }

        assertEquals 42,
                     returnValue,
                     'owner should have responded to method call'
    }

    @Test
    void testCallingNonExistentMethod() {
        def sb = new StringBuffer()

        shouldFail(MissingMethodException) {
            sb.tap {
                someNoneExistentMethod()
            }
        }
    }

    @Test
    void testClosureWithResolveStrategyExplicitlySet() {
        def closure = {
            append 'some text'
        }
        closure.resolveStrategy = Closure.OWNER_ONLY

        def sb = new StringBuffer()

        // .tap should use DELEGATE_FIRST, even though
        // the closure has another strategy set
        sb.tap closure

        assertEquals 'some text', sb.toString(), 'delegate had wrong value'
    }

    def ownerMethod() {
        42
    }

    void append(String s) {
        fail 'this should never have been called'
    }
}

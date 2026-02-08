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
package testingguide

import groovy.test.GroovyTestCase
import groovy.test.NotYetImplemented

class GroovyTestCaseExampleTests extends GroovyTestCase {

/*  // tag::assertions[]
class MyTestCase extends GroovyTestCase {
    // end::assertions[]
*/
    // tag::assertions[]

    void testAssertions() {
        assertTrue(1 == 1)
        assertEquals("test", "test")

        def x = "42"
        assertNotNull "x must not be null", x
        assertNull null

        assertSame x, x
    }

    // end::assertions[]

/*  // tag::assertions[]
}
    // end::assertions[]
*/

    // tag::assertScript[]
    void testScriptAssertions() {
        assertScript '''
            def x = 1
            def y = 2

            assert x + y == 3
        '''
    }
    // end::assertScript[]

    // tag::should_fail_without_class[]
    void testInvalidIndexAccess1() {
        def numbers = [1,2,3,4]
        shouldFail {
            numbers.get(4)
        }
    }
    // end::should_fail_without_class[]

    // tag::should_fail_with_class[]
    void testInvalidIndexAccess2() {
        def numbers = [1,2,3,4]
        shouldFail IndexOutOfBoundsException, {
            numbers.get(4)
        }
    }
    // end::should_fail_with_class[]

    // tag::should_fail_with_msg[]
    void testInvalidIndexAccess3() {
        def numbers = [1,2,3,4]
        def msg = shouldFail IndexOutOfBoundsException, {
            numbers.get(4)
        }
        assert msg.contains('Index: 4, Size: 4') ||
            msg.contains('Index 4 out-of-bounds for length 4') ||
            msg.contains('Index 4 out of bounds for length 4')
    }
    // end::should_fail_with_msg[]

    // tag::not_yet_implemented[]
    void testNotYetImplemented1() {
        if (notYetImplemented()) return   // <1>

        assert 1 == 2                     // <2>
    }
    // end::not_yet_implemented[]

    // tag::not_yet_implemented_ast[]
    @NotYetImplemented
    void testNotYetImplemented2() {
        assert 1 == 2
    }
    // end::not_yet_implemented_ast[]

}

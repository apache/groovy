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
// tag::junit4_example[]
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

class JUnit4ExampleTests {

    @Test
    void indexOutOfBoundsAccess() {
        def numbers = [1,2,3,4]
        shouldFail {
            numbers.get(4)
        }
    }

    // end::junit4_example[]

    // tag::should_fail_return[]
    @Test
    void shouldFailReturn() {
        def e = shouldFail {
            throw new RuntimeException('foo',
                                       new RuntimeException('bar'))
        }
        assert e instanceof RuntimeException
        assert e.message == 'foo'
        assert e.cause.message == 'bar'
    }
    // end::should_fail_return[]

    // tag::junit4_example[]
}
// end::junit4_example[]

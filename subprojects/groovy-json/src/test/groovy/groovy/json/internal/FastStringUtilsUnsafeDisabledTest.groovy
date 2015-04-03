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
package groovy.json.internal

class FastStringUtilsUnsafeDisabledTest extends GroovyTestCase {

    FastStringUtils.StringImplementation oldStringImplementation

    void setUp() {
        // to disable Unsafe usage, we set the StringImplementation to the very safe UNKNOWN

        oldStringImplementation = FastStringUtils.STRING_IMPLEMENTATION
        FastStringUtils.STRING_IMPLEMENTATION = FastStringUtils.StringImplementation.UNKNOWN
    }

    @Override
    void tearDown() {
        FastStringUtils.STRING_IMPLEMENTATION = oldStringImplementation
    }

    void testToCharArray() {
        synchronized (FastStringUtils) {
            def str = "some test"
            assert !FastStringUtils.toCharArray(str).is(str.value)
        }
    }

    void testToCharArrayWithStringBuilder() {
        synchronized (FastStringUtils) {
            def str = new StringBuilder().append("some test")
            // StringBuilder#toString() returns a new String object
            assert FastStringUtils.toCharArray(str) == "some test".toCharArray()
        }
    }

    void testNoCopyStringFromChars() {
        synchronized (FastStringUtils) {
            def source = "äöüliu"
            def chars = source.toCharArray()

            assert FastStringUtils.noCopyStringFromChars(chars) == source
        }
    }
}

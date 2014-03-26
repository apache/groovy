/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.json.internal

class FastStringUtilsTest extends GroovyTestCase {

    void testToCharArray() {
        synchronized (FastStringUtils) {
            def str = "some test"
            // FastStringUtils accesses the underlying char array directly
            assert FastStringUtils.toCharArray(str).is(str.value) : FastStringUtils.STRING_IMPLEMENTATION.toString()
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

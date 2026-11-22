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
package bugs

import org.codehaus.groovy.runtime.powerassert.PowerAssertionError
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-8151: a failed {@code assert} comparing a literal to a method call
 * used not to show the right-hand value in the power-assert output. The
 * rendered message must include the value returned by the method.
 */
final class Groovy8151 {

    @Test
    void testAssertShowsMethodCallResult() {
        def err = shouldFail(PowerAssertionError, '''
            class Test {
                static void main(String[] args) {
                    assert 'test' == transform('test')
                }
                static String transform(String str) {
                    str + 'aaa'
                }
            }
        ''')
        assert err.message.contains('testaaa')
        assert err.message.contains("assert 'test' == transform('test')")
    }

    @Test
    void testAssertShowsResultWithToString() {
        def err = shouldFail(PowerAssertionError, '''
            class Test {
                static void main(String[] args) {
                    assert 'test' == transform('test').toString()
                }
                static String transform(String str) {
                    str + 'aaa'
                }
            }
        ''')
        assert err.message.contains('testaaa')
    }

    @Test
    void testAssertShowsResultOfStaticallyImportedMethod() {
        def err = shouldFail(PowerAssertionError, '''
            import static bugs.Groovy8151Helper.transform
            assert 'test' == transform('test')
        ''')
        assert err.message.contains('testaaa')
    }
}

class Groovy8151Helper {
    static String transform(String str) {
        str + 'aaa'
    }
}

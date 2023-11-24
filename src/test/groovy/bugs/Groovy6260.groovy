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
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

final class Groovy6260 {

    @Test
    void testErrorMessage1() {
        def err = shouldFail '''
            class Foo {
                def getBar() {}
            }

            def foo = new Foo()
            foo.bar = 123
        '''
        assert err.message == 'Cannot set read-only property: bar for class: Foo'
    }

    @Test
    void testErrorMessage2() {
        def err = shouldFail '''
            class Foo {
                void setBar(bar) {}
            }

            def foo = new Foo()
            foo.bar
        '''
        assert err.message == 'Cannot get write-only property: bar for class: Foo'
    }

    // GROOVY-8064
    @Test
    void testErrorMessage3() {
        def err = shouldFail '''
            new File('/tmp').lastModified
        '''
        assert err.message == 'Cannot get write-only property: lastModified for class: java.io.File'
    }

    @Test
    void testErrorMessage4() {
        def err = shouldFail '''
            class Foo {
                def    getBar() {}
                static getBaz() {}
            }

            Foo.bar
        '''
        assert err.message == 'No such property: bar for class: Foo\nPossible solutions: baz'
    }
}

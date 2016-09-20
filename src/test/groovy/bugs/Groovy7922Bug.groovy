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
package groovy.bugs;

import gls.CompilableTestSupport;

public class Groovy7922Bug extends CompilableTestSupport {

    void testMethodSelection() {
        def message = shouldNotCompile '''
            import groovy.transform.CompileStatic

            interface FooA {}
            interface FooB {}
            class FooAB implements FooA, FooB {}
            @CompileStatic
            class TestGroovy {
                static void test() { println new TestGroovy().foo(new FooAB()) }
                def foo(FooB x) { 43 }
                def foo(FooA x) { 42 }
            }

            TestGroovy.test()
        ''';

        assert message.contains("ambiguous")

        shouldCompile '''
            import groovy.transform.CompileStatic

            interface FooA {}
            interface FooB {}
            class FooAB implements FooA, FooB {}
            @CompileStatic
            class TestGroovy {
                static void test() { println new TestGroovy().foo((FooA)null) }
                def foo(FooB x) { 43 }
                def foo(FooA x) { 42 }
            }

            TestGroovy.test()
        '''
    }
}

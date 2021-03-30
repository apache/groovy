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
package gls.scope

import gls.CompilableTestSupport

class MultipleDefinitionOfSameVariableTest extends CompilableTestSupport {

    void testInSameBlock() {
        shouldNotCompile '''
            def foo = 1
            def foo = 2
        '''

        shouldNotCompile '''
            class Foo {
                def foo() {
                    def bar=1
                    def bar=2
                }
            }
        '''
    }

    void testInSubBlocks() {
        shouldNotCompile '''
             def foo = 1
             5.times { def foo=2 }
        '''

        shouldNotCompile '''
            def foo = 1
            label1: { def foo=2 }
        '''

        shouldNotCompile '''
            def foo = 1
            for (i in []) { def foo=2 }
        '''

        shouldNotCompile '''
            def foo = 1
            while (true) { def foo=2 }
        '''
    }

    void testInNestedClosure() {
        shouldNotCompile '''
            def foo = 1
            5.times { 6.times {def foo=2 }
        '''

        assertScript '''
            def foo = 1
            5.times { 6.times {foo=2 } }
            assert foo == 2
        '''
    }

    void testBindingHiding() {
        assertScript '''
            foo = 1
            def foo = 3
            assert foo==3
            assert this.foo == 1
            assert binding.foo == 1
        '''
    }

    void testBindingAccessInMethod() {
        assertScript '''
            def methodUsingBinding() {
                try {
                    s = "  bbb  ";
                } finally {
                    s = s.trim();
                }
                assert s == "bbb"
            }
            methodUsingBinding()
            assert s == "bbb"
        '''
    }

    void testMultipleOfSameName() {
        shouldNotCompile '''
            class DoubleField {
                def zero = 0
                def zero = 0
            }
        '''
    }

    void testPropertyField() {
        shouldCompile '''
            class A {
                def foo
                private foo
            }
        '''
    }

    void testPropertyFieldBothInit() {
        shouldNotCompile '''
            class A {
                def foo = 3
                private foo = 4
            }
        '''
    }

    void testFieldProperty() {
        shouldCompile '''
            class A {
                private foo
                def foo
            }
        '''
    }

    void testFieldPropertyBothInit() {
        shouldNotCompile '''
            class A {
                private foo = 'a'
                def foo = 'b'
            }
        '''
    }

    void testFieldPropertyProperty() {
        shouldNotCompile '''
            class A {
                private foo
                def foo
                def foo
            }
        '''
    }

    void testPropertyFieldField() {
        shouldNotCompile '''
            class A {
                def foo
                private foo
                private foo
            }
        '''
    }

    void testFieldAndPropertyWithInit() {
        assertScript '''
            class X {
                def foo = 3
                public foo
                public bar
                def bar = 4
            }

            def x = new X()
            def result = [x.foo, x.bar]
            assert result == [3, 4]
        '''
    }

    void testPropertyAndFieldWithInit() {
        assertScript '''
            class Y {
                def foo
                public foo = 5
                public bar = 6
                def bar
            }

            def y = new Y()
            result = [y.foo, y.bar]
            assert result == [5, 6]
        '''
    }

}
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

import groovy.test.GroovyTestCase

class Groovy7938Bug extends GroovyTestCase {
    void testClassUsageInInterfaceDef() {
        assertScript """
            class Outer {
                static Integer fooCount = 0
                Integer barCount = 0
                static void incFoo() { fooCount++ }
                void incBar() { barCount++ }
                static class Nested {
                    static void nestedIncFoo() { incFoo() }
                    static class NestedNested {
                        static void nestedNestedIncFoo() { incFoo() }
                    }
                }
                Inner innerFactory() { new Inner() }
                class Inner {
                    InnerInner innerInnerFactory() { new InnerInner() }
                    void innerIncFoo() { incFoo() }
                    static void staticInnerIncFoo() { incFoo() }
                    class InnerInner {
                        void innerInnerIncFoo() { incFoo() }
                        static void staticInnerInnerIncFoo() { incFoo() }
                    }
                }
            }
            Outer.incFoo()
            Outer.Nested.nestedIncFoo()
            Outer.Nested.NestedNested.nestedNestedIncFoo()
            assert Outer.fooCount == 3
            new Outer().with {
                incBar()
                incFoo()
                innerFactory().with {
                    incBar()
                    innerIncFoo()
                    staticInnerIncFoo()
                    innerInnerFactory().with {
                        incBar()
                        innerInnerIncFoo()
                        staticInnerInnerIncFoo()
                    }
                }
                assert barCount == 3
                assert fooCount == 8
            }
        """
    }
}

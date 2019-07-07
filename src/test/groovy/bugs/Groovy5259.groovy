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

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy5259 {

    @Test
    void testInnerClassAccessingOuterClassConstant() {
        assertScript '''
            class InnerAccessOuter {
                static final String OUTER_CONSTANT = 'Constant Value'

                class InnerClass {
                    InnerClass() {
                    }

                    String innerCompiled() {
                        OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass()
                    assert OUTER_CONSTANT == inner.innerCompiled()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''
    }

    @Test
    void testInnerClassWithWrongCallToSuperAccessingOuterClassConstant() {
        shouldFail '''
            class InnerAccessOuter {
                protected static final String OUTER_CONSTANT = 'Constant Value'

                class InnerClass {
                    InnerClass() {
                        // there's no Object#<init>(String) method, but it throws a VerifyError when a new instance
                        // is created, meaning a wrong super call is generated
                        super(OUTER_CONSTANT)
                    }
                    String m() {
                         OUTER_CONSTANT
                    }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass()
                    inner.m()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''
    }

    @Test
    void testInnerClassWithSuperClassAccessingOuterClassConstant() {
        assertScript '''
            class Base {
                Base(String str) {}
            }
            class InnerAccessOuter {
                static final String OUTER_CONSTANT = 'Constant Value'

                class InnerClass extends Base {
                    InnerClass() {
                        super(OUTER_CONSTANT)
                    }

                    String innerCompiled() { OUTER_CONSTANT }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass() // throws a VerifyError
                    assert OUTER_CONSTANT == inner.innerCompiled()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''
    }

    @Test
    void testInnerClassWithSuperClassAccessingSuperOuterClassConstant() {
        assertScript '''
            class Base {
                Base(String str) {}
            }
            class OuterBase {
                protected static final String OUTER_CONSTANT = 'Constant Value'
            }
            class InnerAccessOuter extends OuterBase {

                class InnerClass extends Base {
                    InnerClass() {
                        super(OUTER_CONSTANT)
                    }

                    String innerCompiled() { OUTER_CONSTANT }
                }

                void testInnerClassAccessOuter() {
                    def inner = new InnerClass() // throws a VerifyError
                    assert OUTER_CONSTANT == inner.innerCompiled()
                }
            }
            new InnerAccessOuter().testInnerClassAccessOuter()
        '''
    }
}

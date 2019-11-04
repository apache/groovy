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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

final class Groovy7300 extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testShouldNotThrowStackOverflow() {
        assertScript '''
            class A {
                private String field1 = 'test'

                String getField1() {
                    return this.field1
               }
            }

            class B extends A {
                @Override
                String getField1() {
                    super.field1
                }
            }

            B b = new B()

            assert b.field1 == 'test'
        '''
    }

    void testShouldNotThrowStackOverflowWithSuper() {
        assertScript '''
            class A {
                private String field1 = 'test'

                void setField1(String val) { field1 = val }

                String getField1() {
                    return this.field1
               }
            }

            class B extends A {
                @Override
                String getField1() {
                    super.field1 = 'test 2'
                    super.field1
                }
            }

            B b = new B()

            assert b.field1 == 'test 2'
        '''
    }
}

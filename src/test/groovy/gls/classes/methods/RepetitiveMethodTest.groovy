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
package gls.classes.methods

import gls.CompilableTestSupport

class RepetitiveMethodTest extends CompilableTestSupport {

    void testRepetitiveMethod() {
        def message = shouldNotCompile('''
            class A  {
                void foo() {}
                void foo() {}
            }
        ''')
        assert message.contains('duplicates another method of the same signature')
    }

    void testRepetitiveMethodsAllowedForProperties() {
        shouldCompile '''
            class PropertyOverride {
                boolean success
                int num

                boolean isSuccess() {
                    success
                }

                int getNum() {
                    num
                }
            }
        '''
    }

    void testRepetitiveConstructor() {
        def message = shouldNotCompile('''
            class A {
                A(int x) {}
                A(int y) {}
            }
        ''')
        assert message.contains('duplicates another constructor of the same signature')
    }
}

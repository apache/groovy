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

class Groovy4264Bug extends GroovyTestCase {
    void testSubClassHavingMainEntryPoint() {
        try {
            assertScript """
                class B4264 extends A4264 {
                    static main(args){
                        throw new RuntimeException('B4264#main() called correctly')
                    }
                }
                
                class A4264 {}
            """
            fail('B4264 execution should have made the script fail')
        } catch(RuntimeException ex) {
            assert ex.message.contains('B4264#main() called correctly')
        }
    }

    // this scenario used to work correctly. Just added to ensure that there is no change in its behavior
    void testScriptExecutionTakingPreferenceOverClasses() {
        try {
            assertScript """
                class Y4264 extends X4264 {
                    static main(args){
                        throw new RuntimeException('X4264#main() called correctly')
                    }
                }
                
                class X4264 {}
                throw new RuntimeException('Script class executed correctly')
            """
            fail('Script class execution should have caused a failure')
        } catch(RuntimeException ex) {
            assert ex.message.contains('Script class executed correctly')
        }
    }

    // this scenario used to work correctly. Just added to ensure that there is no change in its behavior
    void testExecutionOfJustAScript() {
        try {
            assertScript """
                throw new RuntimeException('Script class executed correctly')
            """
            fail('Script class execution should have caused a failure')
        } catch(RuntimeException ex) {
            assert ex.message.contains('Script class executed correctly')
        }
    }
}

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

class Groovy7620Bug extends GroovyTestCase {
    void testShouldSeeThatMethodIsNotImplemented() {
        def msg = shouldFail '''
            abstract class A {
               abstract Object getFoo()

               void test() {
                   println getFoo()
               }
            }

            class B extends A {
               static Object foo
            }

            new B().test()
            '''

        assert msg.contains("The method 'java.lang.Object getFoo()' is already defined in class 'B'")
    }

    void testShouldSeeConflictInTypeSignature() {
        def msg = shouldFail '''
            interface C {
               Object getFoo()
            }

            class D implements C {
               static Object foo
            }

            new B().test()
            '''

        assert msg.contains("The method 'java.lang.Object getFoo()' is already defined in class 'D'")
    }
}

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

class Groovy4457GenericTypeDeclarationLeakTest extends GroovyTestCase {

    void testLeak() {
        assertScript """
            class A<String> {}

            class B {
                void foo(String s) {}
            }

            // use the name to check the class, since the error was that String was seen as
            // a symbol resolved to Object, not as the class String, thus a ... == String would
            // not have failed
            assert B.declaredMethods.find { it.name == "foo" }.parameterTypes[0].name.contains("String")
        """
    }

    void testLeakWithInnerClass() {
        assertScript """
            class A<String> {
                static class B {
                    void foo(String s) {}
                }
            }

            assert A.B.declaredMethods.find { it.name == "foo" }.parameterTypes[0].name.contains("String")
        """
    }

    void testNonStaticInnerClassGenerics() {
        assertScript '''
            class A<T> {
                void bar(T s) {}
                class B {
                  void foo(T s) {}
                }
            }
            assert A.class.methods.find{it.name=="bar"}.parameterTypes[0].name.contains("java.lang.Object")
            assert A.B.class.methods.find{it.name=="foo"}.parameterTypes[0].name.contains("java.lang.Object")
        '''
    }
}

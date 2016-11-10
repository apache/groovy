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

import gls.CompilableTestSupport

class Groovy4356Bug extends CompilableTestSupport {

    void testDeclareGenericPropertyInNestedClassWithGenericTypeAtNestedLevel() {
        shouldCompile """
            class OuterS1<T1> {
                T1 ofoo
                static class InnerS1<T1> {
                    T1 ifoo
                }
            }
        """
    }

    void testDeclareGenericParameterInStaticMethodWithMethodLevelGenericType() {
        shouldCompile """
            class TestS2 {
                static <T2> void foo(T2 param1){}
            }
        """
    }

    void testDeclareGenericLocalVarInStaticMethodWithMethodLevelGenericType() {
        shouldCompile """
            class TestS3 {
                static <T3> void foo() {
                    T3 localVar1
                }
            }
        """
    }

    void testDeclareGenericPropertyOfInnerClassWithGenericTypeAtOuterLevel() {
        shouldCompile """
            class Outer<T4> {
                T4 ofoo
                class Inner {
                    T4 ifoo
                }
            }
        """
    }

    void testDeclareGenericPropertyOfNestedClassWithGenericTypeAtOuterLevel() {
        shouldNotCompile """
            class Outer<T5> {
                T5 ofoo
                static class Inner {
                    T5 ifoo
                }
            }
        """
    }

    void testDeclareGenericStaticPropertyOfClassWithGenericType() {
        shouldNotCompile """
            class Test1<T6> {
                static T6 f1
            }
        """
    }

    void testDeclareGenericParameterInStaticMethodOfClassWithGenericType() {
        shouldNotCompile """
            class Test2<T7> {
                static foo(T7 param1) {}
            }
        """
    }

    void testDeclareGenericLocalVarInStaticMethodOfClassWithGenericType() {
        shouldNotCompile """
            class Test3<T8> {
                static foo() {
                    T8 localVar1
                }
            }
        """
    }
}

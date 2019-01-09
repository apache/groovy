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
package groovy.transform.stc

/**
 * Unit tests for static type checking : implicit and explicit returns.
 */
class ReturnsSTCTest extends StaticTypeCheckingTestCase {

    void testVoidReturn() {
        shouldFailWithMessages '''
            void method() {
            }

            int x = method()
        ''', 'Cannot assign value of type void to variable of type int'
    }

    void testIncompatibleExplicitReturn() {
        shouldFailWithMessages '''
            String method() {
                return 'String'
            }

            int x = method()
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testIncompatibleExplicitReturn2() {
        shouldFailWithMessages '''
            int method() {
                return 'String'
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testIncompatibleImplicitReturn2() {
        shouldFailWithMessages '''
            int method() {
                'String'
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testIncompatibleImplicitReturn() {
        shouldFailWithMessages '''
            String method() {
                'String'
            }

            int x = method()
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testImplicitReturnFailureWithIfElse() {
        shouldFailWithMessages '''
            int method() {
                if (true) {
                    'String'
                } else {
                    2
                }
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testImplicitReturnFailureWithIfElse2() {
        shouldFailWithMessages '''
            int method() {
                if (true) {
                    2
                } else {
                    'String'
                }
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testImplicitReturnFailureWithIfElse3() {
        shouldFailWithMessages '''
            int method() {
                if (true) {
                    'String'
                } else {
                    'String'
                }
            }
        ''',
            'Cannot return value of type java.lang.String on method returning type int', // first branch
            'Cannot return value of type java.lang.String on method returning type int' // second branch
    }

    void testImplicitReturnFailureWithSwitch() {
         shouldFailWithMessages '''
             int method(int x) {
                 switch (x) {
                    case 1:
                        2
                        break
                    case 2:
                        'String'
                        break
                    default:
                        3
                 }
             }
         ''', 'Cannot return value of type java.lang.String on method returning type int'
    }


   void testImplicitReturnFailureWithSwitch2() {
         assertScript '''
             int method(int x) {
                 switch (x) {
                    case 1:
                        2
                        break
                    case 2:
                        'String'
                    default:
                        3
                 }
             }
         ''' // should not fail because default case overrides case 2 return type
     }

    void testImplicitReturnToString() {
        assertScript '''
            // automatic toString works
            String greeting(String name) {
                def sb = new StringBuilder()
                sb << "Hi" << name
            }
        '''
    }

    void testWrongReturnType() {
        shouldFailWithMessages '''
            double greeting(String name) {
                new Object()
            }
        ''', 'Cannot return value of type java.lang.Object on method returning type double'
    }

    void testRecursiveTypeInferrence() {
        assertScript '''
            def fib(int i) {
                i < 2 ? 1 : (fib(i - 2) as int) + (fib(i - 1) as int)
            }
            fib(2)
        '''
    }

    void testFindMethodWithInferredReturnType() {
        assertScript '''
            def square(int i) { i*i }
            int foo(int i) {
                (Integer)square(i)
            }
            assert foo((Integer)square(2))==16
        '''
    }

    void testReturnTypeInferrenceInSingleClass() {
        assertScript '''
        class Foo {
            int square(int i) { i*i }

            int foo(int i) {
                square(i)
            }
        }
        new Foo().foo(2)
        '''
    }
    
    void testInferredReturnTypeWithImplicitConversion() {
        shouldFailWithMessages '''
            String methodWithImplicitConversion() {
                new Date()
            }
            methodWithImplicitConversion().years
        ''', 'No such property: years for class: java.lang.String'
    }

    // GROOVY-5835
    void testReturnInClosureShouldNotBeConsideredAsReturnOfEnclosingMethod() {
        assertScript '''
            int enclosingMethod() {
                def cl = { return 'String' } // should not think it's a return for the enclosing method
                1
            }
        '''
    }

}


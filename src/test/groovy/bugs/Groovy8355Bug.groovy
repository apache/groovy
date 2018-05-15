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

class Groovy8355Bug extends GroovyTestCase {
    void testGroovy8355() {
        assertScript '''
        import groovy.transform.CompileStatic
        
        @CompileStatic
        class Foo {
            Object str = new Object()
            def bar() {
                str = "str"
                str.toUpperCase()
            }
        }
        
        assert "STR" == new Foo().bar()
        '''
    }

    void test2() {
        assertScript '''
        import groovy.transform.CompileStatic
        
        @CompileStatic
        class Foo {
            Object str = new Object()
            def bar() {
                str = "str"
                str.toUpperCase()
            }
            
            def bar2() {
                str = 1
                str + 2
            }
        }
        
        Foo foo = new Foo()
        assert "STR" == foo.bar()
        assert 3 == foo.bar2()
        '''
    }

    void testTypeInferenceFieldVsLocalVariable() {
        assertScript '''
            // tag::typeinference_field_vs_local_variable[]
            class SomeClass {
                def someUntypedField                                                                // <1>
                String someTypedField                                                               // <2>

                void someMethod() {
                    someUntypedField = '123'                                                        // <3>
                    someUntypedField = someUntypedField.toUpperCase()  // compile-time error        // <4>
                }

                void someSafeMethod() {
                    someTypedField = '123'                                                          // <5>
                    someTypedField = someTypedField.toUpperCase()                                   // <6>
                }

                void someMethodUsingLocalVariable() {
                    def localVariable = '123'                                                       // <7>
                    someUntypedField = localVariable.toUpperCase()                                  // <8>
                }
            }
            // end::typeinference_field_vs_local_variable[]
            SomeClass
        '''
    }

    /*
    void test3() {
        assertScript '''
        import groovy.transform.CompileStatic
        
        @CompileStatic
        class Foo {
            Object str = "str"
            def bar() {
                str.toUpperCase()
            }
        }
        
        assert "STR" == new Foo().bar()
        '''
    }
    */
}

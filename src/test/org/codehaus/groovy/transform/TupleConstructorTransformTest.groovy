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
package org.codehaus.groovy.transform

class TupleConstructorTransformTest extends GroovyShellTestCase {

    void testOk() {
        assertScript """
            import groovy.transform.TupleConstructor

            @TupleConstructor
            class Person {
                String firstName
                String lastName
            }

            def p = new Person("John", "Doe")
            assert p.firstName == "John"
            assert p.lastName == "Doe"
        """
    }

    void testIncludesAndExcludesTogetherResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.TupleConstructor

                @TupleConstructor(includes='surName', excludes='surName')
                class Person {
                    String surName
                }

                new Person("Doe")
            """
        }
        assert message.contains("Error during @TupleConstructor processing: Only one of 'includes' and 'excludes' should be supplied not both.")
    }

    void testIncludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.TupleConstructor

                @TupleConstructor(includes='sirName')
                class Person {
                    String firstName
                    String surName
                }

                def p = new Person("John", "Doe")
            """
        }
        assert message.contains("Error during @TupleConstructor processing: 'includes' property 'sirName' does not exist.")
    }

    void testExcludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.TupleConstructor

                @TupleConstructor(excludes='sirName')
                class Person {
                    String firstName
                    String surName
                }

                def p = new Person("John", "Doe")
            """
        }
        assert message.contains("Error during @TupleConstructor processing: 'excludes' property 'sirName' does not exist.")
    }

    void testIncludesWithEmptyList_groovy7523() {
        assertScript '''
            @groovy.transform.TupleConstructor(includes=[])
            class Cat {
                String name
                int age
            }
            assert Cat.declaredConstructors.size() == 1
        '''
    }

}
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

import groovy.test.GroovyShellTestCase

class EqualsAndHashCodeTransformTest extends GroovyShellTestCase {

    void testOk() {
        assertScript """
            import groovy.transform.EqualsAndHashCode
            @EqualsAndHashCode
            class Person {
                String first, last
                int age
            }

            def p1 = new Person(first:'John', last:'Smith', age:21)
            def p2 = new Person(first:'John', last:'Smith', age:21)
            assert p1 == p2
            def map = [:]
            map[p1] = 45
            assert map[p2] == 45
        """
    }

    void testIncludesAndExcludesTogetherResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.EqualsAndHashCode

                @EqualsAndHashCode(includes='surName', excludes='surName')
                class Person {
                    String surName
                }

                new Person(surName: "Doe")
            """
        }
        assert message.contains("Error during @EqualsAndHashCode processing: Only one of 'includes' and 'excludes' should be supplied not both.")
    }

    void testIncludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.EqualsAndHashCode

                @EqualsAndHashCode(includes='sirName')
                class Person {
                    String surName
                }

                new Person(surName: "Doe")
            """
        }
        assert message.contains("Error during @EqualsAndHashCode processing: 'includes' property 'sirName' does not exist.")
    }

    void testExcludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.EqualsAndHashCode

                @EqualsAndHashCode(excludes='sirName')
                class Person {
                    String firstName
                    String surName
                }

                new Person(firstName: "John", surName: "Doe")
            """
        }
        assert message.contains("Error during @EqualsAndHashCode processing: 'excludes' property 'sirName' does not exist.")
    }

    void testIncludesInternalPropertyNamesIfRequested() {
        assertScript '''
            import groovy.transform.EqualsAndHashCode

            @EqualsAndHashCode(allNames = true)
            class HasInternalNameProperty {
                String $
            }

            def a = new HasInternalNameProperty($: "a")
            def b = new HasInternalNameProperty($: "b")
            assert a != b
            assert a.hashCode() != b.hashCode()
        '''
    }

    void testPrimitiveBooleanPropertiesWithIsGetter_GROOVY6245() {
        assertScript '''
            @groovy.transform.EqualsAndHashCode
            class A {
                boolean a
                boolean isA() { a }
            }

            def a1 = new A(a: true)
            def a2 = new A(a: true)
            def a3 = new A(a: false)
            assert a1 == a2
            assert a1 != a3
        '''
    }

}
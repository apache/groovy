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

    // GROOVY-12726
    void testHandWrittenEqualsAndHashCodeWithHelpersArePreserved() {
        assertScript '''
            import groovy.transform.EqualsAndHashCode
            @EqualsAndHashCode
            class Cell {
                int value
                boolean equals(Object other) { false }
                int hashCode() { 7 }
                private boolean _equals(Object other) { true }
                private int _hashCode() { 9 }
            }
            def cell = new Cell(value: 1)
            assert !cell.equals(cell)
            assert cell.hashCode() == 7
        '''
    }

    // GROOVY-12726
    void testHandWrittenEqualsAndHashCodeDelegateToGeneratedHelpers() {
        assertScript '''
            import groovy.transform.EqualsAndHashCode
            @EqualsAndHashCode
            class Cell {
                int value
                boolean equals(Object other) { _equals(other) }
                int hashCode() { _hashCode() }
            }
            def a = new Cell(value: 1)
            def b = new Cell(value: 1)
            def c = new Cell(value: 2)
            assert a == b
            assert a != c
            assert a.hashCode() == b.hashCode()
            assert a.hashCode() != c.hashCode()
        '''
    }

    // GROOVY-12726
    void testRepeatedAnnotationDoesNotDuplicateGeneratedMethods() {
        assertScript '''
            import groovy.transform.EqualsAndHashCode
            @EqualsAndHashCode
            @EqualsAndHashCode
            class Cell {
                int value
            }
            def a = new Cell(value: 1)
            def b = new Cell(value: 1)
            def c = new Cell(value: 2)
            assert a == b
            assert a != c
            assert a.hashCode() == b.hashCode()
            assert Cell.declaredMethods.count { it.name == 'equals' } == 1
            assert Cell.declaredMethods.count { it.name == 'hashCode' } == 1
            assert Cell.declaredMethods.every { it.name != '_equals' && it.name != '_hashCode' }
        '''
    }

}

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
package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class CanonicalGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitCanonical = parseClass('''@groovy.transform.Canonical
       class ClassUnderTest {
           String name
           int age
       }''')

    final Class<?> explicitCanonical = parseClass('''@groovy.transform.Canonical
       class ClassUnderTest {
           String name
           int age
           ClassUnderTest(String n, int a) { }
           boolean equals(Object o) { false }
           int hashCode() { 42 }
           String toString() { '' }
       }''')

    @Test
    void test_noArg_constructor_is_annotated() {
        assertConstructorIsAnnotated(implicitCanonical)
    }

    @Test
    void test_constructor_is_annotated() {
        assertConstructorIsAnnotated(implicitCanonical, String, int.class)
    }

    @Test
    void test_equals_is_annotated() {
        assertMethodIsAnnotated(implicitCanonical, 'equals', Object)
    }

    @Test
    void test_canEqual_is_annotated() {
        assertMethodIsAnnotated(implicitCanonical, 'canEqual', Object)
    }

    @Test
    void test_hashCode_is_annotated() {
        assertMethodIsAnnotated(implicitCanonical, 'hashCode')
    }

    @Test
    void test_toString_is_annotated() {
        assertMethodIsAnnotated(implicitCanonical, 'toString')
    }

    @Test
    void test_constructor_is_not_annotated() {
        assertConstructorIsNotAnnotated(explicitCanonical, String, int.class)
    }

    @Test
    void test_equals_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitCanonical, 'equals', Object)
    }

    @Test
    void test_hashCode_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitCanonical, 'hashCode')
    }

    @Test
    void test_toString_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitCanonical, 'toString')
    }
}
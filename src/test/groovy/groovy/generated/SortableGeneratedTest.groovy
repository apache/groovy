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
class SortableGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitSortable = parseClass('''@groovy.transform.Sortable
      class ClassUnderTest {
          String name
          int age
      }''')

    final Class<?> explicitSortable = parseClass('''@groovy.transform.Sortable
      class ClassUnderTest {
          String name
          int age
          int compareTo(Object o) { 42 }
          int compareTo(ClassUnderTest o) { 42 }
      }''')

    @Test
    void test_compareTo_is_annotated() {
        assertMethodIsAnnotated(implicitSortable, 'compareTo', Object)
    }

    @Test
    void test_compareTo_with_exact_type_is_annotated() {
        assertMethodIsAnnotated(implicitSortable, 'compareTo', implicitSortable)
    }

    @Test
    void test_static_comparatorByName_is_annotated() {
        assertMethodIsAnnotated(implicitSortable, 'comparatorByName')
    }

    @Test
    void test_static_comparatorByAge_is_annotated() {
        assertMethodIsAnnotated(implicitSortable, 'comparatorByAge')
    }

    @Test
    void test_explicit_compareTo_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitSortable, 'compareTo', Object)
    }

    @Test
    void test_explicit_compareTo_with_exact_type_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitSortable, 'compareTo', explicitSortable)
    }
}
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
class IndexedPropertyGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitIndex = parseClass('''import groovy.transform.IndexedProperty
      class ClassUnderTest {
          @IndexedProperty
          String[] names
          @IndexedProperty
          List ages
      }''')

    final Class<?> explicitIndex = parseClass('''import groovy.transform.IndexedProperty
      class ClassUnderTest {
          @IndexedProperty
          String[] names
          @IndexedProperty
          List ages
          String getNames(int idx) { null }
          Object getAges(int idx) { null }
          void setNames(int idx, String n) { }
          void setAges(int idx, Object o) { }
      }''')

    @Test
    void test_implicit_getNames_is_annotated() {
        assertExactMethodIsAnnotated(implicitIndex, 'getNames', String, int.class)
    }

    @Test
    void test_implicit_getAges_is_annotated() {
        assertExactMethodIsAnnotated(implicitIndex, 'getAges', Object, int.class)
    }

    @Test
    void test_implicit_setNames_is_annotated() {
        assertMethodIsAnnotated(implicitIndex, 'setNames', int.class, String)
    }

    @Test
    void test_implicit_setAges_is_annotated() {
        assertMethodIsAnnotated(implicitIndex, 'setAges', int.class, Object)
    }

    @Test
    void test_explicit_getNames_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitIndex, 'getNames', String, int.class)
    }

    @Test
    void test_explicit_getAges_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitIndex, 'getAges', Object, int.class)
    }

    @Test
    void test_explicit_setNames_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitIndex, 'setNames', int.class, String)
    }

    @Test
    void test_explicit_setAges_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitIndex, 'setAges', int.class, Object)
    }
}
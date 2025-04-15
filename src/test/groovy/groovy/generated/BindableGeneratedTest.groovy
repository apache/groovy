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

import java.beans.PropertyChangeListener

@CompileStatic
class BindableGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitBindable = parseClass('''@groovy.beans.Bindable
      class ClassUnderTest {
          String name
          int age
      }''')

    final Class<?> explicitBindable = parseClass('''@groovy.beans.Bindable
      class ClassUnderTest {
          String name
          int age
          void addPropertyChangeListener(java.beans.PropertyChangeListener l) { }
          void addPropertyChangeListener(String p, java.beans.PropertyChangeListener l) { }
          void removePropertyChangeListener(java.beans.PropertyChangeListener l) { }
          void removePropertyChangeListener(String p, java.beans.PropertyChangeListener l) { }
          void firePropertyChange(String p, Object o, Object n) { }
          java.beans.PropertyChangeListener[] getPropertyChangeListeners() { null }
          java.beans.PropertyChangeListener[] getPropertyChangeListeners(String p) { null }
      }''')

    @Test
    void test_addPropertyChangeListener_is_annotated() {
        assertMethodIsAnnotated(implicitBindable, 'addPropertyChangeListener', PropertyChangeListener)
    }

    @Test
    void test_addPropertyChangeListener2_is_annotated() {
        assertMethodIsAnnotated(implicitBindable, 'addPropertyChangeListener', String, PropertyChangeListener)
    }

    @Test
    void test_removePropertyChangeListener_is_annotated() {
        assertMethodIsAnnotated(implicitBindable, 'removePropertyChangeListener', PropertyChangeListener)
    }

    @Test
    void test_removePropertyChangeListener2_is_annotated() {
        assertMethodIsAnnotated(implicitBindable, 'removePropertyChangeListener', String, PropertyChangeListener)
    }

    @Test
    void test_firePropertyChange_is_annotated() {
        assertMethodIsAnnotated(implicitBindable, 'firePropertyChange', String, Object, Object)
    }

    @Test
    void test_getPropertyChangeListeners_is_annotated() {
        assertExactMethodIsAnnotated(implicitBindable, 'getPropertyChangeListeners', PropertyChangeListener[].class)
    }

    @Test
    void test_getPropertyChangeListeners2_is_annotated() {
        assertExactMethodIsAnnotated(implicitBindable, 'getPropertyChangeListeners', PropertyChangeListener[].class, String)
    }

    @Test
    void test_explicit_addPropertyChangeListener_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'addPropertyChangeListener', PropertyChangeListener)
    }

    @Test
    void test_explicit_addPropertyChangeListener2_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'addPropertyChangeListener', String, PropertyChangeListener)
    }

    @Test
    void test_explicit_removePropertyChangeListener_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'removePropertyChangeListener', PropertyChangeListener)
    }

    @Test
    void test_explicit_removePropertyChangeListener2_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'removePropertyChangeListener', String, PropertyChangeListener)
    }

    @Test
    void test_explicit_firePropertyChange_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'firePropertyChange', String, Object, Object)
    }

    @Test
    void test_explicit_getPropertyChangeListeners_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitBindable, 'getPropertyChangeListeners', PropertyChangeListener[].class)
    }

    @Test
    void test_explicit_getPropertyChangeListeners2_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitBindable, 'getPropertyChangeListeners', PropertyChangeListener[].class, String)
    }
}
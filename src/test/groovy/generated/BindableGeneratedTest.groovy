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
    void test_explicit_addPropertyChangeListener_is_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'addPropertyChangeListener', PropertyChangeListener)
    }

    @Test
    void test_explicit_addPropertyChangeListener2_is_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'addPropertyChangeListener', String, PropertyChangeListener)
    }

    @Test
    void test_explicit_removePropertyChangeListener_is_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'removePropertyChangeListener', PropertyChangeListener)
    }

    @Test
    void test_explicit_removePropertyChangeListener2_is_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'removePropertyChangeListener', String, PropertyChangeListener)
    }

    @Test
    void test_explicit_firePropertyChange_is_annotated() {
        assertMethodIsNotAnnotated(explicitBindable, 'firePropertyChange', String, Object, Object)
    }

    @Test
    void test_explicit_getPropertyChangeListeners_is_annotated() {
        assertExactMethodIsNotAnnotated(explicitBindable, 'getPropertyChangeListeners', PropertyChangeListener[].class)
    }

    @Test
    void test_explicit_getPropertyChangeListeners2_is_annotated() {
        assertExactMethodIsNotAnnotated(explicitBindable, 'getPropertyChangeListeners', PropertyChangeListener[].class, String)
    }
}
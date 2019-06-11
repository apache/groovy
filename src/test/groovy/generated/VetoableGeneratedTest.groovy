package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

import java.beans.VetoableChangeListener

@CompileStatic
class VetoableGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitVetoable = parseClass('''@groovy.beans.Vetoable
      class ClassUnderTest {
          String name
          int age
      }''')

    final Class<?> explicitVetoable = parseClass('''@groovy.beans.Vetoable
      class ClassUnderTest {
          String name
          int age
          void addVetoableChangeListener(java.beans.VetoableChangeListener l) { }
          void addVetoableChangeListener(String p, java.beans.VetoableChangeListener l) { }
          void removeVetoableChangeListener(java.beans.VetoableChangeListener l) { }
          void removeVetoableChangeListener(String p, java.beans.VetoableChangeListener l) { }
          void fireVetoableChange(String p, Object o, Object n) throws java.beans.PropertyVetoException { }
          java.beans.VetoableChangeListener[] getVetoableChangeListeners() { null }
          java.beans.VetoableChangeListener[] getVetoableChangeListeners(String p) { null }
      }''')

    @Test
    void test_addVetoableChangeListener_is_annotated() {
        assertMethodIsAnnotated(implicitVetoable, 'addVetoableChangeListener', VetoableChangeListener)
    }

    @Test
    void test_addVetoableChangeListener2_is_annotated() {
        assertMethodIsAnnotated(implicitVetoable, 'addVetoableChangeListener', String, VetoableChangeListener)
    }

    @Test
    void test_removeVetoableChangeListener_is_annotated() {
        assertMethodIsAnnotated(implicitVetoable, 'removeVetoableChangeListener', VetoableChangeListener)
    }

    @Test
    void test_removeVetoableChangeListener2_is_annotated() {
        assertMethodIsAnnotated(implicitVetoable, 'removeVetoableChangeListener', String, VetoableChangeListener)
    }

    @Test
    void test_fireVetoableChange_is_annotated() {
        assertMethodIsAnnotated(implicitVetoable, 'fireVetoableChange', String, Object, Object)
    }

    @Test
    void test_getVetoableChangeListeners_is_annotated() {
        assertExactMethodIsAnnotated(implicitVetoable, 'getVetoableChangeListeners', VetoableChangeListener[].class)
    }

    @Test
    void test_getVetoableChangeListeners2_is_annotated() {
        assertExactMethodIsAnnotated(implicitVetoable, 'getVetoableChangeListeners', VetoableChangeListener[].class, String)
    }

    @Test
    void test_explicit_addVetoableChangeListener_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitVetoable, 'addVetoableChangeListener', VetoableChangeListener)
    }

    @Test
    void test_explicit_addVetoableChangeListener2_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitVetoable, 'addVetoableChangeListener', String, VetoableChangeListener)
    }

    @Test
    void test_explicit_removeVetoableChangeListener_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitVetoable, 'removeVetoableChangeListener', VetoableChangeListener)
    }

    @Test
    void test_explicit_removeVetoableChangeListener2_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitVetoable, 'removeVetoableChangeListener', String, VetoableChangeListener)
    }

    @Test
    void test_explicit_fireVetoableChange_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitVetoable, 'fireVetoableChange', String, Object, Object)
    }

    @Test
    void test_explicit_getVetoableChangeListeners_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitVetoable, 'getVetoableChangeListeners', VetoableChangeListener[].class)
    }

    @Test
    void test_explicit_getVetoableChangeListeners2_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitVetoable, 'getVetoableChangeListeners', VetoableChangeListener[].class, String)
    }
}
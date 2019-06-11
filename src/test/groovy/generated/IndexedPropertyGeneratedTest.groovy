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
    void test_explicit_getNames_is_annotated() {
        assertExactMethodIsNotAnnotated(explicitIndex, 'getNames', String, int.class)
    }

    @Test
    void test_explicit_getAges_is_annotated() {
        assertExactMethodIsNotAnnotated(explicitIndex, 'getAges', Object, int.class)
    }

    @Test
    void test_explicit_setNames_is_annotated() {
        assertMethodIsNotAnnotated(explicitIndex, 'setNames', int.class, String)
    }

    @Test
    void test_explicit_setAges_is_annotated() {
        assertMethodIsNotAnnotated(explicitIndex, 'setAges', int.class, Object)
    }
}
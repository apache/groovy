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
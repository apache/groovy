package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class DelegateGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitDelegate = parseClass('''
      class ClassUnderTest {
          static class D {
              void m1() { }
              void m2() { }
          }
          
          @Delegate
          D delegate = new D()
      }''')

    final Class<?> explicitDelegate = parseClass('''
      class ClassUnderTest {
          static class D {
              void m1() { }
              void m2() { }
          }
          
          @Delegate
          D delegate = new D()
          
          void m1() { }
          // void m2() { }
      }''')

    @Test
    void test_m1_is_annotated() {
        assertMethodIsAnnotated(implicitDelegate, 'm1')
    }

    @Test
    void test_m2_is_annotated() {
        assertMethodIsAnnotated(implicitDelegate, 'm2')
    }

    @Test
    void test_explicit_m1_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitDelegate, 'm1')
    }

    @Test
    void test_explicit_m2_is_annotated() {
        assertMethodIsAnnotated(explicitDelegate, 'm2')
    }
}
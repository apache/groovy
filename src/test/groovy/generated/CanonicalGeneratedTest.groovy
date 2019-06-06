package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * @author Dmitry Vyazelenko
 * @author Andres Almiray
 */
@CompileStatic
class CanonicalGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitCanonical = new GroovyClassLoader().parseClass('''@groovy.transform.Canonical
       |class ClassUnderTest { 
       | String name
       | int age
       |}'''.stripMargin())

    final Class<?> explicitCanonical = new GroovyClassLoader().parseClass('''@groovy.transform.Canonical
       |class ClassUnderTest { 
       | String name
       | int age
       | ClassUnderTest(String n, int a) { }
       | boolean equals(Object o) { false }
       | int hashCode() { 42 }
       | String toString() { '' }
       |}'''.stripMargin())

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
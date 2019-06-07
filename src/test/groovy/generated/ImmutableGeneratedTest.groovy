package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class ImmutableGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitImmutable = parseClass('''@groovy.transform.Immutable
       |class ClassUnderTest { 
       | String name
       | int age
       |}''')

    final Class<?> explicitImmutable = parseClass('''@groovy.transform.Immutable
       |class ClassUnderTest { 
       | String name
       | int age
       | boolean equals(Object o) { false }
       | int hashCode() { 42 }
       | String toString() { '' }
       |}''')

    @Test
    void test_noArg_constructor_is_annotated() {
        assertConstructorIsAnnotated(implicitImmutable)
    }

    @Test
    void test_Map_constructor_is_annotated() {
        assertConstructorIsAnnotated(implicitImmutable, Map)
    }

    @Test
    void test_constructor_is_annotated() {
        assertConstructorIsAnnotated(implicitImmutable, String, int.class)
    }

    @Test
    void test_equals_is_annotated() {
        assertMethodIsAnnotated(implicitImmutable, 'equals', Object)
    }

    @Test
    void test_canEqual_is_annotated() {
        assertMethodIsAnnotated(implicitImmutable, 'canEqual', Object)
    }

    @Test
    void test_hashCode_is_annotated() {
        assertMethodIsAnnotated(implicitImmutable, 'hashCode')
    }

    @Test
    void test_toString_is_annotated() {
        assertMethodIsAnnotated(implicitImmutable, 'toString')
    }

    @Test
    void test_equals_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitImmutable, 'equals', Object)
    }

    @Test
    void test_hashCode_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitImmutable, 'hashCode')
    }

    @Test
    void test_toString_is_not_annotated() {
        assertMethodIsNotAnnotated(explicitImmutable, 'toString')
    }
}
package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class AutoCloneGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitAutoClone = parseClass('''@groovy.transform.AutoClone
       class ClassUnderTest {
       }''')

    final Class<?> explicitAutoClone = parseClass('''@groovy.transform.AutoClone
       class ClassUnderTest {
           Object clone() throws java.lang.CloneNotSupportedException { null }
       }''')

    @Test
    void test_clone_is_annotated() {
        assertExactMethodIsAnnotated(implicitAutoClone, 'clone', Object)
    }

    @Test
    void test_clone_with_exact_type_is_annotated() {
        assertExactMethodIsAnnotated(implicitAutoClone, 'clone', implicitAutoClone)
    }

    @Test
    void test_clone_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitAutoClone, 'clone', Object)
    }
}
package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class ConstructorsGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> noExplicitConstructors = parseClass('''class ClassUnderTest {
       }''')

    final Class<?> explicitNoArgConstructor = parseClass('''class ClassUnderTest {
           ClassUnderTest() { }
       }''')

    @Test
    void test_default_constructors_are_annotated() {
        assertConstructorIsAnnotated(noExplicitConstructors)
    }

    @Test
    void test_explicit_notArg_constructor_is_not_annotated() {
        assertConstructorIsNotAnnotated(explicitNoArgConstructor)
    }
}
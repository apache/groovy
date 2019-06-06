package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * @author Dmitry Vyazelenko
 * @author Andres Almiray
 */
@CompileStatic
class ConstructorsGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> noExplicitConstructors = new GroovyClassLoader().parseClass('''class ClassUnderTest { 
       |}'''.stripMargin())

    final Class<?> explicitNoArgConstructor = new GroovyClassLoader().parseClass('''class ClassUnderTest { 
       | ClassUnderTest() { }
       |}'''.stripMargin())

    @Test
    void test_default_constructors_are_annotated() {
        assertConstructorIsAnnotated(noExplicitConstructors)
    }

    @Test
    void test_explicit_notArg_constructor_is_not_annotated() {
        assertConstructorIsNotAnnotated(explicitNoArgConstructor)
    }
}
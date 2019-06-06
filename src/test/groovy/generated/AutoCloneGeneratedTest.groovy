package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Ignore
import org.junit.Test

/**
 * @author Dmitry Vyazelenko
 * @author Andres Almiray
 */
@CompileStatic
class AutoCloneGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> implicitAutoClone = new GroovyClassLoader().parseClass('''@groovy.transform.AutoClone
       |class ClassUnderTest {
       |}'''.stripMargin())

    final Class<?> explicitAutoClone = new GroovyClassLoader().parseClass('''@groovy.transform.AutoClone
       |class ClassUnderTest {
       | Object clone() throws java.lang.CloneNotSupportedException { null }
       |}'''.stripMargin())

    @Test
    void test_clone_is_annotated() {
        assertExactMethodIsAnnotated(implicitAutoClone, 'clone', Object)
    }

    @Test
    void test_clone_with_exact_type_is_annotated() {
        assertExactMethodIsAnnotated(implicitAutoClone, 'clone', implicitAutoClone)
    }

    @Ignore('https://issues.apache.org/jira/browse/GROOVY-9162')
    @Test
    void test_clone_is_not_annotated() {
        assertExactMethodIsNotAnnotated(explicitAutoClone, 'clone', Object)
    }
}
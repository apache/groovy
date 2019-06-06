package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * @author Dmitry Vyazelenko
 * @author Andres Almiray
 */
@CompileStatic
class PropertiesGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> withProps = new GroovyClassLoader().parseClass('''class WithProps { 
       | String name
       |}'''.stripMargin())

    final Class<?> withExplicitProps = new GroovyClassLoader().parseClass('''class WithExplicitProps { 
       | private String name
       | String getName() { name }
       | void setName(String n) { name = n }
       |}'''.stripMargin())

    @Test
    void test_implicit_getName_is_annotated() {
        assertMethodIsAnnotated(withProps, 'getName')
    }

    @Test
    void test_implicit_setName_is_annotated() {
        assertMethodIsAnnotated(withProps, 'setName', String)
    }

    @Test
    void test_explicit_getName_is_annotated() {
        assertMethodIsNotAnnotated(withExplicitProps, 'getName')
    }

    @Test
    void test_explicit_setName_is_annotated() {
        assertMethodIsNotAnnotated(withExplicitProps, 'setName', String)
    }
}
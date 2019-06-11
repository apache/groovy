package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class PropertiesGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> withProps = parseClass('''class WithProps {
           String name
       }''')

    final Class<?> withExplicitProps = parseClass('''class WithExplicitProps {
           private String name
           String getName() { name }
           void setName(String n) { name = n }
       }''')

    @Test
    void test_implicit_getName_is_annotated() {
        assertMethodIsAnnotated(withProps, 'getName')
    }

    @Test
    void test_implicit_setName_is_annotated() {
        assertMethodIsAnnotated(withProps, 'setName', String)
    }

    @Test
    void test_explicit_getName_is_not_annotated() {
        assertMethodIsNotAnnotated(withExplicitProps, 'getName')
    }

    @Test
    void test_explicit_setName_is_not_annotated() {
        assertMethodIsNotAnnotated(withExplicitProps, 'setName', String)
    }
}
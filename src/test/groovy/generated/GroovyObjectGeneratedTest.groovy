package groovy.generated

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * @author Dmitry Vyazelenko
 * @author Andres Almiray
 */
@CompileStatic
class GroovyObjectGeneratedTest extends AbstractGeneratedAstTestCase {
    final Class<?> classUnderTest = new GroovyClassLoader().parseClass('class MyClass { }')

    @Test
    void test_invokeMethod_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'invokeMethod', String, Object)
    }

    @Test
    void test_getProperty_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'getProperty', String)
    }

    @Test
    void test_setProperty_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'setProperty', String, Object)
    }

    @Test
    void test_getMetaClass_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'getMetaClass')
    }

    @Test
    void test_setMetaClass_is_annotated() {
        assertMethodIsAnnotated(classUnderTest, 'setMetaClass', MetaClass)
    }
}
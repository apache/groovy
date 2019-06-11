package groovy.generated

import groovy.transform.CompileStatic
import groovy.transform.Generated

import java.lang.reflect.Constructor
import java.lang.reflect.Method

@CompileStatic
abstract class AbstractGeneratedAstTestCase {
    protected final Class<?> parseClass(String str) {
        new GroovyClassLoader().parseClass(str)
    }

    protected final void assertClassIsAnnotated(Class<?> classUnderTest) {
        assert classUnderTest.getAnnotation(Generated)
    }

    protected final void assertClassIsNotAnnotated(Class<?> classUnderTest) {
        assert !classUnderTest.getAnnotation(Generated)
    }

    protected final void assertConstructorIsAnnotated(Class<?> classUnderTest, Class... paramTypes) {
        assert findConstructor(classUnderTest, paramTypes).getAnnotation(Generated)
    }

    protected final void assertConstructorIsNotAnnotated(Class<?> classUnderTest, Class... paramTypes) {
        assert !findConstructor(classUnderTest, paramTypes).getAnnotation(Generated)
    }

    protected final void assertMethodIsAnnotated(Class<?> classUnderTest, String methodName, Class... paramTypes) {
        assert findMethod(classUnderTest, methodName, paramTypes).getAnnotation(Generated)
    }

    protected final void assertMethodIsNotAnnotated(Class<?> classUnderTest, String methodName, Class... paramTypes) {
        assert !findMethod(classUnderTest, methodName, paramTypes).getAnnotation(Generated)
    }

    protected final void assertExactMethodIsAnnotated(Class<?> classUnderTest, String methodName, Class returnType, Class... paramTypes) {
        assert findExactMethod(classUnderTest, methodName, returnType, paramTypes).getAnnotation(Generated)
    }

    protected final void assertExactMethodIsNotAnnotated(Class<?> classUnderTest, String methodName, Class returnType, Class... paramTypes) {
        assert !findExactMethod(classUnderTest, methodName, returnType, paramTypes).getAnnotation(Generated)
    }

    private Method findMethod(Class<?> classUnderTest, String methodName, Class... paramTypes) {
        Method target = classUnderTest.getMethod(methodName, paramTypes)
        assert target
        target
    }

    private Method findExactMethod(Class<?> classUnderTest, String methodName, Class returnType, Class... paramTypes) {
        Method target = classUnderTest.methods.find { m ->
            m.name == methodName &&
                m.returnType == returnType &&
                Arrays.equals(m.parameterTypes, paramTypes)
        }
        assert target
        target
    }

    private Constructor findConstructor(Class<?> classUnderTest, Class... paramTypes) {
        Constructor target = classUnderTest.getConstructor(paramTypes)
        assert target
        target
    }
}

package gls.annotations.closures;

import java.lang.reflect.Constructor;

import groovy.lang.Closure;

// Uses annotation type declared in Java,
// instantiates annotation closure using pure Java APIs (no GDK),
// triggers stub generation for class JavaCompatibility
public class AnnotationClosureJavaCompatibilityTest extends AnnotationClosureExhaustiveTestSupport {
    public Class<?> getAnnotationClass() {
        return JavaAnnotationWithClassElement.class;
    }

    public Class<?> getAnnotatedClass() {
        return JavaCompatibility.class;
    }

    public void verify(Class closureClass) {
        try {
            Constructor ctor = closureClass.getConstructor(Object.class, Object.class);
            Closure closure = (Closure) ctor.newInstance(null, null);
            assertEquals(3, closure.call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


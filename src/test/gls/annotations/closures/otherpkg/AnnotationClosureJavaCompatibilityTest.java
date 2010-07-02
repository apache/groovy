package gls.annotations.closures.otherpkg;

import java.lang.reflect.Constructor;

import groovy.lang.Closure;

import gls.annotations.closures.AnnotationClosureExhaustiveTestSupport;
import gls.annotations.closures.JavaAnnotationWithClassElement;
import gls.annotations.closures.JavaCompatibility;

// Lives in a different package, uses annotation type declared in Java,
// and retrieves annotation closure using Java lang + API,
// triggers stub generation for class JavaCompatibility
public class AnnotationClosureJavaCompatibilityTest extends AnnotationClosureExhaustiveTestSupport {
    public Class<?> getAnnotationClass() { return JavaAnnotationWithClassElement.class; }

    public Class<?> getAnnotatedClass() { return JavaCompatibility.class; }

    public void verify(Class closureClass) {
        try {
            Constructor ctor = closureClass.getConstructor(Object.class, Object.class);
            Closure closure = (Closure) ctor.newInstance(null, null);
            assertEquals(3, closure.call());
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}


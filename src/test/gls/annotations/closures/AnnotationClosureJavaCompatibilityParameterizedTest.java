package gls.annotations.closures;

// Same as AnnotationClosureCompatibilityTest except that annotation closure's
// declared type is a bound parameterized type (affects stub generation)
public class AnnotationClosureJavaCompatibilityParameterizedTest extends AnnotationClosureJavaCompatibilityTest {
    public Class<?> getAnnotationClass() {
        return JavaAnnotationWithClassElementParameterized.class;
    }

    public Class<?> getAnnotatedClass() {
        return JavaCompatibilityParameterized.class;
    }
}

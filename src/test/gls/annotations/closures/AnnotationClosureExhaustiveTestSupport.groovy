package gls.annotations.closures

abstract class AnnotationClosureExhaustiveTestSupport extends GroovyTestCase {
    abstract Class getAnnotationClass()

    abstract Class getAnnotatedClass()

    abstract void verify(Class closureClass)

    void testWorksOnClassLevel() {
        worksOn(annotatedClass)
    }

    void testWorksOnMethodLevel() {
        worksOn(annotatedClass.getDeclaredMethod("aMethod", Object))
    }

    void testWorksOnFieldLevel() {
        worksOn(annotatedClass.getDeclaredField("aField"))
    }

    void testWorksOnPropertyLevel() {
        worksOn(annotatedClass.getDeclaredField("aProperty"))
    }

    private worksOn(level) {
        verify(level.getAnnotation(getAnnotationClass()).elem())
    }
}

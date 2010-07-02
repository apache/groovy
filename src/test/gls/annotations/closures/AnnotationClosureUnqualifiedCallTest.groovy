package gls.annotations.closures

class AnnotationClosureUnqualifiedCallTest extends AnnotationClosureExhaustiveTestSupport {
    Class getAnnotationClass() { AnnWithClassElement }

    Class getAnnotatedClass() { UnqualifiedCall }

    void verify(Class closureClass) {
        def closure = closureClass.newInstance(this, this)
        assert closure.call() == 42
    }

    def answer() { 42 }
}

@AnnWithClassElement(elem = { answer() })
class UnqualifiedCall {
    @AnnWithClassElement(elem = { answer() })
    private aField

    @AnnWithClassElement(elem = { answer() })
    def aProperty

    @AnnWithClassElement(elem = { answer() })
    def aMethod(@AnnWithClassElement(elem = { answer() }) aParam) {
        @AnnWithClassElement(elem = { answer() })
        def aLocal
    }
}
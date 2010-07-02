package gls.annotations.closures

class AnnotationClosureThisObjectCallTest extends AnnotationClosureExhaustiveTestSupport {
    Class getAnnotationClass() { AnnWithClassElement }

    Class getAnnotatedClass() { CallOnThisObject }

    void verify(Class closureClass) {
        def closure = closureClass.newInstance(null, this)
        assert closure.call() == 42
    }

    def answer() { 42 }
}

@AnnWithClassElement(elem = { this.answer() })
class CallOnThisObject {
    @AnnWithClassElement(elem = { this.answer() })
    private aField

    @AnnWithClassElement(elem = { this.answer() })
    def aProperty

    @AnnWithClassElement(elem = { this.answer() })
    def aMethod(@AnnWithClassElement(elem = { this.answer() }) aParam) {
        @AnnWithClassElement(elem = { this.answer() })
        def aLocal
    }
}
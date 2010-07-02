package gls.annotations.closures

class AnnotationClosureOwnerCallTest extends AnnotationClosureExhaustiveTestSupport {
    Class getAnnotationClass() { AnnWithClassElement }

    Class getAnnotatedClass() { CallOnOwner }

    void verify(Class closureClass) {
        def closure = closureClass.newInstance(this, null)
        closure.resolveStrategy = Closure.OWNER_ONLY
        assert closure.call() == 42
    }

    def answer() { 42 }
}

@AnnWithClassElement(elem = { owner.answer() })
class CallOnOwner {
    @AnnWithClassElement(elem = { owner.answer() })
    private aField

    @AnnWithClassElement(elem = { owner.answer() })
    def aProperty

    @AnnWithClassElement(elem = { owner.answer() })
    def aMethod(@AnnWithClassElement(elem = { owner.answer() }) aParam) {
        @AnnWithClassElement(elem = { owner.answer() })
        def aLocal
    }
}

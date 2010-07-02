package gls.annotations.closures

@JavaAnnotationWithClassElement(elem = { 1 + 2 })
class JavaCompatibility {
    @JavaAnnotationWithClassElement(elem = { 1 + 2 })
    private aField

    @JavaAnnotationWithClassElement(elem = { 1 + 2 })
    def aProperty

    @JavaAnnotationWithClassElement(elem = { 1 + 2 })
    def aMethod(@JavaAnnotationWithClassElement(elem = { 1 + 2 }) aParam) {
        @JavaAnnotationWithClassElement(elem = { 1 + 2 })
        def aLocal
    }
}
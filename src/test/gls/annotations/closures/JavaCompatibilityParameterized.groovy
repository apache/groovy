package gls.annotations.closures

@JavaAnnotationWithClassElementParameterized(elem = { 1 + 2 })
class JavaCompatibilityParameterized {
    @JavaAnnotationWithClassElementParameterized(elem = { 1 + 2 })
    private aField

    @JavaAnnotationWithClassElementParameterized(elem = { 1 + 2 })
    def aProperty

    @JavaAnnotationWithClassElementParameterized(elem = { 1 + 2 })
    def aMethod(@JavaAnnotationWithClassElementParameterized(elem = { 1 + 2 }) aParam) {
        @JavaAnnotationWithClassElementParameterized(elem = { 1 + 2 })
        def aLocal
    }
}
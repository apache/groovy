package groovy.bugs

class Groovy3069Bug extends GroovyTestCase {
    final String CLOSURE_STR = '[Closure]'
    final String CLASS_METHOD_STR = '[ClassMethod]'

    def void testClosureParamPrecedenceWithTypeSpecified() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithTypeSpecified(cl)
    }

    def void testClosureParamPrecedenceWithTypeNotSpecified() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithTypeNotSpecified(cl)
    }

    def void testClosureLocalVarPrecedenceExplicitClosureType() {
        Closure method = { CLOSURE_STR }

        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    def void testClosureLocalVarPrecedenceImplicitClosureType() {
        def method = { CLOSURE_STR }

        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    String method() {
        return CLASS_METHOD_STR
    }

    String checkPrecendenceWithTypeSpecified(Closure method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    String checkPrecendenceWithTypeNotSpecified(method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }
}

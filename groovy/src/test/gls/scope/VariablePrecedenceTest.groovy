package gls.scope

/**
*  test case based on GROOVY-3069
*/
class VariablePrecedenceTest extends GroovyTestCase {
    final String CLOSURE_STR = '[Closure]'
    final String CLASS_METHOD_STR = '[ClassMethod]'

    def void testClosureParamPrecedenceExplicitClosureType() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithExplicitClosureType(cl)
    }

    def void testClosureParamPrecedenceImplicitClosureType() {
        def cl = { CLOSURE_STR }
        checkPrecendenceWithImplicitClosureType(cl)
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

    String checkPrecendenceWithExplicitClosureType(Closure method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }

    String checkPrecendenceWithImplicitClosureType(method) {
        assert method() == CLOSURE_STR
        assert this.method() == CLASS_METHOD_STR
    }
}

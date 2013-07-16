import gls.CompilableTestSupport

class SyntaxTest extends CompilableTestSupport {

    void testValidIdentifiers() {
        // tag::valid_identifiers[]
        def name
        def item3
        def with_underscore
        def $dollarStart
        // end::valid_identifiers[]
    }

    void testInvalidIdentifiers() {
        shouldNotCompile '''
            // tag::invalid_identifiers[]
            def 3tier
            def a+b
            def a#b
            // end::invalid_identifiers[]
        '''
    }

    void testVariableDefinition() {
        // tag::variable_definition[]
        def a = 1
        // end::variable_definition[]

        // tag::assert_var_exists[]
        assert a == 1
        // end::assert_var_exists[]
    }
}
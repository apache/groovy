class SyntaxTest extends GroovyTestCase {

    void testVariableDefinition() {
        // tag::variable_definition[]
        def a = 1
        // end::variable_definition[]

        // tag::assert_var_exists[]
        assert a == 1
        // end::assert_var_exists[]
    }
}
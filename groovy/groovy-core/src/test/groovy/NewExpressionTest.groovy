class NewExpressionTest extends GroovyTestCase {

    void testNewInstance() {
        cheese = new String( "hey you hosers" )
        assert cheese != null
        System.err.println( cheese )
    }

}

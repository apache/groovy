class ExpandoPropertyTest extends GroovyTestCase {

    void testExpandoProperty() {
        foo = new Expando()
        
        foo.cheese = "Cheddar"
        foo.name = "Gromit"
        
        assert foo.cheese == "Cheddar"
        assert foo.name == "Gromit"
        
        assert foo.expandoProperties.size() == 2
    }
}

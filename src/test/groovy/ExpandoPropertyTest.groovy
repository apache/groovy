class ExpandoPropertyTest extends GroovyTestCase {

    void testExpandoProperty() {
        foo = new Expando()
        
        foo.cheese = "Cheddar"
        foo.name = "Gromit"
        
        assert foo.cheese == "Cheddar"
        assert foo.name == "Gromit"
        
        assert foo.expandoProperties.size() == 2
    }
    
    void testExpandoMethods() {
        foo = new Expando()

        foo.cheese = "Cheddar"
        foo.name = "Gromit"
        foo.nameLength = { return name.length() }
        foo.multiParam = { a, b, c | println("Called with ${a}, ${b}, ${c}"); return a + b + c }

        assert foo.cheese == "Cheddar"
        assert foo.name == "Gromit"
        assert foo.nameLength() == 6
        assert foo.multiParam(1, 2, 3) == 6
        
        // lets test using wrong number of parameters
        shouldFail { foo.multiParam(1) }
        shouldFail { foo.nameLength(1, 2) }
    }
}

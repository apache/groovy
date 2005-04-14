class ExpandoPropertyTest extends GroovyTestCase {

    void testExpandoProperty() {
        def foo = new Expando()
        
        foo.cheese = "Cheddar"
        foo.name = "Gromit"
        
        assert foo.cheese == "Cheddar"
        assert foo.name == "Gromit"
        
        assert foo.expandoProperties.size() == 2
    }
    
    void testExpandoMethods() {
        def foo = new Expando()

        foo.cheese = "Cheddar"
        foo.fullName = "Gromit"
        foo.nameLength = { return fullName.length() }
        foo.multiParam = { a, b, c -> println("Called with ${a}, ${b}, ${c}"); return a + b + c }

        assert foo.cheese == "Cheddar"
        assert foo.fullName == "Gromit"
        assert foo.nameLength() == 6 , foo.nameLength()
        assert foo.multiParam(1, 2, 3) == 6
        
        // lets test using wrong number of parameters
        shouldFail { foo.multiParam(1) }
        shouldFail { foo.nameLength(1, 2) }
    }
}

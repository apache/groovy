package groovy.bugs

class Groovy37XXBug extends GroovyTestCase {
    void testVarArgsWithAnInterfaceAsVarArgArrayTypeWithInheritenceInArgs() {
        def obj
        
        obj = new Groovy3799Helper(new ConcreteFoo3799(), new UnrelatedFoo3799())
    	assert obj.foos.size() == 2

    	obj = new Groovy3799Helper("a", "b", new ConcreteFoo3799(), new UnrelatedFoo3799())
        assert obj.foos.size() == 2
    }
}

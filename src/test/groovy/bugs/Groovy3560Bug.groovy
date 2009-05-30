package groovy.bugs

class Groovy3560Bug extends GroovyTestCase {
    void testVarArgsWithAnInterfaceAsVarArgArrayType() {
    	assert Groovy3560Helper.m1(new Groovy3560A(), new Groovy3560B()) == 2
    	assert Groovy3560Helper.m2("a", "b", new Groovy3560A(), new Groovy3560B()) == 2
    }
}

package groovy

class NamedParameterTest extends GroovyTestCase {

    void testPassingNamedParametersToMethod() {
        someMethod(name:"gromit", eating:"nice cheese", times:2)
    }
    
    protected void someMethod(args) {
        assert args.name == "gromit"
        assert args.eating == "nice cheese"
        assert args.times == 2
        assert args.size() == 3
    }
}

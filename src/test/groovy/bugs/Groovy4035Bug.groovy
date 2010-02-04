package groovy.bugs

class Groovy4035Bug extends GroovyTestCase {
    void testSuperCallInsideAnAIC() {
        def aic = new Foo4035() {
            def foo(Object msg) {
                return "AIC-" + super.foo(msg)
            }
        }
        assert aic.foo("42") == "AIC-Foo4035-42"
    }

    void testSuperCallInsideANormalInnerClass() {
        def inner = new Inner4035()
        
        assert inner.foo("42") == "Inner-Foo4035-42"
    }

    class Inner4035 extends Foo4035 {
        def foo(Object msg) {
            return "Inner-" + super.foo(msg)
        }
    }
}

class Foo4035 {
    def foo(msg) {
        "Foo4035-" + msg
    }
}

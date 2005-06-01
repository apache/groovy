import org.codehaus.groovy.runtime.ReflectionMethodInvoker

class GroovyInterceptableTest extends GroovyTestCase {

    void testMethodInterception() {
        def g = new GI()
        assert g.someInt() == 2806
        assert g.someUnexistingMethod() == 1
        assert g.toString() == "invokeMethodToString"
    }

    void testProperties() {
        def g = new GI()
        assert g.foo == 89
        g.foo = 90
        assert g.foo == 90
        // should this be 1 or 90?
        assert g.getFoo() == 1
    }
}

class GI implements GroovyInterceptable {

    @Property foo = 89

    int someInt() { 2806 }
    String toString() { "originalToString" }

    Object invokeMethod(String name, Object args) {
        if ("toString" == name)
            return "invokeMethodToString"
        else if ("someInt" == name)
            return ReflectionMethodInvoker.invoke(this, name, args)
        else
            return 1
    }
}


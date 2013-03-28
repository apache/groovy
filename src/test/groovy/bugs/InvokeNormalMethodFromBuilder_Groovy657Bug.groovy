package groovy.bugs

/**
  * Test that ensures that:
  * <ul>
  *   <li>it is possible to write a builder in Groovy</li>
  *   <li>it is possible to call normal methods from the builder,
  *       without the methods being trapped endlessly by createNode()</li>
  * </ul>
  *
  * @author Guillaume Laforge
  */
class InvokeNormalMethodFromBuilder_Bug657 extends GroovyTestCase {
    void testInvokeNormalMethod() {
        def b = new Builder()
        assert b.callNormalMethod() == "first"

        def value = b.someNode() {}
        assert value == "second"
    }
}

class Builder extends BuilderSupport {

    void setParent(Object parent, Object child) {}

    Object createNode(Object name)                 { return createNode(name, [:], null) }
    Object createNode(Object name, Map attributes) { return createNode(name, attributes, null) }
    Object createNode(Object name, Object value)   { return createNode(name, [:], value) }

    Object createNode(Object name, Map attributes, Object value) {
        println "create ${name}"
        return callOtherStaticallyTypedMethod()
    }

    String callNormalMethod()               { println "normalMethod"; return "first" }
    String callOtherStaticallyTypedMethod() { println "otherMethod";  return "second" }
    
}
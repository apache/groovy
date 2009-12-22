package groovy.bugs

class Groovy3949Bug extends GroovyTestCase {
    void testClosureCallInStaticContextForClassWithStaticCallMethod() {
        assert Class3949.m { "$it 123" } == "1234 123"
    }
}

class Class3949 {
    static call(arg) {"wrong call"}
    static m(Closure closure) {
        closure("1234")
    }
}

package groovy

/**
 * Invoke normal methods first: if no statically typed method exist, use invokeMethod().
 *
 * @author Guillaume Laforge
 */
class InvokeNormalMethodsFirstTest extends GroovyTestCase {

    void testPrintln() {
        println "call global println function"
    }

    void testStaticMethodOnJdkObject() {
        def myString = " static method "
        def newString = myString.trim()

        assert newString == "static method"
    }

    void testCallClosure() {
        def clos = { msg -> msg + " is Groovy" }
        def str = clos("Guillaume")

        assert str == "Guillaume is Groovy"
    }

    void testCallNormalMethodFromAGroovyDefinedClass() {
        def p = new Printer()
        def str = "Guillaume"
        def result = p.returnSelf(str)

        assert result == str
    }

    void testCallNormalMethodFirstFromWackyObject() {
        def w = new Wacky()
        def str = "Groovy"
        def staticResult = w.returnSelf(str)
        def invokeResult = w.nonExistingMethod(str)

        assert staticResult == str
        assert invokeResult == "invokerMethod call"
    }
}

class Printer {
    String returnSelf(msg) {
        return msg
    }
}

class Wacky {
    String returnSelf(msg) {
        return msg
    }

    Object invokeMethod(String name, Object args) {
        return "invokerMethod call"
    }
}
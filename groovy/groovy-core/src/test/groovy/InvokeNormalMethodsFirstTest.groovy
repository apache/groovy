
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
        myString = " static method "
        newString = myString.trim()

        assert newString == "static method"
    }

    void testCallClosure() {
        clos = { msg :: msg + " is Groovy" }
        str = clos("Guillaume")

        assert str == "Guillaume is Groovy"
    }

    void testCallNormalMethodFromAGroovyDefinedClass() {
        p = new Printer()
        str = "Guillaume"
        result = p.returnSelf(str)

        assert result == str
    }

    void testCallNormalMethodFirstFromWackyObject() {
        w = new Wacky()
        str = "Groovy"
        staticResult = w.returnSelf(str)
        invokeResult = w.nonExistingMethod(str)

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
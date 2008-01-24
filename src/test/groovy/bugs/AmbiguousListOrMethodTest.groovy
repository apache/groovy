package groovy.bugs

class AmbiguousListOrMethodTest extends GroovyTestCase {

    void testLocalVariableVersion() {
        def foo = [3, 2, 3]

        def val = foo [0]
        println val
        assert val == 3
    }

    void testUndefinedPropertyVersion() {
        try {
            def val = this.foo [0]
            println val
        }
        catch (MissingPropertyException e) {
            println "Worked! Caught missing property $e"
        }
    }

    void testMethodCallVersion() {
        def val = foo([0])
        println val
        assert val == 1
    }


    def foo(int val) {
        println "Calling foo method with a int param of val"
        println val
        return null
    }

    def foo(List myList) {
        println "Calling foo method with a list param of $myList"
        return myList.size()
    }

    void testCanFindCorrectMethod() {
        def e = new Example()
        assert e["", ""] == 2
        assert e[""] == 1
    }

}

class Example {
    def getAt(String a, String b) {return 2}
    def getAt(String a) {return 1}
}

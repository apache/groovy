package groovy.bugs

class Groovy3405Bug extends GroovyTestCase {

    void testAddingStaticMethodsOnMCWithDefaultParameters() {
        // test with 2 params having default values
        String.metaClass.'static'.testStaticTwoParams = { first = "foo", second = "bar" ->  return "$first - $second" }
        assert "baz - qux" == "".testStaticTwoParams("baz", "qux")
        assert "baz - bar" == "".testStaticTwoParams("baz")
        assert "foo - bar" == "".testStaticTwoParams()

        // test with 1 param having default value
        String.metaClass.'static'.testStaticOneParam = { first = "foo" ->  return first }
        assert "baz" == "".testStaticOneParam("baz")
        assert "foo" == "".testStaticOneParam()

        println "Done"
    }
}

class HelloWorld extends GroovyTestCase {

    String foo = "John"
    String bar = "Jez"

    void testCase() {
        println "Hello"
        println 123
        println 123.456

        println "Hello $foo!"
        println "Hello ${bar}!"
    }

}
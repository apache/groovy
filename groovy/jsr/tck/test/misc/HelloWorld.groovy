class HelloWorld extends GroovyTestCase {

    String foo = "John"
    String bar = "Jez"

    void testCase() {
        println "Hello"
        println 123
        println 123.456

        println "Hello $foo!"
        println "Hello ${bar}!"

        def x = 123

        println "value is $x"

        x = x + 1
        println "value is now ${x}"

        def f = this
        def answer = f.foo("hello ", "James")

        println "Received answer $answer"

        assert answer == "hello James"

        answer = foo2("hello ", "Guillaume")
        assert answer == "hello Guillaume"

        println "Now the answer is $answer"
    }

    String foo(a, b) {
        return a + b
    }


    def foo2(a, b) {
        def answer = a + b
        return answer
    }

}
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

    void testIf() {
        def x = 123
        def y = 1
        if (x > 100) {
            y = 2
        }
        else { // TODO remove this when the parser works!
        }
        assert y == 2
    }

    void testWhile() {
        def x = 10

        while (x > 0) {
            println "loop value with x=$x"
            x = x - 1

            if (x == 3) {
                break //FOO
            }
        }

        //FOO: println "Done"
    }
    String foo(a, b) {
        return a + b
    }


    def foo2(a, b) {
        def answer = a + b
        return answer
    }

    void testClosure() {
        def list = [1, 2, 3]

        //println "list is $list"

        list.each {(e)| println("List contains $e") }
    }

}
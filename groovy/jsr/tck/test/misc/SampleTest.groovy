package foo.bar

import java.io.File
import java.util.Date as UDate

class SampleTest extends GroovyTestCase {

    String foo = "John"
    String bar = "Jez"

    void testNew() {
        x = new ArrayList()
        x.add(123)

        assert x.size() == 1

        println "created list $x"

        /* TODO not working yet!

        f = new File("foo.txt")

        println "File name $f.name"

                println new UDate()
        */
    }

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

    void testIfElse() {
        def x = 123
        def y = 1
        if (x > 100) {
            y = 2
        }
        else {
            y = 3
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

        println "list is $list"

        list.each {(e)| println "List contains $e" }
    }

    void testFor() {
        def list = [1, 2, 3]

        println "normal iteration loop on list $list"

        for (i in list) {
            println "for item: $i"
        }

        println "typed iteration loop on list $list"

        for (Integer i in list) {
            println "for item: $i"
        }
    }


    void testTryCatch() {
        try {
            methodThatDoesNotThrowException()
        }
        catch (Exception e) {
            fail "Should not throw exception $e"
        }
    }

    void testTryCatchFinally() {
        try {
            methodThatDoesNotThrowException()
        }
        catch (Exception e) {
            fail "Should not throw exception $e"
        }
        finally {
            println "Called from finally block"
        }
    }

    void methodThatDoesNotThrowException() {
        println "Normal method invocation..."
    }


    /* TODO when parser fixed


    void testMap() {
        m = [1:2, "foo":"bar", "x":4.2]

        println "Created map $m"

        m.each { (k, v)| println "key $k and value $v" }
    }


    void testTryCatchWithException() {
        try {
            methodThatThrowsException()
            fail "Should have thrown an exception by now!"
        }
        catch (Exception e) {
            println "Worked! Caught expected exception $e"

        }
    }

    void methodThatThrowsException() {
        // TODO parser doesn't return the thrown expression
        throw new Exception("Test exception")
    }

    void testIf() {
        def x = 123
        def y = 1
        if (x > 100) {
            y = 2
        }
        assert y == 2
    }
    */


}
package foo.bar

import java.io.File
import java.util.Date as UDate

// TODO allow annotations on classes again :)
//@Bean(cool=true)

class SampleTest extends GroovyTestCase {

    String foo = "John"
    String bar = "Jez"
    private static def dummyx = 123
    protected final def dummyY = 456

    void testRange() {
        def range = 1..3
        assert range == [1, 2, 3]

        for (i in 1..10) {
            println "Range loop at $i"
        }

        (10..1).each { println "Range closure loop at $it" }
    }


    void testListClosure() {
        def list = [1, 2, 3]

        println "list is $list"

        // TODO sort out parser
        // list.each {|e| println("List contains $e") }
    }

    void testMap() {
        n = [:]
        assert n instanceof java.util.Map

        m = ["y":2, "foo":"bar", "x":4.2]

        // TODO
        //m.each { (k, v)| println "key $k and value $v" }

        println "Created map $m"

        m2 = [x:123, y:456, z:"whatnot"]

        println "Created map with named arguments $m2"

        // TODO parser can't handle this!
        //m3 = [123:456, 678:"whatnot"]
        m3 = [(123):456, (678):"whatnot"]
        println "Created map with named arguments $m3"
    }

    void testMultiLineString() {
        println """
this is
a very
long string
which spans
many lines
"""
    }

    void testStrings() {
        assert "\\" == '\\'
        assert "\\" != "\\\\"
    }

    void testNew() {
        x = new ArrayList()
        x.add(123)

        assert x.size() == 1

        println "created list $x"
    }

    void testNewWithImports() {
        f = new File("foo.txt")

        def name = f.name
        def name2 = f.getName()
        assert name == name2


        println "File name $name"

        println new UDate()

        def sd = new java.sql.Date(105, 11, 5)
        println "created SQL date of value $sd"
    }

    void testAsCastAndInstanceof() {
        def x = "foo"

        def foo = x as String
        def bar = (String) x

        assert foo == bar
        assert foo instanceof String
    }

    void testComplexExpression() {
        def list = [1, [1, [1, 2], 3], 3]

        def x = list[1][1][1]

        assert x == 2

        def y = list.get(1)[1].get(1)

        assert x == y
    }

    void testCase() {
        println "Hello"
        println 123
        println 123.456

        println "Hello $foo!"
        println "Hello ${bar}!"


        def f = this
        def answer = f.foo("hello ", "James")

        println "Received answer $answer"

        assert answer == "hello James"

        answer = foo2("hello ", "Guillaume")
        assert answer == "hello Guillaume"

        println "Now the answer is $answer"
    }

    void testSubscript() {
        list = [1, 2, 3]

        def x = list[2]
        assert x == 3
    }

    void testIf() {
        def x = 123
        def y = 1
        if (x > 100) {
            y = 2
        }

        assert y == 2
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
                break
            }
        }
    }

    void testWhileWithLabel() {
        def x = 10

        while (x > 0) {
            println "loop value with x=$x"
            x = x - 1

            if (x == 3) {
                break FOO
            }
        }

        FOO: println "Done"
    }

    String foo(a, b) {
        return a + b
    }


    def foo2(a, b) {
        def answer = a + b
        return answer
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

    void testSwitch() {
        x = 12
        switch (x) {
            case 1:
                fail "not 1"
                break

            case  12:
                println "Switch worked! answer is 12!"
                break

            case "text":
                fail "not text"
                break

            default:
                fail "Not defailt"
                break
        }

        x = 5
        switch (x) {
            case 1:
                fail "not 1"
                break

            case  12:
                fail "not 12"
                break

            case "text":
                fail "not text"
                break

            default:
                println "worked, the default answer"
                break
        }
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


    void testBug675() {
        assert "\\"!="\\\\"

        // Are the following valid now? Must $ be escaped?

        // TODO  assert "\\$"=="\\"+"$"


        assert ("\\\\"+"\\").length() == 3
        // TODO assert "\\3 $1$2" == "\\" + "3" + " " + "$" + "1" + "$" + "2"
        // TODO assert "\\\\3 \\$1$2" == "\\" + "\\" + "3" + " " + "\\"+ "$" + "1" + "$" + "2"
        // TODO assert "\\\\\\3 \\\\$1$2" == "\\" + "\\\\" + "3" + " " + "\\\\"+ "$" + "1" + "$" + "2"
        // TODO assert "\\\\\\\\3 \\\\\\$1$2" == "\\\\" + "\\\\" + "3" + " " + "\\\\\\"+ "$" + "1" + "$" + "2"

        assert "\\\\" == "\\" + "\\"
        assert "\\\\".length() == 2

        z = 100 + 200
        assert "Hello\\, \\World\\".charAt(4) == "o".charAt(0)
        assert "Hello\\, \\World\\".charAt(5) == "\\".charAt(0)
        assert "Hello\\, \\World\\".charAt(6) == ",".charAt(0)

        // TODO failing tests...
        // assert "\\"+"\\\\" == "\\"+"\\"+"\\" && "\\\\"+"\\" == "\\"+"\\"+"\\"
        // assert "\\\\ \\ ${z}" == "\\\\ \\ 300"
        // assert "\\\\ \\ ${z}" == "\\" + "\\" + " " + "\\" + " " + "300"
    }


    @webService void methodWithAnnotations() {
        println "Hey"
    }

    protected synchronized static void something() {
        println "Hey, I'm synchronized and static"
    }

    /** TODO runtime breaks!
    void testPrePostFix() {
        def x = 124

        println("value is $x")

        if (x > 100) {
        ++x
        }

        assert x == 124
        assert ++x == 125

        assert x++ == 125
        assert --x == 124
        assert x-- == 124

        //println("value is now ${x}")
    }
    */


    // TODO when parser fixed
    //-------------------------------------------------------------------------


    /*

    void testArrays() {
        // TODO this line seems to cause a stack overflow...
        String[] array = { "a", "b", "c" }

        List list = array.asList()

        assert list == ["a", "b", "c"]
    }


    void testMapWithIntegerKey() {
        m = [1:2, "foo":"bar", "x":4.2]

        println "Created map $m"

        m.each { (k, v)| println "key $k and value $v" }
    }

    */

}
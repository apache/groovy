
class A {
    private def testIf() {
        if (5)
            5
    }

    private def testIfElse() {
        def a
        if (5)
            println("")
        else
            if (a == 4)
                1 + 3
            else
                12

        if (5)
            println("")
        else
            if (a == 4)
                1 + 3
    }

    private def testFor() {
        for (; ;)
            return 5
        for (def a = 1; a < 10; a++)
            println(a)
        def a
        for (; a < 10; a++)
            break

        for (a = 0; a; ++a)
            println(a)
    }

    private def testForIn() {
        for (a in Collections.EMPTY_LIST)
            println(hashCode())

        for (def b in Collections.EMPTY_LIST)
            println(this)
    }

    private def testWhile() {
        while (a)
            println('!')
    }

    private def testJava5For() {
        for (int i : [])
            println(i)
    }
}

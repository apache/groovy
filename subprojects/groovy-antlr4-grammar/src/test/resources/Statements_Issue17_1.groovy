
class A {
    private def testIf() {
        if (5) {

        }
    }

    private def testIfElse() {
        def a
        if (5) {
        } else { if (a == 4) {
        } else {
        }}
    }

    private def testFor() {
        for (; ;) {
            return 5
        }
        for (def a = 1; a < 10; a++) {
            println(a)
        }
        def a
        for (; a < 10; a++) {
            println(a)
            break
        }
        for (a = 0; a; ++a) {
            println(a)
            println(a + 1)
            continue
        }
    }

    private def testForIn() {
        for (a in Collections.EMPTY_LIST) {
            println(hashCode())
        }

        for (def b in Collections.EMPTY_LIST) {
            println(this)
        }
    }

    private def testWhile() {
        while (a) {
            println('!')
        }
    }

    private def testSwitch() {
        switch (1) {
        }
        switch (1) {
            case 1: println('1'); break;
            case 2: println('2'); break;
            default: println('d'); break;
        }
        switch (1) {
            case 1: println('1'); break;
        }
        switch (1) {
            default: println('1'); break;
        }
    }

    private def testComplexSwitch() {
        def x
        def result = ' '

        switch (x) {
            case 'foo':
                result = 'found foo'

            case 'bar':
                result = result + 'bar'

        //case [4, 5, 6, 'inList']:
        //   result = 'list'
        //   break

            case 12..30:
                result = 'range'
                break

            case Integer:
                result = 'integer'
                break

            case Number:
                result = 'number'
                break

            default:
                result = 'default'
        }
    }

    private def testJava5For() {
        for (int i : []) {
            println(i)
        }
    }
}

class WhileLoopTest extends GroovyTestCase {

    void testVerySimpleWhile() {
        val = doWhileMethod(0, 5)
        println(val)
    }

    void testMoreComplexWhile() {
        def x = 0
        def y = 5

        while ( y > 0 ) {
            x = x + 1
            y = y - 1
        }

        assert x == 5
    }

    void testDoWhileWhile() {
        def x = 0, y = 5

        do {
            x = x + 1
            y = y - 1
        }
        while ( y > 0 )

        assert x == 5
    }

    def doWhileMethod(x, m) {
        while ( x < m ) {
            x = increment(x)
        }

        return x
    }

    def increment(x) {
        x + 1
    }
}

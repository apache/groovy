class DoWhileLoopTest extends GroovyTestCase {

    void testDoWhileWhile() {
        def x = 0
        def y = 5

        do {
            x = x + 1
            y = y - 1
        }
        while ( y > 0 )

        assert x == 5
    }
}

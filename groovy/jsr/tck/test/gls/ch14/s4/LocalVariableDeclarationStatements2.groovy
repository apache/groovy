/**
 * Every local variable declaration statement is immediately contained by a block.
 */
class Foo {
    def run() {
        int a = 1
        {
            int b = 2
            assert a == 1
            assert b == 2
        }
        assert a == 1
        //assert b == 2 //@fail
    }
}

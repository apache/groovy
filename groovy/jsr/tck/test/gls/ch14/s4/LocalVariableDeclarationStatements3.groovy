/**
 * Local variable declaration statements may be intermixed freely with other kinds of statements in the block.
 */
class Foo {
    def run() {
        int a = 1
        assert a == 1
        int b = 2
        assert b == 2
    }
}

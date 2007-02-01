package groovy.bugs

class VariablePrecedenceTest extends GroovyTestCase {
    def x = 100
    def y = 93
    def c = {x -> assert x == 1; assert y == 93; }

    void testFoo() {
        String[] args = ["a"]
        main(args)
    }

    static void main(args) {
        def vfoo = new VariablePrecedenceTest()
        vfoo.c.call(1)
        def z = 874;
        1.times { assert vfoo.x == 100; assert z == 874; z = 39; }
        assert z == 39;
        vfoo.local();
    }

    void local() {
        c.call(1);
        def z = 874;
        1.times { assert x == 100; assert z == 874; z = 39; }
        assert z == 39;
    }
}
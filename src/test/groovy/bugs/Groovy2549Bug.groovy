package groovy.bugs

class Groovy2549Bug extends GroovyTestCase {
    void test2549 () {
        def x = 1 + (2 as int)
        assert x == 3

        def y = 1 + (2 as Integer)
        assert y == 3

        def z = 1 + (2 as long)
        assert z == 3

	def zzz = 1 + (2 as float)
        assert zzz == 3
    }
}

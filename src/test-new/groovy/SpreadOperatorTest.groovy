/**
 * @version $Revision$
 *
 * @author phk
 */

// package groovy;

class SpreadOperatorTest extends GroovyTestCase {

    void testSpreadingInList() {
        println([1, *[222, 333], 456])
        assert [1, *[222, 333], 456] == [1, 222, 333, 456]

        y = [1,2,3]
        println([*y])
        assert [*y] == y
    }

    void testSpreadingRange() {
        r = 1..10
        assert [*r] == r
        assert [*1..10] == r
    }

    void testSpreadingInMethodParameters() {
        // println sum(1, *[2, 3], 4)
        // println sum(*[10, 20, 30, 40])
        assert sum(1, *[2, 3], 4) == 10
        assert sum(*[10, 20, 30, 40]) == 100

        z = [11, 22, 33]
        // println sum(1, *z)
        // println sum(*z, 2)
        // println sum(*z, 44)
        assert sum(1, *z) == 67
        assert sum(*z, 2) == 68
        assert sum(*z, 44) == 110

        x = ["foo", "Bar-"]
        // println sum(*x, *x)
        assert sum(*x, *x) == "fooBar-fooBar-"
    }

    def sum(a, b, c, d) {
        return a + b + c + d
    }

    void testSpreadingInClosureParameters() {
        twice = {:: it*2}
        // println twice(3)
        // println twice("abcd")
        // println twice(*[11])
        assert twice(3) == 6
        assert twice("abcd") == 'abcdabcd'
        assert twice(*[11]) == 22
    }
}

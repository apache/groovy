/**
 * @version $Revision$
 *
 * <code>[2, 3].toSpreadList() equals to *[2, 3]</code> <br><br>
 *
 * For an example, <pre>
 *        assert [1, *[2, 3], 4] == [1, [2, 3].toSpreadList(), 4]
 * </pre>
 *
 * @author Pilho Kim
 */

class SpreadListOperatorTest extends GroovyTestCase {

    void testSpreadingInList() {
        println([1, *[222, 333], 456])
        println([1, [222, 333].toSpreadList(), 456])

        assert [1, *[222, 333], 456] == [1, 222, 333, 456]
        assert [1, [222, 333].toSpreadList(), 456] == [1, 222, 333, 456]

        def y = [1,2,3]

        assert [*y] == y
        assert [y.toSpreadList()] == y
    }

    void testSpreadingRange() {
        def r = 1..10

        assert [*r] == r
        assert [*1..10] == r

        assert [r.toSpreadList()] == r
        assert [(1..10).toSpreadList()] == r
    }

    void testSpreadingInMethodParameters() {
        assert sum(1, *[2, 3], 4) == 10
        assert sum(*[10, 20, 30, 40]) == 100

        assert sum(1, [2, 3].toSpreadList(), 4) == 10
        assert sum([10, 20, 30, 40].toSpreadList()) == 100

        def z = [11, 22, 33]

        assert sum(1, *z) == 67
        assert sum(1, z.toSpreadList()) == 67

        assert sum(*z, 2) == 68
        assert sum(z.toSpreadList(), 2) == 68

        assert sum(*z, 44) == 110
        assert sum(z.toSpreadList(), 44) == 110

        def x = ["foo", "Bar-"]

        assert sum(*x, *x) == "fooBar-fooBar-"
        assert sum(x.toSpreadList(), x.toSpreadList()) == "fooBar-fooBar-"
    }

    def sum(a, b, c, d) {
        return a + b + c + d
    }

    void testSpreadingInClosureParameters() {
        def twice = {-> it*2}
        assert twice(3) == 6
        assert twice("abcd") == 'abcdabcd'

        assert twice(*[11]) == 22
        assert twice([11].toSpreadList()) == 22
    }
}

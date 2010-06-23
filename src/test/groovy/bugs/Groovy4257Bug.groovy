package groovy.bugs

class Groovy4257Bug extends GroovyTestCase {
    void testSetUnique() {
        Set orig = [[3035, 26972], [2795, 34412]]
        Set clone = orig.clone()
        orig.unique()
        assert orig == clone
        orig.unique{ it }
        assert orig == clone
    }

    void testNumberCoercion() {
        def orig = [3, 3L, 3.0] as Set
        def expected = [3] as Set
        orig.unique()
        assert orig == expected
        orig = [3, 3L, 3.0] as Set
        orig.unique{ it }
        assert orig == expected
    }
}

package groovy

class ChainedAssignmentTest extends GroovyTestCase {

    def dummy(v) {
        print v
    }

    void testCompare() {
        def i = 123
        def s = "hello"

        def i2
        def i1 = i2 = i;
        assert i1 == 123
        assert i2 == 123

        def s1
        dummy(s1 = s)
        assert s1 == "hello"
    }
}

class ChainedAssignment extends GroovyTestCase {

    dummy(v) {
        print v
    }

    void testCompare() {
        i = 123
        s = "hello"

        i1 = i2 = i;
        assert i1 == 123
        assert i2 == 123

        dummy(s1 = s)
        assert  s1 == "hello"
	}
}

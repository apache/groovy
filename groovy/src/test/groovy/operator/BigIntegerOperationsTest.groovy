package groovy.operator

class BigIntegerOperationsTest extends GroovyTestCase {
    void testAssign() {
        BigInteger foo
        foo = (byte) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (short) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (int) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (long) 20
        assert foo.class == BigInteger
        assert foo == 20

        foo = (float) 0.5f
        assert foo.class == BigInteger
        assert foo == 0

        foo = (double) 0.5d
        assert foo.class == BigInteger
        assert foo == 0
        
        foo = 10.5G
        assert foo.class == BigInteger
        assert foo == 10
        
        double d = 1000
        d *= d
        d *= d
        d *= d
        assert (long)d != d
		assert (BigInteger) d == d
    }
}

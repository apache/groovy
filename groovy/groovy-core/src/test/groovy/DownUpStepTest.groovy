import java.math.*

public class DownUpStepTest extends GroovyTestCase {

    void testDownto() {
        def z = []
        (10.5).downto(5.9) { z << it }
        assertEquals( [10.5, 9.5, 8.5, 7.5, 6.5], z)
    }

    void testBigIntegerDowntoBigDecimal() {
        def z = []
        (new BigInteger("10")).downto(new BigDecimal("5.9")) { z << it }
        assertEquals( [new BigInteger("10"), new BigInteger("9"), new BigInteger("8"), 
        	new BigInteger("7"), new BigInteger("6")], z)
    }

    void testUpto() {
        def z = 0.0
        (3.1).upto(7.2) { z += it }
        assert z == 3.1 + 4.1 + 5.1 + 6.1 + 7.1
        assert z == 25.5
    }

    void testStep() {
        def z = 0.0
        (1.2).step(3.9, 0.1) { z += it }
        assert z == 67.5
    }

    void testDownStep() {
        def z = 0.0
        (3.8).step(1.1, -0.1) { z += it }
        assert z == 67.5
    }
}

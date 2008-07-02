package groovy
/**
 * Test case for using the "as" keyword to convert between strings
 * and numbers in both directions.
 */
class AsTest extends GroovyTestCase {

    def subject
    /**
     * Test that "as String" works for various types.
     */
    void testAsString() {
        assert (48256846 as String) == "48256846"
        assert (0.345358 as String) == "0.345358"
        assert (12.5432D as String) == "12.5432"
        assert (3568874G as String) == "3568874"
    }

    void testStringAsBigInteger() {
        subject = "34587203957357" as BigInteger
        assert subject.class == BigInteger
        assert subject == 34587203957357
    }

    void testStringAsLong() {
        subject = "32498687" as Long
        assert subject.class == Long
        assert subject == 32498687L
    }

    void testStringAsInt() {
        subject = "32498687" as int
        assert subject.class == Integer
        assert subject == 32498687
    }

    void testStringAsShort() {
        subject = "13279" as Short
        assert subject.class == Short
        assert subject == 13279
    }

    void testStringAsByte() {
        subject = "12" as Byte
        assert subject.class == Byte
        assert subject == 12
    }

    void testStringAsBigDecimal() {
        subject = "12.54356" as BigDecimal
        assert subject.class == BigDecimal
        assert subject == 12.54356
    }

    void testStringAsDouble() {
        subject = "1.345" as double
        assert subject.class == Double
        assert subject == 1.345
    }

    void testStringAsFloat() {
        subject = "1.345" as float
        assert subject.class == Float
        assert subject == 1.345F
    }
}

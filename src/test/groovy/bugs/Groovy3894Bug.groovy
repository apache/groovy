package groovy.bugs

class Groovy3894Bug extends GroovyTestCase {
    void testInfinityToBigDecimalConversion() {
        BigDecimal x = 999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
        assert x ** 5 == Double.POSITIVE_INFINITY
        try {
            BigDecimal y = x ** 5
        } catch (NumberFormatException nfe) {
            assert nfe.message == 'Infinite or NaN'
        }

        x = -999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
        assert x ** 5 == Double.NEGATIVE_INFINITY
        try {
            BigDecimal y = x ** 5
        } catch (NumberFormatException nfe) {
            assert nfe.message == 'Infinite or NaN'
        }
    }
}

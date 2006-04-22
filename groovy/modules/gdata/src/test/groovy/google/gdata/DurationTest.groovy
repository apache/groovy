class DurationTest extends GroovyTestCase {
    void testFixedDurationArithmetic() {
        def oneDay = 2.days - 1.day
        assert oneDay.getMillis() == (24 * 60 *60 * 1000)
        
        oneDay = 1.day - 2.days + 24.hours
        assert oneDay.getMillis() == (24 * 60 *60 * 1000)
   }
}
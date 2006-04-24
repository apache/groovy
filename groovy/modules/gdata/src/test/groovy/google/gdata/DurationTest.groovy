package groovy.google.gdata

import org.codehaus.groovy.runtime.TimeCategory

class DurationTest extends GroovyTestCase {
    void testFixedDurationArithmetic() {
        use(TimeCategory) {
            def oneDay = 2.days - 1.day
            assert oneDay.getMillis() == (24 * 60 *60 * 1000)
            
            oneDay = 2.days - 1.day + 24.hours - 1440.minutes
            assert oneDay.getMillis() == (24 * 60 *60 * 1000)
        }
   }
}

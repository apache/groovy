package groovy.google.gdata

import org.codehaus.groovy.runtime.TimeCategory
import java.uti.Date

class DurationTest extends GroovyTestCase {
    void testFixedDurationArithmetic() {
        use(TimeCategory) {
            def oneDay = 2.days - 1.day
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000)
            
            oneDay = 2.days - 1.day + 24.hours - 1440.minutes
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000)
        }
   }
    
    void testDatumDependantArithmetic() {
        use(TimeCategory) {
            def twoMonths = 1.month + 1.month
            shouldFail {
                println twoMonths.toMilliseconds()
            }
            
            def monthAndWeek = 1.month + 1.week
            shouldFail {
                println monthAndWeek.toMilliseconds()
            }
            
            def now = new Date()
            def then = monthAndWeek + now
            def week = then - 1.month - now
            assert week.toMilliseconds() == (7 * 24 * 60 * 60 * 1000)
            
            then = now + monthAndWeek
            week = then - 1.month - now
            assert week.toMilliseconds() == (7 * 24 * 60 * 60 * 1000)
            
            assert (now + monthAndWeek) == (monthAndWeek + now)
            
            week = then - (now + 1.month)
            assert week.toMilliseconds() == (7 * 24 * 60 * 60 * 1000)
        }
    }
}

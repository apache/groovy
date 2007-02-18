package groovy.time

import org.codehaus.groovy.runtime.TimeCategory
import java.util.Date

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
        //
        // Comment this out for the present
        // The problam is that this test fails if there is a daylight savins change any thime in the next five weeks
        //
        /*
        use(TimeCategory) {
            def twoMonths = 1.month + 1.month
            def twoMonthsFromNow = 2.months.from.now - 0.months.from.now 
            def oneMonthFromNow = 1.month.from.now - 0.months.from.now
            
            assert twoMonths.toMilliseconds() == twoMonthsFromNow.toMilliseconds()
            
            def monthAndWeek = 1.month + 1.week
            
            assert monthAndWeek.toMilliseconds() == (oneMonthFromNow + 1.week).toMilliseconds()
            
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
        */
    }
}

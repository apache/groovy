package groovy.time

import org.codehaus.groovy.runtime.TimeCategory
import java.util.Date

class DurationTest extends GroovyTestCase {
    void testFixedDurationArithmetic() {
        use(TimeCategory) {
            def oneDay = 2.days - 1.day
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000): \
                "Expected ${24 * 60 * 60 * 1000} but was ${oneDay.toMilliseconds()}"
            
            oneDay = 2.days - 1.day + 24.hours - 1440.minutes
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000): \
                "Expected ${24 * 60 * 60 * 1000} but was ${oneDay.toMilliseconds()}"
        }
   }

    void testDurationArithmetic() {
        use(TimeCategory) {
            // add two durations
            def twoMonthsA = 1.month + 1.month
            // subtract two absolute dates to get a duration
            def twoMonthsB = 2.months.from.now - 0.months.from.now
            assert twoMonthsA.toMilliseconds() == twoMonthsB.toMilliseconds()

            // add two durations
            def monthAndWeekA = 1.month + 1.week
            // add absolute date and a duration
            def monthAndWeekB = 1.month.from.now + 1.week - 0.months.from.now
            assert monthAndWeekA.toMilliseconds() == monthAndWeekB.toMilliseconds()
        }
    }

    void testDatumDependantArithmetic() {
        use(TimeCategory) {
            def now = new Date()
            def then = (now + 1.month) + 1.week
            def week = then - (now + 1.month)
            assert week.toMilliseconds() == (7 * 24 * 60 * 60 * 1000): \
                "Expected ${7 * 24 * 60 * 60 * 1000} but was ${week.toMilliseconds()}"
        }
    }
}

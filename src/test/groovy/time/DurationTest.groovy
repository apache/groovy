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
            def nowOffset = (new Date()).daylightSavingsOffset

            // add two durations
            def twoMonthsA = 1.month + 1.month
            // subtract dates which are two months apart
            def offset = 2.months.from.now.daylightSavingsOffset - 0.months.from.now.daylightSavingsOffset
            def twoMonthsB = 2.months.from.now + offset - 0.months.from.now
            assertEquals "Two months should equal difference between two fates two months apart",
                (twoMonthsA - nowOffset).toMilliseconds(), twoMonthsB.toMilliseconds()

            // add two durations
            def monthAndWeekA = 1.month + 1.week
            // subtract absolute date and a duration from another absolute date
            offset = (1.month.from.now + 1.week).daylightSavingsOffset - 0.months.from.now.daylightSavingsOffset
            def monthAndWeekB = 1.month.from.now + 1.week + offset - 0.months.from.now
            assertEquals "A week and a month absolute duration should be the same as the difference between two dates that far apart",
                (monthAndWeekA - nowOffset).toMilliseconds(), monthAndWeekB.toMilliseconds()
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

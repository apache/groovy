package groovy.time

import groovy.time.TimeCategory
import static java.util.Calendar.*

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

    void testDurationToString() {
        use(TimeCategory) {
            def duration = 4.days + 2.hours + 5.minutes + 12.milliseconds

            assert "4 days, 2 hours, 5 minutes, 0.012 seconds" == duration.toString()
        }
    }

    void testDurationArithmetic() {
        use(TimeCategory) {
            //def nowOffset = (new Date()).daylightSavingsOffset
            def nowOffset = 0.months.from.now.daylightSavingsOffset

            // add two durations
            def twoMonthsA = 1.month + 1.month
            // two months from an absolute day
			def twoMonthsB = new Date(0) + 2.months + 2.days // for Feb.
			
            assertEquals "Two months absolute duration should be the same as the difference between two dates two months apart\n",
                twoMonthsA.toMilliseconds(), twoMonthsB.time

            // add two durations
            def monthAndWeekA = 1.month + 1.week
            // add absolute date and a duration
			def monthAndWeekB = new Date(0) + 1.week + 1.month

            assertEquals "A week and a month absolute duration should be the same as the difference between two dates that far apart\n",
                monthAndWeekA.toMilliseconds(), monthAndWeekB.time
        }
    }
    
    void testMinugesAgo() { // See GROOVY-3687
    	use ( TimeCategory ) {
	    	def now = Calendar.getInstance()
	    	def before = 10.minutes.ago
	    	now.add( Calendar.MINUTE, -11 )
			assertTrue "10.minutes.ago should not zero out the date", 
				now.timeInMillis < before.time
				
			now = Calendar.getInstance()
			now.add( Calendar.MINUTE, -10 )
			assertTrue "10.minutes.ago should be older than 'now - 10 minutes'", 
				now.timeInMillis >= before.time
    	}
    }
    
    void testFromNow() {
    	use ( TimeCategory ) {
	    	def now = Calendar.getInstance()
	    	now.add( MINUTE, 10 )
			def later = 10.minutes.from.now
	    	assertTrue "10.minutes.from.now should be later!", 
				now.timeInMillis <= later.time

			now = Calendar.getInstance() 
			now.add( MINUTE, 11 )
			assertTrue "10.minutes.from.now should be less calendar + 11 minutes", 
				now.timeInMillis > later.time

			now = Calendar.getInstance()
			now.add( WEEK_OF_YEAR, 3 )
			now.set( HOUR_OF_DAY, 0 )
			now.set( MINUTE, 0 )
			now.set( SECOND, 0 )
			now.set( MILLISECOND, 0 )
			later = 3.weeks.from.now
			assertEquals "weeks from now!", now.timeInMillis, later.time
    	}
    }

    void testDatumDependantArithmetic() {
        use(TimeCategory) {
            def start = new Date(961552080000)
            def then = (start + 1.month) + 1.week
            def week = then - (start + 1.month)
            assert week.toMilliseconds() == (7 * 24 * 60 * 60 * 1000): \
                "Expected ${7 * 24 * 60 * 60 * 1000} but was ${week.toMilliseconds()}"
        }
    }
}
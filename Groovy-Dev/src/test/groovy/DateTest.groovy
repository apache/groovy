package groovy

import static java.util.Calendar.*

class DateTest extends GroovyTestCase {
  
    void testNextPrevious() {
        def x = new Date()
        def y = x + 2
        
        assert x < y
        ++x
        --y
        
        assert x == y
        x += 2
        assert x > y
        
        println "have dates ${x} and ${y}"
    }
    
    void testDateRange() {
        
        def today = new Date()
        def later = today + 3
        
        def expected = [today, today + 1, today + 2, today + 3]
        
        def list = []
        for (d in today..later) {
            list << d
        }
        assert list == expected
    }

    void testDateIndex() {
        Date d = new GregorianCalendar(2002, FEBRUARY, 2).time
        assert d[MONTH] == FEBRUARY
        assert d[DAY_OF_WEEK] == SATURDAY
    }
}

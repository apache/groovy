import java.util.Date

class DateTest extends GroovyTestCase {
  
    void testNextPrevious() {
        x = new Date()
        y = x + 2
        
        assert x < y
        ++x
        --y
        
        assert x == y
        x += 2
        assert x > y
        
        println "have dates ${x} and ${y}"
    }
    
    void testDateRange() {
        
        today = new Date()
        later = today + 3
        
        expected = [today, today + 1, today + 2, today + 3]
        
        list = []
        for (d in today..later) {
            list << d
        }
        assert list == expected
    }
}

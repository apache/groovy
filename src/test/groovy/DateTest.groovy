import java.util.Date

class DateTest extends GroovyTestCase {
  
    void testNextPrevious() {
        x = new Date()
        y = x + 2
        
        assert x < y
        x++
        y--
        
        assert x == y
        x += 2
        assert x > y
        
        println "have dates ${x} and ${y}"
	}
}

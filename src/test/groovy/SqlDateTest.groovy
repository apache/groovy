package groovy;

import org.codehaus.groovy.runtime.TimeCategory

class SqlDateTest extends GroovyTestCase {

    void testIncrement() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        def oldOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)
        sqlDate++
        def newOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)

        assertTrue  "incrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date

        def diff = sqlDate.getTime() - rightNowMillis
        // adjust for daylight savings
        diff += (newOffset - oldOffset).toMilliseconds()
        assertEquals "incrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }

      void testDecrement() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        def oldOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)
        sqlDate--
        def newOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)

        assertTrue  "decrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        def diff = rightNowMillis - sqlDate.getTime()
        // adjust for daylight savings
        diff += (newOffset - oldOffset).toMilliseconds()
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
      
    void testPlusOperator() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        def oldOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)
        sqlDate += 1
        def newOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)

        assertTrue  "the plus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        def diff = sqlDate.getTime() - rightNowMillis
        // adjust for daylight savings
        diff += (newOffset - oldOffset).toMilliseconds()
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
    
    void testMinusOperator() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        def oldOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)
        sqlDate -= 1
        def newOffset = TimeCategory.getDaylightSavingsOffset(sqlDate)

        assertTrue  "the minus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        def diff = rightNowMillis - sqlDate.getTime()
        // adjust for daylight savings
        diff += (newOffset - oldOffset).toMilliseconds()
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
}

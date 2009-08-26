package groovy;

import groovy.time.TimeCategory

class SqlDateTest extends GroovyTestCase {

    void testIncrement() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate++
        def incOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        assertTrue "incrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date

        // difference adjusted for daylight savings
        def diff = (sqlDate.getTime() + incOffset) - (nowMillis + nowOffset)
        assertEquals "incrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }

      void testDecrement() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate--
        def decOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        assertTrue "decrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        // difference adjusted for daylight savings
        def diff = (nowMillis + nowOffset) - (sqlDate.getTime() + decOffset)

        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
      
    void testPlusOperator() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate += 1
        def incOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()

        assertTrue  "the plus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        // difference adjusted for daylight savings
        def diff = (sqlDate.getTime() + incOffset) - (nowMillis + nowOffset)
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
    
    void testMinusOperator() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate -= 1
        def decOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()

        assertTrue  "the minus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        // difference adjusted for daylight savings
        def diff = (nowMillis + nowOffset) - (sqlDate.getTime() + decOffset)
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
}

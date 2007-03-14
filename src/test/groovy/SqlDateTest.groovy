package groovy;

class SqlDateTest extends GroovyTestCase {

    void testIncrement() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        sqlDate++
        
        assertTrue  "incrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date

        def diff = sqlDate.getTime() - rightNowMillis
        // TODO adjust for daylight savings
        assertEquals "incrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }

      void testDecrement() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        sqlDate--
        
        assertTrue  "decrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        def diff = rightNowMillis - sqlDate.getTime()
        // TODO adjust for daylight savings
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
      
    void testPlusOperator() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        sqlDate += 1
        
        assertTrue  "the plus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        def diff = sqlDate.getTime() - rightNowMillis
        // TODO adjust for daylight savings
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
    
    void testMinusOperator() {
        def rightNowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(rightNowMillis)
        sqlDate -= 1
        
        assertTrue  "the minus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        def diff = rightNowMillis - sqlDate.getTime()
        // TODO adjust for daylight savings
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
}

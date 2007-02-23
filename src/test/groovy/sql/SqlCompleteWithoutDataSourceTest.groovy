package groovy.sql

import java.sql.DriverManager

/**
 * Tests the use of the Sql class using just a Connection 
 * rather than a DataSource
 */
class SqlCompleteWithoutDataSourceTest extends SqlCompleteTest {
    
    protected def newSql(String uri) {
        def driver = org.hsqldb.jdbcDriver
        println("Loading driver ${driver}")
        return new Sql(DriverManager.getConnection(uri))
    }
}

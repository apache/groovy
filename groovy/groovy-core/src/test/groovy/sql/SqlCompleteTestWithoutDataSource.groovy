package groovy.sql

import org.axiondb.jdbc.AxionDriver
import java.sql.DriverManager

/**
 * Tests the use of the Sql class using just a Connection 
 * rather than a DataSource
 */
class SqlCompleteTestWithoutDataSource extends SqlCompleteTest {
    
    protected newSql(String uri) {
        driver = AxionDriver
        println("Loading driver ${driver}")
        return new Sql(DriverManager.getConnection(uri))
    }
}

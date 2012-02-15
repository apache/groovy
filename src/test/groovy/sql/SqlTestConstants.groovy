package groovy.sql

// trick to handle pesky lower case class names
import org.hsqldb.jdbcDriver as Driver
import org.hsqldb.jdbc.jdbcDataSource as DataSource

class SqlTestConstants {
    //// legacy HSQLDB: "hsqldb:hsqldb:1.8.0.10"
    public static final String DB_URL_PREFIX = 'jdbc:hsqldb:mem:testDB'
    public static final String DB_URL_SUFFIX = ''
    public static final Class DB_DATASOURCE = DataSource
    public static final Class DB_DRIVER = Driver
    public static final String DB_USER = 'sa'
    public static final String DB_PASSWORD = ''
    public static final String DB_DS_KEY = 'database'

    //// new HSQLDB: group:"org.hsqldb", name:"hsqldb", version:"2.2.8", classifier:"jdk5"
    //// currently causing JVM crash under JDK15 on Bamboo CI server
//    public static final String DB_URL_PREFIX = 'jdbc:hsqldb:mem:testDB'
//    public static final String DB_URL_SUFFIX = ''
//    public static final Class DB_DATASOURCE = org.hsqldb.jdbc.JDBCDataSource
//    public static final Class DB_DRIVER = org.hsqldb.jdbc.JDBCDriver
//    public static final String DB_USER = 'sa'
//    public static final String DB_PASSWORD = ''
//    public static final String DB_DS_KEY = 'database'

    //// H2 database: "com.h2database:h2:1.3.164" currently not working for:
    //// SqlCallTest#testBuiltinStoredProcedureQuery (stored proc not supported or diff syntax)
    //// SqlCacheTest#* (proxy issues during setup)
//    public static final String DB_URL_PREFIX = 'jdbc:h2:mem:testDB'
//    public static final String DB_URL_SUFFIX = ';DB_CLOSE_DELAY=-1'
//    public static final Class DB_DATASOURCE = org.h2.jdbcx.JdbcDataSource
//    public static final Class DB_DRIVER = org.h2.Driver
//    public static final String DB_USER = 'sa'
//    public static final String DB_PASSWORD = ''
//    public static final String DB_DS_KEY = 'URL'
}
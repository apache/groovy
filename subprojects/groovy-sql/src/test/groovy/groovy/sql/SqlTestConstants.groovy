/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
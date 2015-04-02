/*
 * Copyright 2003-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
* Tests for groovy.sql.Sql.
*/
class SqlTest extends GroovyTestCase {

    void testConnectingToHsqlDB() {
        assertScript '''
// tag::sql_connecting[]
        import groovy.sql.Sql

        def url = 'jdbc:hsqldb:mem:yourDB'
        def user = 'sa'
        def password = ''
        def driver = 'org.hsqldb.jdbcDriver'
        def sql = Sql.newInstance(url, user, password, driver)

        // use 'sql' instance ...
// end::sql_connecting[]

        // test of a system table within HSQLDB
        assert sql.firstRow('SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS')[0] == 1

// tag::sql_connecting_close[]

        sql.close()
// end::sql_connecting_close[]
'''
    }

    void testConnectingUsingDataSource() {
        assertScript '''
        /*
        commented out as already on classpath
// tag::sql_connecting_grab[]
        @Grab('org.hsqldb:hsqldb:2.3.2')
        @GrabConfig(systemClassLoader=true)
        // create, use, and then close sql instance ...
// end::sql_connecting_grab[]
*/

// tag::sql_connecting_datasource[]
        import groovy.sql.Sql
        import org.hsqldb.jdbc.JDBCDataSource

        def dataSource = new JDBCDataSource(
            database: 'jdbc:hsqldb:mem:yourDB', user: 'sa', password: '')
        def sql = new Sql(dataSource)

        // use then close 'sql' instance ...
// end::sql_connecting_datasource[]

        // test of a system table within HSQLDB
        assert sql.firstRow('SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS')[0] == 1
        sql.close()
'''
    }

}

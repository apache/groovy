/*
 * Copyright 2003-2009 the original author or authors.
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

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

/**
 * Test Sql batch features
 *
 * @author Paul King
 */
class SqlBatchTest extends GroovyTestCase {
    Sql sql
    def personFood
    private others = ['Jean':'Gabin', 'Lino':'Ventura']

    void setUp() {
        DataSource ds = new org.hsqldb.jdbc.jdbcDataSource()
        ds.database = "jdbc:hsqldb:mem:foo" + getMethodName()
        ds.user = 'sa'
        ds.password = ''
        Connection con = ds.connection
        sql = new Sql(con)
        sql.execute("CREATE TABLE person ( id INTEGER, firstname VARCHAR, lastname VARCHAR, PRIMARY KEY (id))")

        // populate some data
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")
    }

    void testManualBatch() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        assert sql.connection.metaData.supportsBatchUpdates()
        sql.cacheConnection {connection ->
            try {
            connection.autoCommit = false
            def stmt = connection.createStatement()
            others.eachWithIndex {entry, index ->
                stmt.addBatch("insert into PERSON (id, firstname, lastname) values (${index + numRows + 1}, '$entry.key', '$entry.value')")
            }
            assert stmt.executeBatch() == [1, 1]
            connection.autoCommit = true
            } catch (Exception e) {
                e.printStackTrace()
                println e.dump()
            }
        }
        assert sql.rows("SELECT * FROM PERSON").size() == 5
    }

}

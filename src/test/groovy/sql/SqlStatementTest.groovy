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

/**
 * Unit test of Sql statement feature
 *
 * @author Paul King
 */
class SqlStatementTest extends GroovyTestCase {
    Sql sql

    void setUp() {
        DataSource ds = new org.hsqldb.jdbc.jdbcDataSource()
        ds.database = "jdbc:hsqldb:mem:foo" + getMethodName()
        ds.user = 'sa'
        ds.password = ''
        Connection con = ds.connection
        sql = new Sql(con)
        sql.execute("create table PERSON ( id integer, firstname varchar, lastname varchar )")

        // now let's populate the datasets
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")
        people.add(id: 4, firstname: "Jean", lastname: "Gabin")
        people.add(id: 5, firstname: "Lino", lastname: "Ventura")
    }

    void testWithStatement() {
        assert sql.rows("SELECT * FROM PERSON").size() == 5
        sql.withStatement{ it.maxRows = 3 }
        assert sql.rows("SELECT * FROM PERSON").size() == 3
    }
}

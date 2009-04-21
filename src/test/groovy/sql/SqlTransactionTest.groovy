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
 * Test Sql transaction features
 *
 * @author Paul King
 */
class SqlTransactionTest extends GroovyTestCase {
    Sql sql
    def personFood

    void setUp() {
        DataSource ds = new org.hsqldb.jdbc.jdbcDataSource()
        ds.database = "jdbc:hsqldb:mem:foo" + getMethodName()
        ds.user = 'sa'
        ds.password = ''
        Connection con = ds.connection
        sql = new Sql(con)
        sql.execute("CREATE TABLE person ( id INTEGER, firstname VARCHAR, lastname VARCHAR, PRIMARY KEY (id))")
        sql.execute("CREATE TABLE person_food ( personid INTEGER, food VARCHAR, FOREIGN KEY (personid) REFERENCES person(id))")

        // populate some data
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")
        people.add(id: 4, firstname: "Jean", lastname: "Gabin")
        people.add(id: 5, firstname: "Lino", lastname: "Ventura")
        personFood = sql.dataSet("PERSON_FOOD")
        personFood.add(personid: 1, food: "cheese")
        personFood.add(personid: 1, food: "wine")
        personFood.add(personid: 2, food: "chicken")
    }

    void testManualTransactionSuccess() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        sql.cacheConnection { connection ->
            connection.autoCommit = false
            personFood.add(personid: 3, food: "beef")
            personFood.add(personid: 4, food: "fish")
            sql.commit()
            connection.autoCommit = true
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 5
    }

    void testWithTransactionSuccess() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        sql.withTransaction {
            personFood.add(personid: 3, food: "beef")
            personFood.add(personid: 4, food: "fish")
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 5
    }

    void testManualTransactionRollback() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        sql.cacheConnection { connection ->
            connection.autoCommit = false
            def numAdds = 0
            try {
                personFood.add(personid: 5, food: "veg")
                numAdds++
                personFood.add(personid: 99, food: "mash")
                numAdds++      // should fail before here
                sql.commit()   // should never get here
                fail("Should have thrown an exception before now")
            } catch (SQLException se) {
                assert numAdds == 1
                assert se.message.contains('Integrity constraint violation')
                sql.rollback()
            }
            connection.autoCommit = true
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
    }

    void testWithTransactionRollback() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        try {
            sql.withTransaction { ->
                personFood.add(personid: 5, food: "veg")
                personFood.add(personid: 99, food: "mash") // should fail
            }
            fail("Should have thrown an exception before now")
        } catch (SQLException se) {
            assert se.message.contains('Integrity constraint violation')
        }
        println sql.rows("SELECT * FROM PERSON_FOOD")
        // TODO fix below, currently returning 4, transaction not working!
//        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
    }
}

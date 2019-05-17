/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.sql

import groovy.test.GroovyTestCase

import java.sql.Connection
import java.sql.SQLException

/**
 * Test Sql transaction features using a Sql built from a connection
 */
class SqlTransactionTestCase extends GroovyTestCase {

    Sql sql
    DataSet personFood

    protected Sql setUpSql() {
        throw new UnsupportedOperationException("Please provide setUpSql in derived class")
    }

    protected tryDrop(String tableName) {
        try {
           sql.execute("DROP TABLE $tableName".toString())
        } catch(Exception e){ }
    }

    void setUp() {
        sql = setUpSql()
        // drop them in this order due to FK constraint
        ["PERSON_FOOD", "PERSON"].each{ tryDrop(it) }

        sql.execute("CREATE TABLE person ( id INTEGER, firstname VARCHAR(10), lastname VARCHAR(10), PRIMARY KEY (id))")
        sql.execute("CREATE TABLE person_food ( personid INTEGER, food VARCHAR(10), FOREIGN KEY (personid) REFERENCES person(id))")

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
        sql.cacheConnection { Connection connection ->
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

    void testManualTransactionRollbackUsingSql() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        sql.cacheConnection { Connection connection ->
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
                assert se.message.toLowerCase().contains('integrity constraint violation')
                sql.rollback()
            }
            connection.autoCommit = true
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
    }

    void testManualTransactionRollbackUsingDataSet() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        personFood.cacheConnection { Connection connection ->
            connection.autoCommit = false
            def numAdds = 0
            try {
                personFood.add(personid: 5, food: "veg")
                numAdds++
                personFood.add(personid: 99, food: "mash")
                numAdds++      // should fail before here
                personFood.commit()   // should never get here
                fail("Should have thrown an exception before now")
            } catch (SQLException se) {
                assert numAdds == 1
                assert se.message.toLowerCase().contains('integrity constraint violation')
                personFood.rollback()
            }
            connection.autoCommit = true
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
    }

    void testWithTransactionRollbackUsingSql() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        def numAdds = 0
        try {
            sql.withTransaction { ->
                personFood.add(personid: 5, food: "veg")
                numAdds++
                personFood.add(personid: 99, food: "mash") // should fail
                numAdds++
            }
            fail("Should have thrown an exception before now")
        } catch (SQLException se) {
            assert numAdds == 1
            assert se.message.toLowerCase().contains('integrity constraint violation')
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
    }

    void testWithTransactionRollbackFromException() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        try {
            sql.withTransaction { ->
                personFood.add(personid: 5, food: "veg")
                throw new Exception("Force rollback")
            }
            fail("Should have thrown an exception before now")
        } catch (SQLException se) {
            assert se.message.toLowerCase().contains('unexpected exception during transaction')
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
    }

    void testWithTransactionRollbackUsingDataSet() {
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
        def numAdds = 0
        try {
            personFood.withTransaction { ->
                personFood.add(personid: 5, food: "veg")
                numAdds++
                personFood.add(personid: 99, food: "mash") // should fail
                numAdds++
            }
            fail("Should have thrown an exception before now")
        } catch (SQLException se) {
            assert numAdds == 1
            assert se.message.toLowerCase().contains('integrity constraint violation')
        }
        assert sql.rows("SELECT * FROM PERSON_FOOD").size() == 3
    }
}

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
import org.codehaus.groovy.runtime.InvokerHelper

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_USER

/**
 * Unit test of Sql cache feature 
 */
class SqlCacheTest extends GroovyTestCase {
    Sql sql
    Connection wrappedCon
    Connection con
    DataSource ds
    int prepareStatementExpectedCall
    int prepareStatementCallCounter
    int createStatementExpectedCall
    int createStatementCallCounter

    @Override
    void setUp() {
        ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER,
                password: DB_PASSWORD)
        con = ds.connection
        def methodOverride = [
                createStatement: {Object[] args ->
                    createStatementCallCounter++
                    assert !createStatementExpectedCall || createStatementCallCounter <= createStatementExpectedCall
                    InvokerHelper.invokeMethod(con, 'createStatement', args)
                },
                prepareStatement: {Object[] args ->
                    prepareStatementCallCounter++
                    assert !prepareStatementExpectedCall || prepareStatementCallCounter <= prepareStatementExpectedCall
                    InvokerHelper.invokeMethod(con, 'prepareStatement', args)
                }
        ]
        wrappedCon = ProxyGenerator.INSTANCE.instantiateDelegate(methodOverride, [Connection], con)
        sql = new Sql(wrappedCon)
        sql.execute("create table PERSON ( id INTEGER, firstname VARCHAR(10), lastname VARCHAR(10) )")
        sql.execute("create table FOOD ( id INTEGER, type VARCHAR(10), name VARCHAR(10))")
        sql.execute("create table PERSON_FOOD ( personid INTEGER, foodid INTEGER)")

        // now let's populate the datasets
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")
        people.add(id: 4, firstname: "Jean", lastname: "Gabin")
        people.add(id: 5, firstname: "Lino", lastname: "Ventura")

        def food = sql.dataSet("FOOD")
        food.add(id: 1, type: "cheese", name: "edam")
        food.add(id: 2, type: "cheese", name: "brie")
        food.add(id: 3, type: "cheese", name: "cheddar")
        food.add(id: 4, type: "drink", name: "beer")
        food.add(id: 5, type: "drink", name: "coffee")

        def person_food = sql.dataSet("PERSON_FOOD")
        person_food.add(personid: 1, foodid: 1)
        person_food.add(personid: 1, foodid: 4)
        person_food.add(personid: 2, foodid: 2)
        person_food.add(personid: 3, foodid: 5)
        person_food.add(personid: 4, foodid: 1)
        person_food.add(personid: 4, foodid: 2)
        person_food.add(personid: 4, foodid: 3)
        person_food.add(personid: 99, foodid: 99)

        prepareStatementCallCounter = 0
    }

    @Override
    void tearDown() {
        super.tearDown()
        sql.close()
    }

    /**
     * Validation of ConnectionWrapper expectation.
     */
    void testValidateWrappedConnectionStatementCall() {
        prepareStatementCallCounter = 0
        prepareStatementExpectedCall = 1
        try {
            wrappedCon.prepareStatement("SELECT * FROM PERSON")
            wrappedCon.prepareStatement("SELECT * FROM PERSON")
            fail("Exception must be raised")
        } catch (AssertionError e) {
            assert prepareStatementCallCounter == 2
        }
    }

    void testCachePreparedStatements() {
        prepareStatementCallCounter = 0
        prepareStatementExpectedCall = 3
        sql.cacheStatements { ->
            invokeQuery()
        }
        assert prepareStatementCallCounter == 3 // 3 diff statements
    }

    void testCacheConnection() {
        prepareStatementCallCounter = 0
        sql.cacheConnection {
            invokeQuery()
        }
        assert prepareStatementCallCounter == 13
    }

    void testNotCacheStatements() {
        prepareStatementCallCounter = 0
        invokeQuery()
        assert prepareStatementCallCounter == 13
    }

    private invokeQuery() {
        sql.eachRow("SELECT * FROM PERSON", []) { person ->
            sql.eachRow("SELECT * FROM PERSON_FOOD WHERE personid = ?", [person.id]) { food ->
                sql.firstRow("SELECT * FROM FOOD WHERE id = ?", [food.foodid])
            }
        }
    }

    /**
     * We here use a wrapper for counting java.sql.Connection.prepareStatement(java.lang.String)
     * calls.
     * When caching is on, same request must not cause a new prepareStatement call :
     * prepareStatementCallCounter must not be increased.
     *
     * When caching is off, same request causes a new prepareStatement call :
     * prepareStatementCallCounter must be increased.
     *
     */
    void testManuallyControlledCaching() {
        sql.cacheStatements = true
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert prepareStatementCallCounter == 1
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert prepareStatementCallCounter == 1
        sql.firstRow("SELECT * FROM FOOD WHERE id = ?", [3])
        assert prepareStatementCallCounter == 2
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert prepareStatementCallCounter == 2

        // Stop caching
        sql.cacheStatements = false
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert prepareStatementCallCounter == 3
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert prepareStatementCallCounter == 4

        // Statements
        sql.cacheStatements = true
        createStatementCallCounter = 0
        sql.firstRow("SELECT * FROM PERSON")
        assert createStatementCallCounter == 1
        sql.firstRow("SELECT * FROM PERSON")
        assert createStatementCallCounter == 1
    }

    /**
     * @see #testManuallyControlledCaching()
     */
    void testNoCaching() {
        // preparedStatements
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert prepareStatementCallCounter == 1
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert prepareStatementCallCounter == 2

        // Statements
        createStatementCallCounter = 0
        sql.firstRow("SELECT * FROM PERSON")
        assert createStatementCallCounter == 1
        sql.firstRow("SELECT * FROM PERSON")
        assert createStatementCallCounter == 2
    }

    /**
     * When caching is on, data source connection must be kept and not released.
     * Use a wrapped delegate for counting javax.sql.DataSource.getConnection() calls.
     * When caching is off, javax.sql.DataSource.getConnection() must be called each time.
     */
    void testManuallyControlledCachingWithDataSource() {
        def connectionCallNumber = 0
        def methodOverride = [getConnection:{connectionCallNumber++; ds.getConnection()}]
        DataSource wrappedDs = ProxyGenerator.INSTANCE.instantiateDelegate(methodOverride, [DataSource], ds)
        sql = new Sql(wrappedDs)
        sql.cacheStatements = true
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert connectionCallNumber == 1
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert connectionCallNumber == 1
        sql.firstRow("SELECT * FROM FOOD WHERE id = ?", [3])
        assert connectionCallNumber == 1
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        // Stop caching
        sql.cacheStatements = false
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert connectionCallNumber == 2
        sql.firstRow("SELECT * FROM PERSON WHERE lastname NOT like ? ", ['%a%'])
        assert connectionCallNumber == 3
    }

    void testExceptionIsNotSwallowedCachingStatements() {
        checkExceptionIsNotSwallowedCachingStatements(new Exception('test.exception'))
    }

    void testSQLExceptionIsNotSwallowedCachingStatements() {
        checkExceptionIsNotSwallowedCachingStatements(new SQLException('test.exception'))
    }

    private checkExceptionIsNotSwallowedCachingStatements(Throwable t) {
        try {
            sql.cacheStatements {
                sql.eachRow("SELECT * FROM PERSON", []) {
                    throw t
                }
            }
            fail('Exception must be raised !')
        } catch (Exception e) {
            assert e.message == t.message
            assert !sql.cacheStatements
        }
    }

    void testExceptionIsNotSwallowedCachingConnection() {
        checkExceptionIsNotSwallowedCachingConnection(new Exception('test.exception'))
    }

    void testSQLExceptionIsNotSwallowedCachingConnection() {
        checkExceptionIsNotSwallowedCachingConnection(new SQLException('test.exception'))
    }

    private checkExceptionIsNotSwallowedCachingConnection(Throwable t) {
        try {
            sql.cacheConnection {
                sql.eachRow("SELECT * FROM PERSON", []) {
                    throw t
                }
            }
            fail('Exception must be raised !')
        } catch (Exception e) {
            assert e.message == t.message
            assert !sql.cacheStatements
        }
    }

}

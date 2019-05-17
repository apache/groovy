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

import javax.sql.DataSource
import java.sql.Connection

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_USER

/**
 * This is more of a sample program than a unit test and is here as an easy
 * to read demo of GDO. The actual full unit test case is in SqlCompleteTest
 */
class SqlTest extends GroovyTestCase {

    private sql

    void setUp() {
        sql = createSql()
    }

    void testSqlQuery() {
        sql.eachRow("select * from PERSON") { println("Hello ${it.firstname} ${it.lastname}") }
    }

    void testQueryUsingColumnIndex() {
        def answer = null
        sql.eachRow("select count(*) from PERSON") { answer = it[0] }
        println "Found the count of ${answer}"
        assert answer == 3
    }

    void testQueryUsingNegativeColumnIndex() {
        def first = null
        def last = null
        sql.eachRow("select firstname, lastname from PERSON where firstname='James'") { row ->
            first = row[-2]
            last = row[-1]
        }
        println "Found name ${first} ${last}"
        assert first == "James"
        assert last == "Strachan"
    }

    void testSqlQueryWithWhereClause() {
        def foo = "drink"
        sql.eachRow("select * from FOOD where type=${foo}") { println("Drink ${it.name}") }
    }

    void testEachRowWithWhereClauseWith2Arguments() {
        def foo = "cheese"
        def bar = "edam"
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { println("Found cheese ${it.name}") }
    }

    void testFirstRowWithWhereClauseWith2Arguments() {
        def foo = "cheese"
        def bar = "edam"
        def result = sql.firstRow("select * from FOOD where type=${foo} and name != ${bar}")
        assert result.name == 'brie'
    }

    void testSqlQueryWithIncorrectlyQuotedDynamicExpressions() {
        def foo = "cheese"
        def bar = "edam"
        sql.eachRow("select * from FOOD where type='${foo}' and name != '${bar}'") { println("Found cheese ${it.name}") }
    }

    void testDataSet() {
        def people = sql.dataSet("PERSON")
        people.each { println("Hey ${it.firstname}") }
    }

    void testDataSetWithClosurePredicate() {
        def food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { println("Cheese ${it.name}") }
    }

    void testExecuteUpdate() {
        def foo = 'food-drink'
        def bar = 'guinness'
        def nRows = sql.executeUpdate("update FOOD set type=? where name=?", [foo, bar]);
        if (nRows == 0) {
            sql.executeUpdate("insert into FOOD (type,name) values (${foo},${bar})");
        }
    }

    void testExecuteInsert() {
        def value = 'log entry'
        if (sql.dataSource.connection.metaData.supportsGetGeneratedKeys()) {
            def keys = sql.executeInsert('insert into LOG (value) values (?)', [value])
            assert 1 == keys.size()
        } else {
            def count = sql.executeUpdate('insert into LOG (value) values (?)', [value])
            assert 1 == count
        }
    }

    void testExecuteInsertWithColumnNamesListParams() {
        def value = 'log entry'
        if (sql.dataSource.connection.metaData.supportsGetGeneratedKeys()) {
            def keys = sql.executeInsert('insert into LOG (value) values (?)', [value], ['ID'])
            assert keys == [[ID:0]]
        }
    }

    void testExecuteInsertWithColumnNamesVarargParams() {
        def value = 'log entry'
        if (sql.dataSource.connection.metaData.supportsGetGeneratedKeys()) {
            def keys = sql.executeInsert('insert into LOG (value) values (?)', ['ID'] as String[], value)
            assert keys == [[ID:0]]
        }
    }

    void testExecuteInsertWithColumnNamesNoVarargs() {
        if (sql.dataSource.connection.metaData.supportsGetGeneratedKeys()) {
            def keys = sql.executeInsert("insert into LOG (value) values 'log entry'", ['ID'] as String[])
            assert keys == [[ID:0]]
        }
    }

    void testExecuteInsertWithColumnNamesGString() {
        def value = 'log entry'
        if (sql.dataSource.connection.metaData.supportsGetGeneratedKeys()) {
            def keys = sql.executeInsert("insert into LOG (value) values $value", ['ID'])
            assert keys == [[ID:0]]
        } else {
            def count = sql.executeUpdate("insert into LOG (value) values $value")
            assert 1 == count
        }
    }

    void testExecuteWithProcessResultsClosure() {
        sql.execute("insert into LOG (value) values ('log entry')") {
            isResultSet, result ->
                assert isResultSet == false && result == 1
        }
    }

    void testMetaData() {
        sql.eachRow('select * from PERSON') {
            assert it[0] != null
            assert it.getMetaData() != null
        }
    }

    void testSubClass() {
        def sub = new SqlSubclass(sql)
        def res = null
        def data = []
        sql.eachRow('select firstname from PERSON') {
            data << it.firstname
        }
        try {
            res = sub.rowsCursor('select * from PERSON')
            while (res.next()) {
                assert data.remove(res.firstname)
            }
            assert data.isEmpty()
        } finally {
            if (res)
                res.close()
        }
        sub.rows('select * from PERSON') { metaData ->
            assert sub.savedConnection && !sub.savedConnection.isClosed()
            data = (1..metaData.columnCount).collect {
                metaData.getColumnName(it).toLowerCase()
            }
        }
        assert data.size() == 2 && !(data - ['firstname', 'lastname'])
    }

    void testCallMethodFromObjectOnGroovyResultSet() {
        sql.eachRow('select * from PERSON') {
            println it.toString()
            println it.hashCode()
        }
    }

    private createSql() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER,
                password: DB_PASSWORD)
        sql = new Sql(ds.connection)
        def sql = new Sql(ds)

        sql.execute("create table PERSON ( firstname VARCHAR(10), lastname VARCHAR(10) )")
        sql.execute("create table FOOD ( type VARCHAR(10), name VARCHAR(10))")
        sql.execute("create table LOG ( value VARCHAR(20), ID INTEGER IDENTITY)")

        // now let's populate the datasets
        def people = sql.dataSet("PERSON")
        people.add(firstname: "James", lastname: "Strachan")
        people.add(firstname: "Bob", lastname: "Mcwhirter")
        people.add(firstname: "Sam", lastname: "Pullara")

        def food = sql.dataSet("FOOD")
        food.add(type: "cheese", name: "edam")
        food.add(type: "cheese", name: "brie")
        food.add(type: "cheese", name: "cheddar")
        food.add(type: "drink", name: "beer")
        food.add(type: "drink", name: "coffee")

        return sql
    }
}

class SqlSubclass extends Sql {
    Connection savedConnection

    SqlSubclass(Sql base) {
        super(base)
    }

    def rowsCursor(String sql) {
        def rs = executeQuery(sql)
        return new GroovyResultSetProxy(rs).getImpl()
    }

    @Override
    void setInternalConnection(Connection conn) {
        savedConnection = conn
    }
}

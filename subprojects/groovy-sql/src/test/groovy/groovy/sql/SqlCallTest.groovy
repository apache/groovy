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
import java.sql.CallableStatement

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_USER

/**
 * Test Sql transaction features using a Sql built from a connection
 * along with testing Stored Procedure calls
 */
class SqlCallTest extends GroovyTestCase {

    Sql sql

    protected Sql setUpSql() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER,
                password: DB_PASSWORD)
        return new Sql(ds.connection)
    }

    protected tryDrop(String tableName) {
        try {
           sql.execute("DROP TABLE $tableName".toString())
        } catch(Exception e){ }
    }

    @Override
    void setUp() {
        sql = setUpSql()
        ["PERSON"].each{ tryDrop(it) }

        sql.execute("CREATE TABLE person ( id INTEGER, firstname VARCHAR(10), lastname VARCHAR(10), PRIMARY KEY (id))")

        // populate some data
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")
        people.add(id: 4, firstname: "Jean", lastname: "Gabin")
        people.add(id: 5, firstname: "Lino", lastname: "Ventura")

        //Syntax for HSQLDB stored procedure creation
        //Result goes into answer out parameter
        sql.execute """
          CREATE PROCEDURE FindByFirst(IN p_firstname VARCHAR(10), OUT answer VARCHAR(25))
            READS SQL DATA
            BEGIN ATOMIC
              DECLARE lastN VARCHAR(10);
              SELECT lastname into lastN FROM person where firstname = p_firstname;
              SET answer = ('Last Name is ' + lastN);
            END;
        """

        //Syntax for HSQLDB stored procedure creation
        //Results go into ResultSet
        sql.execute """
          CREATE PROCEDURE FindAllByFirst(IN p_firstname VARCHAR(10))
            READS SQL DATA DYNAMIC RESULT SETS 1
            BEGIN ATOMIC
              DECLARE resultSet CURSOR WITHOUT HOLD WITH RETURN FOR
                SELECT id, firstname, lastname FROM person where firstname like (p_firstname + '%')
                  order by id asc
                FOR READ ONLY;
              OPEN resultSet;
            END;
        """

        //Results in both an Out Parameter and a ResultSet
        sql.execute """
          CREATE PROCEDURE FindAllByFirstWithTotal(IN p_firstname VARCHAR(10), OUT answer VARCHAR(25))
            READS SQL DATA DYNAMIC RESULT SETS 1
            BEGIN ATOMIC
              DECLARE total INTEGER;
              DECLARE resultSet CURSOR WITHOUT HOLD WITH RETURN FOR
                SELECT id, firstname, lastname FROM person where firstname like (p_firstname + '%')
                  order by id asc
                FOR READ ONLY;
              OPEN resultSet;
              SELECT count(*) into total FROM person where firstname like (p_firstname + '%');
              SET answer = ('Found total ' + total);
            END;
        """

        //Results in both multiple Out Parameters and multiple ResultSets
        sql.execute """
          CREATE PROCEDURE FindAllByFirstAndFindAllByLastWithTotals(IN p_firstname VARCHAR(10), IN p_lastname VARCHAR(10), OUT first_answer VARCHAR(25), OUT last_answer VARCHAR(25))
            READS SQL DATA DYNAMIC RESULT SETS 2
            BEGIN ATOMIC
              DECLARE firstTotal INTEGER;
              DECLARE lastTotal INTEGER;
              DECLARE firstResultSet CURSOR WITHOUT HOLD WITH RETURN FOR
                SELECT id, firstname, lastname FROM person where firstname like (p_firstname + '%')
                  order by id asc
                FOR READ ONLY;
              DECLARE lastResultSet CURSOR WITHOUT HOLD WITH RETURN FOR
                SELECT id, firstname, lastname FROM person where lastname like (p_lastname + '%')
                  order by id asc
                FOR READ ONLY;
              OPEN firstResultSet;
              OPEN lastResultSet;
              SELECT count(*) into firstTotal FROM person where firstname like (p_firstname + '%');
              SELECT count(*) into lastTotal FROM person where lastname like (p_lastname + '%');
              SET first_answer = ('Found total ' + firstTotal);
              SET last_answer = ('Found total ' + lastTotal);
            END;
        """
    }

    @Override
    void tearDown() {
        super.tearDown()
        sql.close()
    }

    void testBuiltinStoredProcedureQuery() {
        def pi = sql.firstRow("call PI()")['@p0']
        assert pi.toString().startsWith('3.14159')
    }

    void testSelectWithFunction() {
        def result = sql.firstRow("select firstname, lastname, CHAR_LENGTH(firstname) as firstsize from PERSON")
        assert result.firstname == 'James' && result.firstsize == 5
    }

    void testCallUsingOutParameters() {
        String found
        sql.call '{call FindByFirst(?, ?)}', ['James', Sql.VARCHAR], { ans ->
            found = ans
        }
        assert found == 'Last Name is Strachan'
    }

    void testCallGStringUsingOutParameters() {
        def first = 'James'
        String found
        sql.call "{call FindByFirst($first, ${Sql.VARCHAR})}", { ans ->
            found = ans
        }
        assert found == 'Last Name is Strachan'
    }

    void testStoredProcedureRows() {
        List<GroovyRowResult> rows = sql.rows '{call FindAllByFirst(?)}', ['J']
        assert rows.size() == 2
        assert rows[0].id == 1
        assert rows[0].firstname == 'James'
        assert rows[0].lastname == 'Strachan'
        assert rows[1].id == 4
        assert rows[1].firstname == 'Jean'
        assert rows[1].lastname == 'Gabin'
    }

    void testStoredProcedureRowsGString() {
        def first = 'J'
        List<GroovyRowResult> rows = sql.rows "{call FindAllByFirst($first)}"
        assert rows.size() == 2
        assert rows[0].id == 1
        assert rows[0].firstname == 'James'
        assert rows[0].lastname == 'Strachan'
        assert rows[1].id == 4
        assert rows[1].firstname == 'Jean'
        assert rows[1].lastname == 'Gabin'
    }

    void testStoredProcedureExecuteWithProcessResultsClosure() {
        boolean first = true
        sql.execute "{call FindAllByFirst('J')}", { isResultSet, result ->
            if (first) {
                first = false
                assert !isResultSet && result == 0
            } else {
                assert isResultSet && result == [[ID:1, FIRSTNAME:'James', LASTNAME:'Strachan'], [ID:4, FIRSTNAME:'Jean', LASTNAME:'Gabin']]
            }
        }
    }

    void testCallWithRows() {
        String found
        List<GroovyRowResult> rows = sql.callWithRows '{call FindAllByFirstWithTotal(?, ?)}', ['J', Sql.VARCHAR], { total ->
            found = total
        }
        assert found == 'Found total 2'

        assert rows.size() == 2
        assert rows[0].id == 1
        assert rows[0].firstname == 'James'
        assert rows[0].lastname == 'Strachan'
        assert rows[1].id == 4
        assert rows[1].firstname == 'Jean'
        assert rows[1].lastname == 'Gabin'
    }

    void testCallWithRowsGString() {
        def first = 'J'
        String found
        List<GroovyRowResult> rows = sql.callWithRows "{call FindAllByFirstWithTotal($first, ${Sql.VARCHAR})}", { total ->
            found = total
        }
        assert found == 'Found total 2'

        assert rows.size() == 2
        assert rows[0].id == 1
        assert rows[0].firstname == 'James'
        assert rows[0].lastname == 'Strachan'
        assert rows[1].id == 4
        assert rows[1].firstname == 'Jean'
        assert rows[1].lastname == 'Gabin'
    }

    void testCallWithAllRows() {
        String foundFirst
        String foundLast
        List<GroovyRowResult> firstRows
        List<GroovyRowResult> lastRows

        List<List<GroovyRowResult>> rowList = sql.callWithAllRows '{call FindAllByFirstAndFindAllByLastWithTotals(?, ?, ?, ?)}', ['J', 'V', Sql.VARCHAR, Sql.VARCHAR], { firstTotal, lastTotal ->
            foundFirst = firstTotal
            foundLast = lastTotal
        }
        assert foundFirst == 'Found total 2'
        assert foundLast == 'Found total 1'

        assert rowList.size() == 2

        firstRows = rowList.get(0)

        assert firstRows[0].id == 1
        assert firstRows[0].firstname == 'James'
        assert firstRows[0].lastname == 'Strachan'
        assert firstRows[1].id == 4
        assert firstRows[1].firstname == 'Jean'
        assert firstRows[1].lastname == 'Gabin'

        lastRows = rowList.get(1)

        assert lastRows[0].id == 5
        assert lastRows[0].firstname == 'Lino'
        assert lastRows[0].lastname == 'Ventura'

    }

    void testCallWithAllRowsGString() {
        def first = 'J'
        def last = 'V'

        String foundFirst
        String foundLast
        List<GroovyRowResult> firstRows
        List<GroovyRowResult> lastRows

        List<List<GroovyRowResult>> rowList = sql.callWithAllRows "{call FindAllByFirstAndFindAllByLastWithTotals($first, $last, ${Sql.VARCHAR}, ${Sql.VARCHAR})}", { firstTotal, lastTotal ->
            foundFirst = firstTotal
            foundLast = lastTotal
        }
        assert foundFirst == 'Found total 2'
        assert foundLast == 'Found total 1'

        assert rowList.size() == 2

        firstRows = rowList.get(0)

        assert firstRows[0].id == 1
        assert firstRows[0].firstname == 'James'
        assert firstRows[0].lastname == 'Strachan'
        assert firstRows[1].id == 4
        assert firstRows[1].firstname == 'Jean'
        assert firstRows[1].lastname == 'Gabin'

        lastRows = rowList.get(1)

        assert lastRows[0].id == 5
        assert lastRows[0].firstname == 'Lino'
        assert lastRows[0].lastname == 'Ventura'
    }

    void testCallWithStatementCaching() {
        String sqlText = '{call FindByFirst(?, ?)}'
        CallableStatement statement = null
        sql.withStatement { statement = (CallableStatement)it }

        sql.cacheStatements = false
        sql.call sqlText, ['James', Sql.VARCHAR]
        assert statement.isClosed()
        assert sql.@statementCache.isEmpty()

        sql.cacheStatements = true
        sql.call sqlText, ['James', Sql.VARCHAR]
        assert !statement.isClosed()
        assert sql.@statementCache.containsKey(sqlText)
        assert sql.@statementCache[sqlText].is(statement)
    }

    void testCallWithRowsStatementCaching() {
        String sqlText = '{call FindByFirst(?, ?)}'
        CallableStatement statement = null
        sql.withStatement { statement = (CallableStatement)it }

        sql.cacheStatements = false
        sql.call sqlText, ['James', Sql.VARCHAR], { ans ->
            // no-op
        }
        assert statement.isClosed()
        assert sql.@statementCache.isEmpty()

        sql.cacheStatements = true
        sql.call sqlText, ['James', Sql.VARCHAR], { ans ->
            // no-op
        }
        assert !statement.isClosed()
        assert sql.@statementCache.containsKey(sqlText)
        assert sql.@statementCache[sqlText].is(statement)
    }

}

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

import groovy.test.GroovyAssert

import javax.sql.DataSource

import static groovy.sql.SqlTestConstants.*

/**
 * Test Sql batch features
 */
class SqlBatchTest extends GroovyTestCase {
    Sql sql
    private final others = ['Jean':'Gabin', 'Lino':'Ventura']

    void setUp() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER,
                password: DB_PASSWORD)
        sql = new Sql(ds.connection)
        sql.execute("CREATE TABLE person ( id INTEGER, firstname VARCHAR(10), lastname VARCHAR(10), PRIMARY KEY (id))")

        // populate some data
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")

        // uncomment to see logging (or set FINEST in <JRE_HOME>/lib/logging.properties)
//        def logger = java.util.logging.Logger.getLogger('groovy.sql')
//        logger.level = java.util.logging.Level.FINE
//        logger.addHandler(new java.util.logging.ConsoleHandler(level: java.util.logging.Level.FINE))
    }

    void testManualBatch() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        assert sql.connection.metaData.supportsBatchUpdates()
        sql.cacheConnection {connection ->
            try {
            connection.autoCommit = false
            def stmt = connection.createStatement()
            others.eachWithIndex {k, v, index ->
                def id = index + numRows + 1
                stmt.addBatch("insert into PERSON (id, firstname, lastname) values ($id, '$k', '$v')")
            }
            assert stmt.executeBatch() == [1, 1]
            connection.autoCommit = true
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
        assert sql.rows("SELECT * FROM PERSON").size() == 5
    }

    void testWithBatch() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        def result = sql.withBatch { stmt ->
            others.eachWithIndex { k, v, index ->
                def id = index + numRows + 1
                stmt.addBatch("insert into PERSON (id, firstname, lastname) values ($id, '$k', '$v')")
            }
        }
        assert result == [1, 1]
        assert sql.rows("SELECT * FROM PERSON").size() == 5
    }

    void testWithBatchHavingSize() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        def myOthers = ['f4':'l4','f5':'l5','f6':'l6','f7':'l7']
        def result = sql.withBatch(3) { stmt ->
            myOthers.eachWithIndex { k, v, index ->
                def id = index + numRows + 1
                stmt.addBatch("insert into PERSON (id, firstname, lastname) values ($id, '$k', '$v')")
            }
        }
        assert result == [1] * myOthers.size()
        assert sql.rows("SELECT * FROM PERSON").size() == numRows + myOthers.size()
        // end result the same as if no batching was in place but logging should show:
        // FINE: Successfully executed batch with 3 command(s)
        // FINE: Successfully executed batch with 1 command(s)
    }

    void testWithBatchHavingSizeUsingPreparedStatement() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        def myOthers = ['f4':'l4','f5':'l5','f6':'l6','f7':'l7']
        def result = sql.withBatch(3, "insert into PERSON (id, firstname, lastname) values (?, ?, ?)") { ps ->
            myOthers.eachWithIndex { k, v, index ->
                def id = index + numRows + 1
                ps.addBatch(id, k, v)
            }
        }
        assert result == [1] * myOthers.size()
        assert sql.rows("SELECT * FROM PERSON").size() == numRows + myOthers.size()
        // end result the same as if no batching was in place but logging should show:
        // FINE: Successfully executed batch with 3 command(s)
        // FINE: Successfully executed batch with 1 command(s)
    }

    void testWithBatchHavingSizeSameSizeAsStatements() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        def myOthers = ['f4':'l4','f5':'l5','f6':'l6','f7':'l7']
        def result = sql.withBatch(myOthers.size(), "insert into PERSON (id, firstname, lastname) values (?, ?, ?)") { ps ->
            myOthers.eachWithIndex { k, v, index ->
                def id = index + numRows + 1
                ps.addBatch(id, k, v)
            }
        }
        assert result == [1] * myOthers.size()
        assert sql.rows("SELECT * FROM PERSON").size() == numRows + myOthers.size()
        // end result the same as if no batching was in place but logging should show:
        // FINE: Successfully executed batch with 4 command(s)
    }

    void testWithBatchNothingAddedToBatch() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3

        def result = sql.withBatch { ps ->
            // Add nothing
        }
        assert result == [] as int[]
    }

    void testWithBatchWithPreparedStatementNothingAddedToBatch() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3

        // If you create a PreparedStatement you have to use it - or else HSQL throws an exception
        GroovyAssert.shouldFail {
            sql.withBatch(3, "insert into PERSON (id, firstname, lastname) values (?, ?, ?)") { ps ->
                // Add nothing - not a good practice at all...
            }
        }
    }

    void testWithBatchInsideWithTransaction() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        def myOthers = ['f4':'l4','f5':'l5','f6':'l6','f7':'l7']
        shouldFail(IllegalStateException) {
            sql.withTransaction {
                sql.withBatch(2, "insert into PERSON (id, firstname, lastname) values (?, ?, ?)") { ps ->
                    myOthers.eachWithIndex { k, v, index ->
                        def id = index + numRows + 1
                        if (k == 'f6') throw new IllegalStateException('BOOM')
                        ps.addBatch(id, k, v)
                    }
                }
            }
        }
        assert sql.rows("SELECT * FROM PERSON").size() == numRows
    }

    void testWithBatchHavingSizeUsingPreparedStatementNamedParams() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        def myOthers = ['f4':'l4','f5':'l5','f6':'l6','f7':'l7']
        def result = sql.withBatch(3, "insert into PERSON (id, firstname, lastname) values (?.id, :first, :last)") { ps ->
            myOthers.eachWithIndex { k, v, index ->
                def id = index + numRows + 1
                ps.addBatch(id:id, first:k, last:v)
            }
        }
        assert result == [1] * myOthers.size()
        assert sql.rows("SELECT * FROM PERSON").size() == numRows + myOthers.size()
        // end result the same as if no batching was in place but logging should show:
        // FINE: Successfully executed batch with 3 command(s)
        // FINE: Successfully executed batch with 1 command(s)
    }

    void testWithBatchHavingSizeUsingPreparedStatementNamedOrdinalParams() {
        def numRows = sql.rows("SELECT * FROM PERSON").size()
        assert numRows == 3
        def myOthers = ['f4':'l4','f5':'l5','f6':'l6','f7':'l7']
        def result = sql.withBatch(3, "insert into PERSON (id, firstname, lastname) values (?1, ?2.first, ?2.last)") { ps ->
            myOthers.eachWithIndex { k, v, index ->
                def id = index + numRows + 1
                ps.addBatch(id, [first:k, last:v])
            }
        }
        assert result == [1] * myOthers.size()
        assert sql.rows("SELECT * FROM PERSON").size() == numRows + myOthers.size()
        // end result the same as if no batching was in place but logging should show:
        // FINE: Successfully executed batch with 3 command(s)
        // FINE: Successfully executed batch with 1 command(s)
    }

    void testWithBatchClosesStatement() {
        // withBatch(int, Closure) should close connection even when statement caching is enabled
        // since it is not cached because there is no sql text to use as the cache key.
        BatchingStatementWrapper wrapper = null
        sql.cacheStatements = true
        sql.withBatch(1) { stmt ->
            wrapper = (BatchingStatementWrapper)stmt
            stmt.addBatch("insert into PERSON (id, firstname, lastname) values (999, 'Test', 'Closes')")
        }
        assert wrapper.@delegate.isClosed()
    }

    void testWithBatchClosureClosesOrCachesStatement() {
        String sqlText = 'insert into PERSON (id, firstname, lastname) values (?, ?, ?)'
        BatchingPreparedStatementWrapper wrapper = null

        sql.cacheStatements = false
        sql.withBatch(20, sqlText) { ps ->
            wrapper = (BatchingPreparedStatementWrapper)ps
            ps.addBatch(111, 'Test1', 'Closes1')
            ps.addBatch(222, 'Test2', 'Closes2')
        }
        assert wrapper.@delegate.isClosed()

        sql.cacheStatements = true
        sql.withBatch(20, sqlText) { ps ->
            wrapper = (BatchingPreparedStatementWrapper)ps
            ps.addBatch(333, 'Test3', 'Closes3')
            ps.addBatch(444, 'Test4', 'Closes4')
        }
        assert !wrapper.@delegate.isClosed()
        assert sql.@statementCache.containsKey(sqlText)
        assert sql.@statementCache[sqlText].is(wrapper.@delegate)
    }
}

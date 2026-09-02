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
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_USER

/**
 * A JDBC Statement batch may hold different statements, so it has nothing to bind against and
 * addBatch takes SQL text. A GString interpolated there is composed into the statement rather than
 * bound - unlike every query method on Sql. That is not rejected, because heterogeneous batches,
 * generated DDL and identifiers that cannot be bound are legitimate uses with no parameterised
 * equivalent, but it is reported so it is not silent.
 */
class SqlBatchInterpolationTest extends GroovyTestCase {

    Sql sql
    private Logger logger
    private List<LogRecord> records
    private Handler handler

    void setUp() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER, password: DB_PASSWORD)
        sql = new Sql(ds.connection)
        sql.execute('CREATE TABLE person ( id INTEGER, firstname VARCHAR(10), PRIMARY KEY (id))')

        records = []
        handler = new Handler() {
            void publish(LogRecord r) { records << r }
            void flush() {}
            void close() {}
        }
        logger = Logger.getLogger('groovy.sql.Sql')
        logger.addHandler(handler)
    }

    void tearDown() {
        logger?.removeHandler(handler)
        sql?.close()
    }

    private List<String> warnings() {
        records.findAll { it.level == Level.WARNING }*.message
    }

    // GROOVY-12342
    void testInterpolatedValueIsReportedNotRejected() {
        def name = "Jean"
        def result = sql.withBatch { stmt ->
            stmt.addBatch("insert into PERSON (id, firstname) values (1, '$name')")
        }

        // the statement still runs: this is a warning, not a rejection
        assert result == [1]
        assert sql.rows('SELECT * FROM PERSON').size() == 1
        assert warnings().any { it.contains('composed into batch SQL as text') }
    }

    // GROOVY-12342
    void testWarningIsLoggedOncePerBatch() {
        sql.withBatch { stmt ->
            (1..3).each { i -> stmt.addBatch("insert into PERSON (id, firstname) values ($i, 'n$i')") }
        }

        assert warnings().count { it.contains('composed into batch SQL as text') } == 1
    }

    // GROOVY-12342
    void testConstantSqlIsNotReported() {
        sql.withBatch { stmt ->
            stmt.addBatch("insert into PERSON (id, firstname) values (1, 'fixed')")
        }

        assert warnings().findAll { it.contains('composed into batch SQL as text') }.isEmpty()
    }

    // GROOVY-12342
    void testDeliberateExpansionIsNotReported() {
        // Sql.expand marks interpolation the caller meant, as it does for the query methods
        def table = Sql.expand('PERSON')
        sql.withBatch { stmt ->
            stmt.addBatch("insert into $table (id, firstname) values (1, 'fixed')")
        }

        assert sql.rows('SELECT * FROM PERSON').size() == 1
        assert warnings().findAll { it.contains('composed into batch SQL as text') }.isEmpty()
    }

    // GROOVY-12342
    void testParameterisedBatchRemainsTheBindingForm() {
        // withBatch(sql, closure) binds, and says nothing
        def result = sql.withBatch('insert into PERSON (id, firstname) values (?, ?)') { ps ->
            ps.addBatch([1, 'Jean'])
            ps.addBatch([2, 'Lino'])
        }

        assert result == [1, 1]
        assert warnings().findAll { it.contains('composed into batch SQL as text') }.isEmpty()
    }
}

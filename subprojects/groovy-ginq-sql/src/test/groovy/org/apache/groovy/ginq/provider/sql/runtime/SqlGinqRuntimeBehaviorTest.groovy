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
package org.apache.groovy.ginq.provider.sql.runtime

import groovy.sql.Sql
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.h2.jdbcx.JdbcDataSource
import org.apache.groovy.ginq.provider.sql.GinqSqlTestSupport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.sql.Connection
import java.sql.SQLException

import static groovy.test.GroovyAssert.shouldFail

class SqlGinqRuntimeBehaviorTest {
    private Sql db

    @BeforeEach
    void setUp() {
        db = GinqSqlTestSupport.newPopulatedDb()
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(db)
    }

    @Test
    void rewriteStarSelectNamesEverySourceColumn() {
        String sql = 'select e.*, d.* from employees e join departments d on e.deptId = d.id'
        String quoted = SqlGinqRuntime.rewriteStarSelect(sql, [['id', 'name'], ['id', 'title']], '"' as char)
        assert quoted == 'SELECT e."id" AS "__ginq0_0", e."name" AS "__ginq0_1", ' +
                'd."id" AS "__ginq1_0", d."title" AS "__ginq1_1" from employees e join departments d on e.deptId = d.id'

        String plain = SqlGinqRuntime.rewriteStarSelect(sql, [['id'], ['title']], (char) 0)
        assert plain.startsWith('SELECT e.id AS __ginq0_0, d.title AS __ginq1_0 ')
    }

    @Test
    void rewriteStarSelectRejectsALayoutThatCannotBeRebuilt() {
        def shape = shouldFail(SQLException) {
            SqlGinqRuntime.rewriteStarSelect('select 1', [[]], '"' as char)
        }
        assert shape.message.contains('no SELECT ... FROM')

        def item = shouldFail(SQLException) {
            SqlGinqRuntime.rewriteStarSelect('select e.name from employees e', [['name']], '"' as char)
        }
        assert item.message.contains('not alias.*')

        def count = shouldFail(SQLException) {
            SqlGinqRuntime.rewriteStarSelect('select e.* from employees e', [[], []], '"' as char)
        }
        assert count.message.contains('aliases')

        def empty = shouldFail(SQLException) {
            SqlGinqRuntime.rewriteStarSelect('select e.* from employees e', [[]], '"' as char)
        }
        assert empty.message.contains('no columns')

        def duplicate = shouldFail(SQLException) {
            SqlGinqRuntime.rewriteStarSelect('select e.* from employees e', [['Name', 'name']], '"' as char)
        }
        assert duplicate.message.contains('duplicate column labels')
    }

    @Test
    void quoteIdentifierEscapesAndRejectsBlankOrUnquotableNames() {
        assert 'plain' == SqlGinqRuntime.quoteIdentifier('plain', (char) 0)
        assert '"a""b"' == SqlGinqRuntime.quoteIdentifier('a"b', '"' as char)
        assert '`odd`' == SqlGinqRuntime.quoteIdentifier('odd', '`' as char)

        def blank = shouldFail(SQLException) {
            SqlGinqRuntime.quoteIdentifier('', '"' as char)
        }
        assert blank.message.contains('blank')

        def unquoted = shouldFail(SQLException) {
            SqlGinqRuntime.quoteIdentifier('a b', (char) 0)
        }
        assert unquoted.message.contains('needs quoting')
    }

    @Test
    void prepareMixedSqlWrapsSourcesAndRenamesACollidingPresenceColumn() {
        String sql = 'SELECT e.*, d.* FROM employees e LEFT JOIN departments d ON e.deptId = d.id'
        String rewritten = SqlGinqRuntime.prepareMixedSql(sql, [['id', 'name'], ['id', '__ginq_present']], '"' as char)
        assert !rewritten.toLowerCase().contains('e.*')
        assert rewritten.contains('__ginq_src') || rewritten.contains('__ginq')
        assert rewritten.toLowerCase().contains('left join')

        def missing = shouldFail(SQLException) {
            SqlGinqRuntime.prepareMixedSql('SELECT 1', [['id']], '"' as char)
        }
        assert missing.message.contains('FROM') || missing.message.contains('SELECT')
    }

    @Test
    void likePatternEscapesWildcardsAndLeavesNullUnknown() {
        assert SqlGinqRuntime.likePattern(null, true, true) == null
        assert '%a!%!_!!' == SqlGinqRuntime.likePattern('a%_!', true, false)
        assert 'a!%!_!!%' == SqlGinqRuntime.likePattern('a%_!', false, true)
        assert 'plain' == SqlGinqRuntime.likePattern('plain', false, false)
    }

    @Test
    void indexZipRequiresAlignedSources() {
        def zipped = SqlGinqRuntime.indexZip(Queryable.from([1, null]), Queryable.from(['a', 'b'])).toList()
        assert zipped.collect { [it.v1, it.v2] } == [[1, 'a'], [null, 'b']]

        def missing = shouldFail(IllegalArgumentException) {
            SqlGinqRuntime.indexZip(null, Queryable.from([1]))
        }
        assert missing.message.contains('null')

        def skewed = shouldFail(IllegalStateException) {
            SqlGinqRuntime.indexZip(Queryable.from([1]), Queryable.from([1, 2]))
        }
        assert skewed.message.contains('not aligned')
        assert skewed.message.contains('1 and 2')
    }

    @Test
    void mixedOrdinalIsSeparateFromTheColumnMap() {
        MixedSourceRow row = new MixedSourceRow([name: 'Ada', mixedOrdinal: 'column'], 4)
        assert row.get('name') == 'Ada'
        assert row.get('mixedOrdinal') == 'column'
        assert row.mixedOrdinal() == 4
        assert SqlGinqRuntime.mixedOrdinal(row) == 4

        def absent = shouldFail(IllegalArgumentException) { SqlGinqRuntime.mixedOrdinal(null) }
        assert absent.message.contains('null')
        def other = shouldFail(IllegalArgumentException) { SqlGinqRuntime.mixedOrdinal('row') }
        assert other.message.contains('String')
    }

    @Test
    void executeAndColumnLabelsAcceptSqlDataSourceAndConnection() {
        assert labels(SqlGinqRuntime.columnLabels(db, 'select name from employees where id = ?', [1])) == ['NAME']

        JdbcDataSource dataSource = new JdbcDataSource()
        dataSource.setURL(db.connection.metaData.URL)
        dataSource.setUser('sa')
        dataSource.setPassword('')
        def viaDataSource = SqlGinqRuntime.execute(dataSource, 'select name from employees where id = ?', [1]).toList()
        assert viaDataSource[0].name == 'Alice'

        Connection connection = db.connection
        def viaConnection = SqlGinqRuntime.execute(connection, 'select name from employees where salary = ?', [6000]).toList()
        assert viaConnection[0].name == 'Frank'

        def unsupported = shouldFail(IllegalArgumentException) {
            SqlGinqRuntime.execute(42, 'select 1', [])
        }
        assert unsupported.message.contains('Integer')

        def quote = shouldFail(SQLException) {
            SqlGinqRuntime.expandAndExecute(db, 'select name from employees', [], [['name']], '""')
        }
        assert quote.message.contains('one character')
    }

    private static List<String> labels(List<String> names) {
        names.collect { it.toUpperCase(Locale.ROOT) }
    }
}

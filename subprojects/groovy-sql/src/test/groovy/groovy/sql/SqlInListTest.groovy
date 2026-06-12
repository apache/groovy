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

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_USER

/**
 * End-to-end tests for {@code Sql.inList(...)} — GROOVY-5436.
 * Exercises the positional, GString, and named-parameter code paths
 * against an in-memory HSQLDB instance.
 */
class SqlInListTest extends GroovyTestCase {

    Sql sql

    @Override
    void setUp() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER,
                password: DB_PASSWORD)
        sql = new Sql(ds.connection)
        sql.execute('CREATE TABLE person (id INTEGER, firstname VARCHAR(10), lastname VARCHAR(10), PRIMARY KEY (id))')
        def people = sql.dataSet('PERSON')
        people.add(id: 1, firstname: 'James', lastname: 'Strachan')
        people.add(id: 2, firstname: 'Bob',   lastname: 'Mcwhirter')
        people.add(id: 3, firstname: 'Sam',   lastname: 'Pullara')
        people.add(id: 4, firstname: 'Jean',  lastname: 'Gabin')
        people.add(id: 5, firstname: 'Lino',  lastname: 'Ventura')
    }

    @Override
    void tearDown() {
        super.tearDown()
        sql.close()
    }

    void testPositionalInList() {
        def rows = sql.rows('SELECT firstname FROM person WHERE id IN (?) ORDER BY id',
                [Sql.inList([1, 3, 5])])
        assert rows*.firstname == ['James', 'Sam', 'Lino']
    }

    void testPositionalInListMixedWithScalarParam() {
        def rows = sql.rows('SELECT firstname FROM person WHERE lastname = ? AND id IN (?) ORDER BY id',
                ['Strachan', Sql.inList([1, 2, 3])])
        assert rows*.firstname == ['James']
    }

    void testPositionalInListSingletonCollection() {
        def rows = sql.rows('SELECT firstname FROM person WHERE id IN (?)',
                [Sql.inList([4])])
        assert rows*.firstname == ['Jean']
    }

    void testPositionalMultipleInListsInSameQuery() {
        def rows = sql.rows(
                'SELECT firstname FROM person WHERE id IN (?) AND firstname IN (?) ORDER BY id',
                [Sql.inList([1, 2, 3, 4]), Sql.inList(['James', 'Sam', 'Mary'])])
        assert rows*.firstname == ['James', 'Sam']
    }

    void testGStringInList() {
        def ids = [2, 4, 5]
        def rows = sql.rows("SELECT firstname FROM person WHERE id IN (${Sql.inList(ids)}) ORDER BY id")
        assert rows*.firstname == ['Bob', 'Jean', 'Lino']
    }

    void testGStringInListMixedWithScalar() {
        def names = ['James', 'Sam']
        def minId = 1
        def rows = sql.rows(
                "SELECT firstname FROM person WHERE firstname IN (${Sql.inList(names)}) AND id >= $minId ORDER BY id")
        assert rows*.firstname == ['James', 'Sam']
    }

    void testNamedParameterInList() {
        def rows = sql.rows(
                'SELECT firstname FROM person WHERE id IN (:ids) ORDER BY id',
                [ids: Sql.inList([1, 3])])
        assert rows*.firstname == ['James', 'Sam']
    }

    void testExecuteUpdateWithInList() {
        def count = sql.executeUpdate(
                'UPDATE person SET lastname = ? WHERE id IN (?)',
                ['Anonymous', Sql.inList([1, 2])])
        assert count == 2
        assert sql.firstRow('SELECT lastname FROM person WHERE id = 1').lastname == 'Anonymous'
        assert sql.firstRow('SELECT lastname FROM person WHERE id = 3').lastname == 'Pullara'
    }

    void testEachRowWithInList() {
        def collected = []
        sql.eachRow('SELECT firstname FROM person WHERE id IN (?) ORDER BY id',
                [Sql.inList([1, 2])]) { row ->
            collected << row.firstname
        }
        assert collected == ['James', 'Bob']
    }

    void testInListValueOrderIsPreserved() {
        // The expansion order must match the collection iteration order so
        // values land in positional slots matching the SQL rewrite.
        def rows = sql.rows(
                'SELECT id FROM person WHERE id IN (?) ORDER BY id',
                [Sql.inList([5, 1, 3])])
        assert rows*.id == [1, 3, 5]
    }
}

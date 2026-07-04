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
package org.apache.groovy.ginq.provider.sql.jooq

import groovy.sql.Sql
import org.apache.groovy.ginq.provider.sql.GinqSqlTestSupport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Runs GINQ queries end-to-end against an in-memory H2 database
 * via the jooq-sql provider with the H2 dialect.
 */
class JooqSqlIntegrationTest {
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
    void testSingleTableWhereWithCapturedVariable() {
        def threshold = 2500
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.salary > threshold && e.active == true
            orderby e.name
            select e.name, e.salary
        }
        assert [['Alice', 5000], ['Bob', 3000], ['Carol', 4000], ['Eve', 5000], ['Frank', 6000]] ==
                result.collect { [it.name, it.salary] }
    }

    @Test
    void testJoinOrderByAndLimit() {
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            orderby e.salary in desc, e.name
            limit 1, 2
            select e.name, d.name as deptName
        }
        assert [['Alice', 'Dev'], ['Eve', 'Sales']] == result.collect { [it.name, it.deptName] }
    }

    @Test
    void testGroupByIntoWithHaving() {
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.deptId != null
            groupby e.deptId into g
            having g.count() > 1
            orderby g.deptId
            select g.deptId, g.count() as cnt, g.avg(e -> e.salary) as avgSalary
        }
        assert [[1, 2, 4000], [2, 2, 4500]] == result.collect { [it.deptId, it.cnt, it.avgSalary] }
    }

    @Test
    void testSetOperationWithTrailingOrderByAndLimit() {
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from a in 'employees'
            where a.deptId == 1
            select a.name, a.salary
            union
            from b in 'employees'
            where b.deptId == 2
            orderby b.salary in desc, b.name
            limit 1, 2
            select b.name, b.salary
        }
        assert [['Eve', 5000], ['Carol', 4000]] == result.collect { [it.name, it.salary] }
    }

    @Test
    void testCorrelatedExistsAndLike() {
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where (from d in 'departments' where d.id == e.deptId select d.id).exists()
               && e.name.contains('a')
            orderby e.name
            select e.name
        }
        assert ['Carol', 'Frank'] == result.collect { it.name }
    }

    @Test
    void testScalarSubqueryAndCaseWhen() {
        def result = GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.salary == (from x in 'employees' select max(x.salary))
            select e.name, (e.salary > 4000 ? 'high' : 'low!') as band
        }
        assert [['Frank', 'high']] == result.collect { [it.name, it.band] }
    }

    @Test
    void testDefaultDialect() {
        def result = GQL(provider: 'jooq-sql', dataSource: db) {
            from e in 'employees'
            where e.id == 2
            select e.name
        }
        assert ['Bob'] == result.collect { it.name }
    }

    @Test
    void testAutoDetectPrefersJooq() {
        // with jOOQ on the classpath, provider 'sql' resolves to jooq-sql
        def result = GQL(provider: 'sql', dataSource: db) {
            from e in 'employees'
            where e.id == 3
            select e.name
        }
        assert ['Carol'] == result.collect { it.name }
    }
}

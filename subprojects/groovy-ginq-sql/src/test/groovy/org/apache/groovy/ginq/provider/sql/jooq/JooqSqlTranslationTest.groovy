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
import org.apache.groovy.ginq.provider.sql.GinqSqlTranslationTest.CapturingSql
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Asserts the dialect-specific SQL the jooq-sql provider generates, e.g.
 * H2's alias rendering without {@code AS} and lowercase keywords.
 */
class JooqSqlTranslationTest {
    private Sql realDb
    private CapturingSql db

    @BeforeEach
    void setUp() {
        realDb = GinqSqlTestSupport.newPopulatedDb()
        db = new CapturingSql(realDb)
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(realDb)
    }

    @Test
    void testH2Rendering() {
        def threshold = 2500
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.salary > threshold
            orderby e.name
            limit 1, 2
            select e.name
        }
        assert 'select e.name from employees e where e.salary > ? ' +
                'order by e.name asc nulls last offset ? rows fetch next ? rows only' == db.sqlText
        assert [2500, 1, 2] == db.params
    }

    @Test
    void testSetOperationWithTrailingOrderByAndLimit() {
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
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
        assert 'select a.name, a.salary from employees a where a.deptId = ? ' +
                'union select b.name, b.salary from employees b where b.deptId = ? ' +
                'order by 2 desc nulls last, 1 asc nulls last offset ? rows fetch next ? rows only' == db.sqlText
        assert [1, 2, 1, 2] == db.params
    }

    @Test
    void testGroupByAggregatesAndLike() {
        GQL(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            where e.deptId != null && e.name.contains('a')
            groupby e.deptId into g
            having g.count() > 1
            select g.deptId, g.count() as cnt
        }
        assert "select e.deptId deptId, count(*) cnt from employees e " +
                "where (e.deptId is not null and e.name like ? escape '!') " +
                'group by e.deptId having count(*) > ?' == db.sqlText
        assert ['%a%', 1] == db.params
    }
}

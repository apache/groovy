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
package org.apache.groovy.ginq.provider.sql

import groovy.sql.Sql
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Runs GINQ queries end-to-end against an in-memory HSQLDB database
 * via the native-sql provider.
 */
class GinqSqlIntegrationTest {
    private String dbUrl
    private Sql db

    @BeforeEach
    void setUp() {
        dbUrl = GinqSqlTestSupport.newDbUrl()
        db = GinqSqlTestSupport.newPopulatedDb(dbUrl)
    }

    @AfterEach
    void tearDown() {
        GinqSqlTestSupport.closeDb(db)
    }

    @Test
    void testSingleTableWhereWithCapturedVariable() {
        def threshold = 2500
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary > threshold && e.active == true
            orderby e.name
            select e.name, e.salary
        }
        assert [['Alice', 5000], ['Bob', 3000], ['Carol', 4000], ['Eve', 5000], ['Frank', 6000]] ==
                result.collect { [it.name, it.salary] }
    }

    @Test
    void testOrderByDescAndLimit() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.salary in desc, e.name
            limit 1, 2
            select e.name
        }
        assert ['Alice', 'Eve'] == result.collect { it.name }
    }

    @Test
    void testDistinct() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.active == true
            orderby e.salary
            select distinct(e.salary)
        }
        assert [3000, 4000, 5000, 6000] == result.collect { it.salary }
    }

    @Test
    void testIsNull() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId == null
            select e.name
        }
        assert ['Dave'] == result.collect { it.name }
    }

    @Test
    void testInAndNotIn() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary in [3000, 4000]
            orderby e.name
            select e.name
        }
        assert ['Bob', 'Carol'] == result.collect { it.name }

        result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary !in [3000, 4000]
            orderby e.name
            select e.name
        }
        assert ['Alice', 'Dave', 'Eve', 'Frank'] == result.collect { it.name }
    }

    @Test
    void testFunctionMapping() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.name.toUpperCase() == 'ALICE'
            select e.name.toLowerCase() as lname
        }
        assert ['alice'] == result.collect { it.lname }
    }

    @Test
    void testInnerJoin() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            orderby e.name
            select e.name, d.name as deptName
        }
        assert [['Alice', 'Dev'], ['Bob', 'Dev'], ['Carol', 'Sales'], ['Eve', 'Sales'], ['Frank', 'Support']] ==
                result.collect { [it.name, it.deptName] }
    }

    @Test
    void testLeftJoin() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            leftjoin d in 'departments' on e.deptId == d.id
            orderby e.name
            select e.name, d.name as deptName
        }
        assert [['Alice', 'Dev'], ['Bob', 'Dev'], ['Carol', 'Sales'], ['Dave', null], ['Eve', 'Sales'], ['Frank', 'Support']] ==
                result.collect { [it.name, it.deptName] }
    }

    @Test
    void testCrossJoin() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            crossjoin d in 'departments'
            select e.name, d.name as deptName
        }
        assert 18 == result.size()
    }

    @Test
    void testGroupByWithAggregates() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId != null
            groupby e.deptId into g
            orderby g.deptId
            select g.deptId, g.count() as cnt, g.avg(e -> e.salary) as avgSalary,
                    g.min(e -> e.salary) as minSalary, g.max(e -> e.salary) as maxSalary
        }
        assert [[1, 2, 4000, 3000, 5000], [2, 2, 4500, 4000, 5000], [3, 1, 6000, 6000, 6000]] ==
                result.collect { [it.deptId, it.cnt, it.avgSalary, it.minSalary, it.maxSalary] }
    }

    @Test
    void testGroupByHaving() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId != null
            groupby e.deptId into g
            having g.count() > 1
            orderby g.deptId
            select g.deptId
        }
        assert [1, 2] == result.collect { it.deptId }
    }

    @Test
    void testJoinWithGroupBy() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            groupby d.name into g
            orderby g.name
            select g.name, g.sum(e -> e.salary) as total
        }
        assert [['Dev', 8000], ['Sales', 9000], ['Support', 6000]] ==
                result.collect { [it.name, it.total] }
    }

    @Test
    void testWholeTableAggregates() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            select count() as cnt, max(e.salary) as maxSalary, min(e.salary) as minSalary
        }
        assert [[6, 6000, 2000]] == result.collect { [it.cnt, it.maxSalary, it.minSalary] }
    }

    @Test
    void testUnionAndUnionAll() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from a in 'employees'
            where a.deptId == 1
            select a.salary
            union
            from b in 'employees'
            where b.deptId == 2
            select b.salary
        }
        assert [3000, 4000, 5000] == result.collect { it.salary }.sort()

        result = GQL(provider: 'native-sql', dataSource: db) {
            from a in 'employees'
            where a.deptId == 1
            select a.salary
            unionall
            from b in 'employees'
            where b.deptId == 2
            select b.salary
        }
        assert [3000, 4000, 5000, 5000] == result.collect { it.salary }.sort()
    }

    @Test
    void testIntersectAndMinus() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from a in 'employees'
            where a.deptId == 1
            select a.salary
            intersect
            from b in 'employees'
            where b.deptId == 2
            select b.salary
        }
        assert [5000] == result.collect { it.salary }

        result = GQL(provider: 'native-sql', dataSource: db) {
            from a in 'employees'
            where a.deptId == 1
            select a.salary
            minus
            from b in 'employees'
            where b.deptId == 2
            select b.salary
        }
        assert [3000] == result.collect { it.salary }
    }

    @Test
    void testWholeRowProjection() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.id == 1
            select e
        }
        assert 1 == result.size()
        assert 'Alice' == result[0].name
        assert 5000 == result[0].salary
    }

    @Test
    void testArithmeticInProjectionAndPredicate() {
        def result = GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary * 2 > 9000
            orderby e.name
            select e.name, e.salary + 500 as bumped
        }
        assert [['Alice', 5500], ['Eve', 5500], ['Frank', 6500]] ==
                result.collect { [it.name, it.bumped] }
    }

    @Test
    void testGQReturnsQueryable() {
        def queryable = GQ(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary >= 5000
            orderby e.name
            select e.name
        }
        assert ['Alice', 'Eve', 'Frank'] == queryable.toList().collect { it.name }
    }

    @Test
    void testAutoDetectSqlProvider() {
        def result = GQL(provider: 'sql', dataSource: db) {
            from e in 'employees'
            where e.id == 2
            select e.name
        }
        assert ['Bob'] == result.collect { it.name }
    }

    @Test
    void testExplicitAstWalker() {
        def result = GQL(astWalker: 'org.apache.groovy.ginq.provider.sql.GinqSqlWalker', dataSource: db) {
            from e in 'employees'
            where e.id == 3
            select e.name
        }
        assert ['Carol'] == result.collect { it.name }
    }

    @Test
    void testDataSourceAsJdbcDataSource() {
        def dataSource = new org.hsqldb.jdbc.JDBCDataSource(database: dbUrl, user: 'sa', password: '')
        def result = GQL(provider: 'native-sql', dataSource: dataSource) {
            from e in 'employees'
            where e.id == 4
            select e.name
        }
        assert ['Dave'] == result.collect { it.name }
    }

    @Test
    void testDataSourceAsConnection() {
        def connection = java.sql.DriverManager.getConnection(dbUrl, 'sa', '')
        try {
            def result = GQL(provider: 'native-sql', dataSource: connection) {
                from e in 'employees'
                where e.id == 5
                select e.name
            }
            assert ['Eve'] == result.collect { it.name }
        } finally {
            connection.close()
        }
    }
}

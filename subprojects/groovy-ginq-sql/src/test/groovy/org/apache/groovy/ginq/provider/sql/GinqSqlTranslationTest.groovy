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

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.sql.SQLException

/**
 * Asserts the SQL text and bound parameters the native-sql provider generates,
 * by capturing what reaches {@link Sql#rows(String, List)}. Queries still
 * execute against a real H2 database, so the SQL is also validated.
 */
class GinqSqlTranslationTest {
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
    void testBoundParameters() {
        def threshold = 2500
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary > threshold && e.active == true
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.salary > ? AND e.active = ?' == db.sqlText
        assert [2500, true] == db.params
    }

    @Test
    void testJoinWithAlias() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            join d in 'departments' on e.deptId == d.id
            where e.salary >= 3000
            select e.name, d.name as deptName
        }
        assert 'SELECT e.name, d.name AS deptName FROM employees e ' +
                'INNER JOIN departments d ON e.deptId = d.id WHERE e.salary >= ?' == db.sqlText
        assert [3000] == db.params
    }

    @Test
    void testNullChecksInListAndFunctions() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId == null || e.salary in [3000, 4000] || e.name.toUpperCase() == 'EVE'
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.deptId IS NULL ' +
                'OR e.salary IN (?, ?) OR UPPER(e.name) = ?' == db.sqlText
        assert [3000, 4000, 'EVE'] == db.params
    }

    @Test
    void testGroupByAggregates() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId != null
            groupby e.deptId into g
            select g.deptId, g.count() as cnt, g.avg(e -> e.salary) as avgSalary
        }
        assert 'SELECT e.deptId AS deptId, COUNT(*) AS cnt, AVG(e.salary) AS avgSalary FROM employees e ' +
                'WHERE e.deptId IS NOT NULL GROUP BY e.deptId' == db.sqlText
        assert [] == db.params
    }

    @Test
    void testHaving() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            groupby e.deptId into g
            having g.count() > 1
            select g.deptId
        }
        assert 'SELECT e.deptId AS deptId FROM employees e GROUP BY e.deptId HAVING COUNT(*) > ?' == db.sqlText
        assert [1] == db.params
    }

    @Test
    void testOrderByAndLimit() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            orderby e.salary in desc, e.name
            limit 1, 2
            select e.name
        }
        assert 'SELECT e.name FROM employees e ORDER BY e.salary DESC NULLS LAST, e.name ASC NULLS LAST ' +
                'OFFSET ? ROWS FETCH NEXT ? ROWS ONLY' == db.sqlText
        assert [1, 2] == db.params
    }

    @Test
    void testDistinct() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            select distinct(e.salary)
        }
        assert 'SELECT DISTINCT e.salary FROM employees e' == db.sqlText
    }

    @Test
    void testLikeMethods() {
        def part = 'li'
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.name.contains(part) || e.name.startsWith('Bo') || e.name.endsWith('ve')
            select e.name
        }
        assert "SELECT e.name FROM employees e WHERE e.name LIKE ? ESCAPE '!' " +
                "OR e.name LIKE ? ESCAPE '!' OR e.name LIKE ? ESCAPE '!'" == db.sqlText
        assert ['%li%', 'Bo%', '%ve'] == db.params
    }

    @Test
    void testSubstringReplaceAndConcat() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.name.substring(0, 3) == 'Ali'
            select e.name.replace('A', 'X') + '!' as decorated
        }
        assert "SELECT REPLACE(e.name, ?, ?) || ? AS decorated FROM employees e " +
                'WHERE SUBSTRING(e.name FROM ? FOR ?) = ?' == db.sqlText
        assert ['A', 'X', '!', 1, 3, 'Ali'] == db.params
    }

    @Test
    void testTernaryBecomesCaseWhen() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            select e.name, (e.salary > 4000 ? 'high' : 'low') as band
        }
        assert "SELECT e.name, CASE WHEN e.salary > ? THEN 'high' ELSE 'low' END AS band FROM employees e" == db.sqlText
        assert [4000] == db.params
    }

    @Test
    void testClassicGroupBy() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            groupby e.deptId
            having count() > 1
            select e.deptId, count() as cnt, sum(e.salary) as total
        }
        assert 'SELECT e.deptId, COUNT(*) AS cnt, COALESCE(SUM(e.salary), 0) AS total FROM employees e ' +
                'GROUP BY e.deptId HAVING COUNT(*) > ?' == db.sqlText
        assert [1] == db.params
    }

    @Test
    void testSetOperationWithTrailingOrderByAndLimit() {
        GQL(provider: 'native-sql', dataSource: db) {
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
        assert 'SELECT a.name, a.salary FROM employees a WHERE a.deptId = ? ' +
                'UNION SELECT b.name, b.salary FROM employees b WHERE b.deptId = ? ' +
                'ORDER BY 2 DESC NULLS LAST, 1 ASC NULLS LAST OFFSET ? ROWS FETCH NEXT ? ROWS ONLY' == db.sqlText
        assert [1, 2, 1, 2] == db.params
    }

    @Test
    void testDerivedTable() {
        GQL(provider: 'native-sql', dataSource: db) {
            from v in (from e in 'employees' where e.salary >= 4000 select e.name, e.salary)
            where v.salary < 6000
            select v.name
        }
        assert 'SELECT v.name FROM (SELECT e.name, e.salary FROM employees e WHERE e.salary >= ?) v ' +
                'WHERE v.salary < ?' == db.sqlText
        assert [4000, 6000] == db.params
    }

    @Test
    void testCorrelatedExists() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where (from d in 'departments' where d.id == e.deptId select d.id).exists()
            select e.name
        }
        assert 'SELECT e.name FROM employees e ' +
                'WHERE EXISTS (SELECT d.id FROM departments d WHERE d.id = e.deptId)' == db.sqlText
        assert [] == db.params
    }

    @Test
    void testInSubquery() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId in (from d in 'departments' where d.name != 'Support' select d.id)
            select e.name
        }
        assert 'SELECT e.name FROM employees e ' +
                'WHERE e.deptId IN (SELECT d.id FROM departments d WHERE d.name <> ?)' == db.sqlText
        assert ['Support'] == db.params
    }

    @Test
    void testScalarSubquery() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary == (from x in 'employees' select max(x.salary))
            select e.name
        }
        assert 'SELECT e.name FROM employees e ' +
                'WHERE e.salary = (SELECT MAX(x.salary) FROM employees x)' == db.sqlText
        assert [] == db.params
    }

    @Test
    void testSetOperation() {
        GQL(provider: 'native-sql', dataSource: db) {
            from a in 'employees'
            where a.deptId == 1
            select a.salary
            union
            from b in 'employees'
            where b.deptId == 2
            select b.salary
        }
        assert 'SELECT a.salary FROM employees a WHERE a.deptId = ? ' +
                'UNION SELECT b.salary FROM employees b WHERE b.deptId = ?' == db.sqlText
        assert [1, 2] == db.params
    }

    @Test
    void testFunctionsAndConstantRange() {
        def low = 1
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.salary in 3000..5000 && e.name.concat('!') == 'Alice!'
            select e.name.length(), e.salary.abs(), e.salary.round(0), e.name.substring(low, 3)
        }
        assert 'SELECT CHAR_LENGTH(e.name), ABS(e.salary), ROUND(e.salary, ?), ' +
                'SUBSTRING(e.name FROM ? FOR ?) FROM employees e ' +
                'WHERE e.salary >= ? AND e.salary <= ? AND e.name || ? = ?' == db.sqlText
        assert [0, low + 1, 3 - low, 3000, 5000, '!', 'Alice!'] == db.params

        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.name.isEmpty() || !(e.name.isEmpty())
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE CHAR_LENGTH(e.name) = 0 OR NOT (CHAR_LENGTH(e.name) = 0)' == db.sqlText
    }

    @Test
    void testInListContainingNull() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId in [null]
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.deptId IS NULL' == db.sqlText
        assert [] == db.params

        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId in [null, 1] || e.salary !in [null, 2000]
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.deptId IS NULL OR e.deptId IN (?) ' +
                'OR e.salary IS NOT NULL AND e.salary NOT IN (?)' == db.sqlText
        assert [1, 2000] == db.params

        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.deptId !in [null]
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE e.deptId IS NOT NULL' == db.sqlText
    }

    @Test
    void testEmptyInAndNullLikePattern() {
        def missing = null
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.id in [] || e.name.contains(missing)
            select e.name
        }
        assert 'SELECT e.name FROM employees e WHERE 1 = 0 OR e.name LIKE ? ESCAPE \'!\'' == db.sqlText
        assert [null] == db.params
    }

    @Test
    void testDescendingAndExclusiveRanges() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.id in 3..1 || e.id in 4..<6
            select e.id
        }
        assert 'SELECT e.id FROM employees e WHERE e.id >= ? AND e.id <= ? OR e.id >= ? AND e.id < ?' == db.sqlText
        assert [1, 3, 4, 6] == db.params
    }

    @Test
    void testCoalesce() {
        def fallback = 'n/a'
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where coalesce(e.deptId, 0) == 0
            select coalesce(e.name, fallback, 'x') as display
        }
        assert 'SELECT COALESCE(e.name, ?, ?) AS display FROM employees e WHERE COALESCE(e.deptId, ?) = ?' == db.sqlText
        assert ['n/a', 'x', 0, 0] == db.params
    }

    @Test
    void testCountDistinct() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            groupby e.deptId into g
            having count(distinct(e.salary)) > 1
            select g.deptId, count(distinct(e.salary)) as kinds, g.count(distinct(e -> e.name)) as names
        }
        assert 'SELECT e.deptId AS deptId, COUNT(DISTINCT e.salary) AS kinds, COUNT(DISTINCT e.name) AS names FROM employees e ' +
                'GROUP BY e.deptId HAVING COUNT(DISTINCT e.salary) > ?' == db.sqlText
        assert [1] == db.params
    }

    @Test
    void testCountDistinctLambdaBody() {
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            groupby e.deptId into g
            select g.count(e -> distinct(e.salary)) as kinds
        }
        assert 'SELECT COUNT(DISTINCT e.salary) AS kinds FROM employees e GROUP BY e.deptId' == db.sqlText
    }

    @Test
    void testMathMaxAndMin() {
        def floor = 4500
        def cap = 5500
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            select Math.max(e.salary, floor) as hi, Math.min(e.salary, cap) as lo
        }
        assert 'SELECT GREATEST(e.salary, ?) AS hi, LEAST(e.salary, ?) AS lo FROM employees e' == db.sqlText
        assert [4500, 5500] == db.params
    }

    @Test
    void testCastKeepsAliasAndRendersCast() {
        def needle = 'li'
        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            where e.id == 1
            select e.name as label, cast(e.id, 'DECIMAL') / 2 as half, e.name.indexOf(needle) as at
        }
        assert 'SELECT e.name AS label, (CAST(CAST(e.id AS DECIMAL) AS DOUBLE) / CAST(? AS DOUBLE)) AS half, ' +
                'POSITION(? IN e.name) - 1 AS at FROM employees e WHERE e.id = ?' == db.sqlText
        assert [2, 'li', 1] == db.params

        GQL(provider: 'native-sql', dataSource: db) {
            from e in 'employees'
            select cast(e.salary, 'decimal(10, 2)') as money, cast(e.id, 'double precision') as realId
        }
        assert 'SELECT CAST(e.salary AS DECIMAL(10,2)) AS money, CAST(e.id AS DOUBLE PRECISION) AS realId FROM employees e' == db.sqlText
    }

    @Test
    void testNativeH2DialectKeepsAnsiLimit() {
        GQL(provider: 'native-sql', dialect: 'H2', dataSource: db) {
            from e in 'employees'
            orderby e.name
            limit 1, 2
            select e.name
        }
        assert 'SELECT e.name FROM employees e ORDER BY e.name ASC NULLS LAST OFFSET ? ROWS FETCH NEXT ? ROWS ONLY' == db.sqlText
        assert [1, 2] == db.params
    }

    static class CapturingSql extends Sql {
        String sqlText
        List params
        final List<String> sqlTexts = []
        final List<List> paramLists = []

        CapturingSql(Sql parent) {
            super(parent)
        }

        @Override
        List<GroovyRowResult> rows(String sql, List<?> params) throws SQLException {
            this.sqlText = sql
            this.params = new ArrayList(params)
            sqlTexts << sql
            paramLists << new ArrayList(params)
            return super.rows(sql, params)
        }
    }
}

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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Asserts the strict-mode compile-time errors of the native-sql provider.
 */
class GinqSqlErrorTest {

    @Test
    void testMissingDataSource() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql') {
                from e in 'employees'
                select e.name
            }
        '''
        assert err.message.contains("the native-sql provider requires a `dataSource`")
    }

    @Test
    void testParallelNotSupported() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db, parallel: true) {
                from e in 'employees'
                select e.name
            }
        '''
        assert err.message.contains('`parallel` is not supported by the native-sql provider')
    }

    @Test
    void testNonLiteralTableReference() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from n in [1, 2, 3]
                select n
            }
        '''
        assert err.message.contains('table references must be string literals')
    }

    @Test
    void testUnknownAliasInsideSubquery() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where (from d in 'departments' where d.id == z.deptId select d.id).exists()
                select e.name
            }
        '''
        assert err.message.contains('Unknown alias in `z.deptId`')
    }

    @Test
    void testUntranslatableOperator() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.name ==~ /A.*/
                select e.name
            }
        '''
        assert err.message.contains('Operator `==~` cannot be translated to SQL by the native-sql provider')
    }

    @Test
    void testUntranslatableExpression() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.name == "prefix${'x'}"
                select e.name
            }
        '''
        assert err.message.contains('cannot be translated to SQL by the native-sql provider')
    }

    @Test
    void testUntranslatableMethod() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.name.reverse() == 'ecilA'
                select e.name
            }
        '''
        assert err.message.contains('Method `reverse` cannot be translated to SQL by the native-sql provider')
    }

    @Test
    void testCapturedCollectionInList() {
        def err = shouldFail '''\
            def salaries = [3000, 4000]
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.salary in salaries
                select e.name
            }
        '''
        assert err.message.contains('only list literals, constant ranges and subqueries are supported on the right-hand side of `in`')
        assert err.message.contains('mixedMode: true')
    }

    @Test
    void testUnknownAlias() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where x.salary > 1000
                select e.name
            }
        '''
        assert err.message.contains('Unknown alias in `x.salary`')
    }

    @Test
    void testWindowFunctionNotSupported() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select e.name, (rowNumber() over (orderby e.salary))
            }
        '''
        assert err.message.contains('window functions are deferred and are not translated by the native-sql provider')
        assert err.message.contains('mixedMode: true')
    }

    @Test
    void testOrderByInInnerSetOperationOperand() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from a in 'employees'
                orderby a.name
                select a.name
                union
                from b in 'employees'
                select b.name
            }
        '''
        assert err.message.contains('`orderby` within set operation operands is only supported on the final operand')
    }

    @Test
    void testOrderByOnSetOperationResultMustMatchSelect() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from a in 'employees'
                select a.name
                union
                from b in 'employees'
                orderby b.salary
                select b.name
            }
        '''
        assert err.message.contains('`orderby` on a set operation result must reference columns of the final `select` clause')
    }

    @Test
    void testElvisOperator() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select e.name ?: 'unknown'
            }
        '''
        assert err.message.contains('the Elvis operator cannot be translated to SQL by the native-sql provider')
    }

    @Test
    void testLikeArgumentMustBeParameter() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                join d in 'departments' on e.deptId == d.id
                where e.name.contains(d.name)
                select e.name
            }
        '''
        assert err.message.contains('the arguments of `contains` must be literals or captured variables')
    }

    @Test
    void testGroupKeyNotInGroupBy() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                groupby e.deptId into g
                select g.name
            }
        '''
        assert err.message.contains('`g.name` is not in the `groupby` clause')
    }

    @Test
    void testShutdownNotSupported() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                shutdown immediate
            }
        '''
        assert err.message.contains('`shutdown` is not supported by the native-sql provider')
    }

    @Test
    void testUnknownJooqDialect() {
        def err = shouldFail '''\
            GQ(provider: 'jooq-sql', dialect: 'NOPE', dataSource: db) {
                from e in 'employees'
                select e.name
            }
        '''
        assert err.message.contains('Unknown dialect: NOPE')
    }

    @Test
    void testSqliteRejectsFloorAndCeiling() {
        def floor = shouldFail '''\
            GQ(provider: 'native-sql', dialect: 'sqlite', dataSource: db) {
                from e in 'employees'
                select e.salary.floor()
            }
        '''
        assert floor.message.contains('FLOOR')
        assert floor.message.contains('sqlite')
        assert floor.message.contains('floor()')

        def ceiling = shouldFail '''\
            GQ(provider: 'native-sql', dialect: 'sqlite', dataSource: db) {
                from e in 'employees'
                select e.salary.ceil()
            }
        '''
        assert ceiling.message.contains('CEILING')
        assert ceiling.message.contains('sqlite')
        assert ceiling.message.contains('ceil()')
    }

    @Test
    void testUnknownNativeDialect() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dialect: 'ORACLE', dataSource: db) {
                from e in 'employees'
                select e.name
            }
        '''
        assert err.message.contains('`ORACLE` is not a native SQL dialect')
        assert err.message.contains('postgres')
        assert err.message.contains("provider: 'jooq-sql'")
    }

    @Test
    void testUnsafeTableNameIsRejected() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees; delete from employees'
                select e.name
            }
        '''
        assert err.message.contains('table name `employees; delete from employees` must be a simple identifier')
        assert !err.message.contains('mixedMode')
    }

    @Test
    void testVariableRangeIsRejected() {
        def err = shouldFail '''\
            def low = 1
            def high = 3
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                where e.id in low..high
                select e.id
            }
        '''
        assert err.message.contains('both bounds are compile-time constants')
        assert err.message.contains('mixedMode: true')
    }

    @Test
    void testJooqProviderNamesItself() {
        def err = shouldFail '''\
            GQ(provider: 'jooq-sql', dialect: 'H2', dataSource: db) {
                from e in 'employees'
                select e.name.reverse()
            }
        '''
        assert err.message.contains('jooq-sql provider')
        assert err.message.contains('mixedMode: true')
    }

    @Test
    void testAnnotationWithoutDataSource() {
        def err = shouldFail '''\
            import groovy.ginq.transform.GQ

            class Repository {
                @GQ(provider = 'native-sql')
                def names() {
                    from e in 'employees'
                    select e.name
                }
            }
        '''
        assert err.message.contains("the native-sql provider requires a `dataSource`")
    }

    @Test
    void testCoalesceNeedsTwoArguments() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select coalesce(e.name)
            }
        '''
        assert err.message.contains('`coalesce` expects at least two arguments')
    }

    @Test
    void testDistinctAggregateOnlyForCount() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select sum(distinct(e.salary))
            }
        '''
        assert err.message.contains('distinct aggregates are supported only for `count`')
    }

    @Test
    void testCountDistinctExpectsOneExpression() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select count(distinct(e.salary, e.deptId))
            }
        '''
        assert err.message.contains('`count(distinct(...))` expects a single expression')
    }

    @Test
    void testCastRejectsUnknownType() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select cast(e.id, 'JAVA_OBJECT')
            }
        '''
        assert err.message.contains('`cast` type `JAVA_OBJECT` is not a supported SQL type')

        def injected = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select cast(e.id, 'INTEGER); DROP TABLE employees')
            }
        '''
        assert injected.message.contains('is not a supported SQL type')

        def displayWidth = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select cast(e.id, 'INT(11)')
            }
        '''
        assert displayWidth.message.contains('`cast` type `INT(11)` is not a supported SQL type')
    }

    @Test
    void testCastRejectsNonLiteralType() {
        def err = shouldFail '''\
            def typeName = 'INTEGER'
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select cast(e.id, typeName)
            }
        '''
        assert err.message.contains('`cast` requires a string literal SQL type')
    }

    @Test
    void testIndexOfRejectsFromIndex() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select e.name.indexOf('A', 1)
            }
        '''
        assert err.message.contains('A from-index has no portable SQL form')
    }

    @Test
    void testMathMaxExpectsTwoArguments() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                select Math.max(e.salary)
            }
        '''
        assert err.message.contains('`Math.max` and `Math.min` expect two arguments')
    }

    @Test
    void testAggregateLambdaParamForMultiTableQuery() {
        def err = shouldFail '''\
            GQ(provider: 'native-sql', dataSource: db) {
                from e in 'employees'
                join d in 'departments' on e.deptId == d.id
                groupby d.name into g
                select g.name, g.sum(x -> x.salary)
            }
        '''
        assert err.message.contains('name the aggregate lambda parameter after a table alias')
    }
}

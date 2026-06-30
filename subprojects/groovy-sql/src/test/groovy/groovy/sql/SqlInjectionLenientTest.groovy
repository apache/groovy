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

import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

import javax.sql.DataSource

import static groovy.sql.SqlTestConstants.DB_DATASOURCE
import static groovy.sql.SqlTestConstants.DB_DS_KEY
import static groovy.sql.SqlTestConstants.DB_PASSWORD
import static groovy.sql.SqlTestConstants.DB_URL_PREFIX
import static groovy.sql.SqlTestConstants.DB_USER

/**
 * GROOVY-12118: verifies the lenient (legacy, insecure) handling of a quoted dynamic expression.
 * <p>
 * This test is deliberately kept in its own class: it relies on the
 * {@code groovy.sql.injection.lenient} system property being set, which is applied here via a forked
 * JVM ({@link ForkedJvm}). Housing it separately avoids any other test in the suite being run with
 * the property unexpectedly in effect. The secure-by-default behaviour (rejection) is covered by
 * {@code SqlTest} and {@code SqlCompleteTest}.
 */
final class SqlInjectionLenientTest {

    private Sql sql
    private String testMethodName

    @BeforeEach
    void setUp(TestInfo testInfo) {
        testMethodName = testInfo.testMethod.get().name
        sql = createSql()
    }

    @AfterEach
    void tearDown() {
        sql?.close()
    }

    @Test
    @ForkedJvm(systemProperties = ['groovy.sql.injection.lenient=true'])
    void testQuotedDynamicExpressionInlinedWhenLenient() {
        def foo = "cheese"
        def bar = "edam"
        // with lenient mode enabled the value is inlined (insecurely) rather than rejected, and the query still runs
        def names = []
        sql.eachRow("select * from FOOD where type='${foo}' and name != '${bar}'") { names << it.name }
        assert names == ['brie', 'cheddar']
    }

    private Sql createSql() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + testMethodName,
                user: DB_USER, password: DB_PASSWORD)
        def sql = new Sql(ds)

        sql.execute('create table FOOD ( type VARCHAR(10), name VARCHAR(10))')

        def food = sql.dataSet('FOOD')
        food.add(type: 'cheese', name: 'edam')
        food.add(type: 'cheese', name: 'brie')
        food.add(type: 'cheese', name: 'cheddar')
        food.add(type: 'drink', name: 'beer')
        food.add(type: 'drink', name: 'coffee')

        return sql
    }
}

/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.sql

class SqlRowsTest extends SqlHelperTestCase {

    Sql sql

    @Override
    protected void setUp() {
        super.setUp()
        sql = createSql()
        ["JOINTESTA", "JOINTESTB"].each { tryDrop(sql, it) }
        sql.execute("create table JOINTESTA ( id INTEGER, bid INTEGER, name VARCHAR(10))")
        sql.execute("create table JOINTESTB ( id INTEGER, name VARCHAR(10))")

        def jointesta = sql.dataSet("JOINTESTA")
        jointesta.add(id: 1, bid: 3, name: 'A 1')
        jointesta.add(id: 2, bid: 2, name: 'A 2')
        jointesta.add(id: 3, bid: 1, name: 'A 3')

        def jointestb = sql.dataSet("JOINTESTB")
        jointestb.add(id: 1, name: 'B 1')
        jointestb.add(id: 2, name: 'B 2')
        jointestb.add(id: 3, name: 'B 3')
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        sql.close()
    }

    void testFirstRowWithPropertyName() {
        def results = sql.firstRow("select firstname, lastname from PERSON where id=1").firstname
        def expected = "James"
        assert results == expected
    }

    void testFirstRowWithPropertyNameAndParams() {
        def results = sql.firstRow("select firstname, lastname from PERSON where id=?", [1]).lastname
        def expected = "Strachan"
        assert results == expected
    }

    void testFirstRowWithPropertyNumber() {
        def results = sql.firstRow("select firstname, lastname from PERSON where id=1")[0]
        def expected = "James"
        assert results == expected
    }

    void testFirstRowWithPropertyNumberAndParams() {
        def results = sql.firstRow("select firstname, lastname from PERSON where id=?", [1])[0]
        def expected = "James"
        assert results == expected
    }

    void testAllRowsWithPropertyNumber() {
        def results = sql.rows("select firstname, lastname from PERSON where id=1 or id=2 order by id")
        assert results[0][0] == "James"
        assert results[0][1] == "Strachan"
        assert results[1][0] == "Bob"
        assert results[1][1] == "Mcwhirter"
    }

    void testAllRowsWithPropertyNumberAndParams() {
        def results = sql.rows("select firstname, lastname from PERSON where id=? or id=? order by id", [1, 2])
        assert results[0][0] == "James"
        assert results[0][1] == "Strachan"
        assert results[1][0] == "Bob"
        assert results[1][1] == "Mcwhirter"
    }

    void testAllRowsWithPropertyName() {
        def results = sql.rows("select firstname, lastname from PERSON where id=1 or id=2 order by id")
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[1].firstname == "Bob"
        assert results[1].lastname == "Mcwhirter"
    }

    void testAsRenaming() {
        def results = sql.rows("select firstname, lastname, firstname || ' ' || lastname as fullname from PERSON where id=1")
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[0].fullname == "James Strachan"
    }

    void testAllRowsWithGStringPropertyName() {
        def name = "James"
        def results = sql.rows("select firstname, lastname from PERSON where firstname = ${name}")
        assert results.size() == 1
        assert results[0].lastname == "Strachan"
    }

    void testAllRowsWithPropertyNameAndParams() {
        def results = sql.rows("select firstname, lastname from PERSON where id=? or id=? order by id", [1, 2])
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[1].firstname == "Bob"
        assert results[1].lastname == "Mcwhirter"
    }

    void testJoinsWithSameName_Groovy3320() {
        // First check it's ok
        sql.rows("select a.id, a.name, b.id, b.name from jointesta as a join jointestb as b on ( a.bid = b.id )").eachWithIndex { row, idx ->
            assert row.size() == 2
        }
        // then check the aliases work now we are using getColumnLabel rather than getColumnName
        sql.rows("select a.id as ai, a.name as an, b.id as bi, b.name as bn from jointesta as a join jointestb as b on ( a.bid = b.id )").each { row ->
            assert row.size() == 4
        }
    }

}

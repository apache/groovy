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

import static groovy.sql.SqlTestConstants.*

class SqlCompleteTest extends SqlHelperTestCase {

    boolean personMetaClosureCalled = false
    boolean foodMetaClosureCalled = false
    Sql sql

    @Override
    protected void setUp() {
        super.setUp()
        sql = createSql()
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        sql.close()
    }

    def personMetaClosure = {metaData ->
        assert metaData.columnCount == 5
        assert metaData.getColumnName(1) == "FIRSTNAME"
        assert metaData.getColumnName(2) == "LASTNAME"
        assert metaData.getColumnName(3) == "ID"
        assert metaData.getColumnName(4) == "LOCATION_ID"
        assert metaData.getColumnName(5) == "LOCATION_NAME"
        assert metaData.every{ it.columnName.contains('I') || it.columnName == 'LASTNAME' }
        assert metaData*.columnName == ["FIRSTNAME", "LASTNAME", "ID", "LOCATION_ID", "LOCATION_NAME"]
        personMetaClosureCalled = true
    }

    def foodMetaClosure = {metaData ->
        assert metaData.columnCount == 2
        assert metaData.getColumnName(1) == "TYPE"
        assert metaData.getColumnName(2) == "NAME"
        assert metaData.any{ it.columnName.contains('Y') }
        assert metaData*.columnName == ["TYPE", "NAME"]
        foodMetaClosureCalled = true
    }

    void testEachRowWithString() {
        def results = [:]
        sql.eachRow("select * from PERSON") {
            results.put(it.firstname, it['lastname'])
        }
        assert results == ["James": "Strachan", "Bob": "Mcwhirter", "Sam": "Pullara"]
        assert !personMetaClosureCalled
    }

    void testEachRowWithNamedParams() {
        def results = [:]
        sql.eachRow("select * from PERSON where firstname like :firstPat and lastname like ?.lastPat", [[firstPat: '%am%', lastPat: '%a%']]) {
            results.put(it.firstname, it['lastname'])
        }
        assert results == ["James": "Strachan", "Sam": "Pullara"]
    }

    void testEachRowWithNamedParamsAsMap_Groovy5405() {
        def results = [:]
        sql.eachRow("select * from PERSON where firstname like :firstPat and lastname like ?.lastPat", [firstPat: '%am%', lastPat: '%a%']) {
            results.put(it.firstname, it['lastname'])
        }
        assert results == ["James": "Strachan", "Sam": "Pullara"]
    }

    void testEachRowWithNamedParametersAndOffset_Groovy5405() {
        sql.eachRow('select * from FOOD where type=:foo', [foo: 'drink'], 2, 1) { row ->
            assert [row[0], row[1]] == ['drink', 'coffee']
        }
    }

    void testRowsWithNamedParametersAndOffset_Groovy5405() {
        def rows = sql.rows('select * from FOOD where type=:foo', [foo: 'drink'], 2, 1)
        assert rows.size() == 1
        assert [rows[0][0], rows[0][1]] == ['drink', 'coffee']
    }

    void testEachRowWithNamedParamsAsNamedArgs_Groovy5405() {
        def results = [:]
        sql.eachRow("select * from PERSON where firstname like :firstPat and lastname like ?.lastPat", firstPat: '%am%', lastPat: '%a%') {
            results.put(it.firstname, it['lastname'])
        }
        assert results == ["James": "Strachan", "Sam": "Pullara"]
    }

    void testEachRowWithNamedParamsAsNamedArgsAndOffset_Groovy5405() {
        sql.eachRow('select * from FOOD where type=:foo', foo: 'drink', 2, 1) { row ->
            assert [row[0], row[1]] == ['drink', 'coffee']
        }
    }

    void testRowsWithNamedParamsAsNamedArgs_Groovy5405() {
        def rows = sql.rows('select * from FOOD where type=:foo', foo: 'drink')
        assert rows.size() == 2
        assert rows[0] == [TYPE: 'drink', NAME: 'beer']
        assert rows[1] == [TYPE: 'drink', NAME: 'coffee']
    }

    void testRowsWithNamedParamsAsNamedArgsAndOffset_Groovy5405() {
        def rows = sql.rows('select * from FOOD where type=:foo', foo: 'drink', 2, 1)
        assert rows.size() == 1
        assert [rows[0][0], rows[0][1]] == ['drink', 'coffee']
    }

    void testEachRowWithParamsAndEmbeddedString() {
        def results = [:]
        sql.eachRow("select * from PERSON where firstname != ':dummy' and lastname = ?", ["Mcwhirter"]) {
            results.put(it.firstname, it['lastname'])
        }
        assert results == ["Bob": "Mcwhirter"]
    }

    void testEachRowWithNamedOrdinalParams() {
        def lastPatHolder = new Expando()
        lastPatHolder.lastPat = '%a%'
        def results = [:]
        sql.eachRow("select * from PERSON where firstname like ?1.firstPat and lastname like ?2.lastPat", [[firstPat:'%am%'], lastPatHolder]) {
            results.put(it.firstname, it['lastname'])
        }
        assert results == ["James": "Strachan", "Sam": "Pullara"]
    }

    void testRowsWithEmptyMapParams() {
        def results = sql.rows("select * from PERSON where firstname like '%am%' and lastname like '%a%'", [:])
        assert results.collectEntries{ [it.firstname, it.lastname] } == ["James": "Strachan", "Sam": "Pullara"]
    }

    // GROOVY-8174: we'd like a strict test like this but current drivers aren't up to it
//    void testRowsWithIncorrectNumberOfParams() {
//        shouldFail(IllegalArgumentException) {
//            sql.rows("select * from PERSON where firstname like ? and lastname like ?", ['foo', 'bar', 'baz'])
//        }
//    }

    void testRowsWithIncorrectParam() {
        shouldFail(IllegalArgumentException) {
            sql.rows("select * from PERSON where firstname like :x", ['foo'])
        }
    }

    void testEachRowWithStringAndClosure() {
        def results = [:]
        sql.eachRow("select * from PERSON", personMetaClosure) {
            results.put(it.firstname, it['lastname'])
        }
        assert results == ["James": "Strachan", "Bob": "Mcwhirter", "Sam": "Pullara"]
        assert personMetaClosureCalled
    }

    void testExecuteWithStringAndClosures() {
        def results = [:]
        sql.execute("select * from PERSON", personMetaClosure) { isResultSet, rs ->
            if (isResultSet) {
                rs.each {
                    results.put(it.firstname, it['lastname'])
                }
            }
        }
        assert results == ["James": "Strachan", "Bob": "Mcwhirter", "Sam": "Pullara"]
        assert personMetaClosureCalled
    }

    void testEachRowWithStringAndList() {
        def results = []
        sql.eachRow("select * from FOOD where type=? and name != ?", ["cheese", "edam"]) { results.add(it.name) }
        assert results == ["brie", "cheddar"]
        assert !foodMetaClosureCalled
    }

    void testEachRowWithStringAndListAndClosure() {
        def results = []
        sql.eachRow("select * from FOOD where type=? and name != ?", ["cheese", "edam"], foodMetaClosure) { results.add(it.name) }
        assert results == ["brie", "cheddar"]
        assert foodMetaClosureCalled
    }

    void testEachRowWithGString() {
        def foo = "drink"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo}") { results.add(it.name) }
        assert results == ["beer", "coffee"]
        assert !foodMetaClosureCalled
    }

    void testEachRowWithGStringAndClosure() {
        def foo = "drink"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo}", foodMetaClosure) { results.add(it.name) }
        assert results == ["beer", "coffee"]
        assert foodMetaClosureCalled
    }

    void testEachRowWithGString2Parameters() {
        def foo = "cheese"
        def bar = "edam"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { results.add(it.name) }
        assert results == ["brie", "cheddar"]
    }

    void testRowsWithString() {
        def result = sql.rows("select * from PERSON order by firstname")
        assert result.size() == 3
        assert result[0].firstname == "Bob"
        assert result[0].lastname == "Mcwhirter"
        assert result[1].firstname == "James"
        assert result[1].lastname == "Strachan"
        assert result[2].firstname == "Sam"
        assert result[2].lastname == "Pullara"
        assert !personMetaClosureCalled
    }

    void testRowsWithStringAndClosure() {
        def result = sql.rows("select * from PERSON order by firstname", personMetaClosure)
        assert result.size() == 3
        assert result[0].firstname == "Bob"
        assert result[0].lastname == "Mcwhirter"
        assert result[1].firstname == "James"
        assert result[1].lastname == "Strachan"
        assert result[2].firstname == "Sam"
        assert result[2].lastname == "Pullara"
        assert personMetaClosureCalled
    }

    void testRowsWithStringAndList() {
        def result = sql.rows("select * from FOOD where type=? and name != ? order by name", ["cheese", "edam"])
        assert result.size() == 2
        assert result[0].name == "brie"
        assert result[0].type == "cheese"
        assert result[1].name == "cheddar"
        assert result[1].type == "cheese"
        assert !foodMetaClosureCalled
    }

    void testRowsWithStringAndListAndClosure() {
        def result = sql.rows("select * from FOOD where type=? and name != ? order by name", ["cheese", "edam"], foodMetaClosure)
        assert result.size() == 2
        assert result[0].name == "brie"
        assert result[0].type == "cheese"
        assert result[1].name == "cheddar"
        assert result[1].type == "cheese"
        assert foodMetaClosureCalled
    }

    void testRowsWithGString() {
        def foo = "drink"
        def result = sql.rows("select * from FOOD where type=${foo} order by name")
        assert result.size() == 2
        assert result[0].name == "beer"
        assert result[0].type == "drink"
        assert result[1].name == "coffee"
        assert result[1].type == "drink"
        assert !foodMetaClosureCalled
    }

    void testRowsWithGStringAndClosure() {
        def foo = "drink"
        def result = sql.rows("select * from FOOD where type=${foo} order by name", foodMetaClosure)
        assert result.size() == 2
        assert result[0].name == "beer"
        assert result[0].type == "drink"
        assert result[1].name == "coffee"
        assert result[1].type == "drink"
        assert foodMetaClosureCalled
    }

    void testFirstRowWithStringAndList() {
        def row = sql.firstRow("select * from FOOD where type=? and name=?", ["cheese", "edam"])
        assert row.type == "cheese"
    }

    /** When no results, firstRow should return null  */
    void testFirstRowWithStringAndListNoResults() {
        def row = sql.firstRow("select * from FOOD where type=?", ["nothing"])
        assert row == null
    }

    void testFirstRowWithGString() {
        def foo = "drink"
        def result = sql.firstRow("select * from FOOD where type=${foo} order by name")
        assert result.name == "beer"
        assert result.type == "drink"
    }

    void testFirstRowShowingGStringCoercionToString() {
        def table = 'PERSON'
        GString query = "select * from $table"
        // table name can't be a parameter so make it a string
        def resultSet = sql.firstRow(query.toString())
        assert resultSet.containsKey('FIRSTNAME')
    }

    void testFirstRowShowingGStringEscaping() {
        def table = 'PERSON'
        // table name can't be a parameter so escape it
        def samPattern = 'Sa%'
        def resultSet = sql.firstRow("select * from ${Sql.expand(table)} where firstname like $samPattern")
        assert resultSet.containsKey('FIRSTNAME')
    }

    void testRowResultOtherMethods() {
        def resultSet = sql.firstRow("select * from PERSON")
        assert resultSet.containsKey('FIRSTNAME')
        assert resultSet.size() > 0
    }

    void testGroovyRowResultAsMapConstructor() {
        def resultSet = sql.firstRow('select * from PERSON')
        assert resultSet == [FIRSTNAME: 'James', LASTNAME: 'Strachan', ID: 1, LOCATION_ID: 10, LOCATION_NAME: 'London']
        def p = new PersonDTO(resultSet)
        assert p.FIRSTNAME == 'James'
    }

    void testDataSet() {
        def results = []
        def people = sql.dataSet("PERSON")
        people.each { results.add(it.firstname) }
        assert results == ["James", "Bob", "Sam"]
    }

    void testDataSetWithNotEqual() {
        def answer = []
        def people = sql.dataSet(Person)
        def list = people.findAll { it.firstname != 'Bob' }
        list.each{ answer << it.firstname }
        assert answer == ["James", "Sam"]
    }

    void testDataSetWithFindAllPredicate() {
        def results = []
        def food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { results.add(it.name) }
        assert results == ["edam", "brie", "cheddar"]
    }

    void testUpdatingDataSet() {
        def results = []
        def features = sql.dataSet("FEATURE")
        features.each {
            /** @todo HSQLDB doesn't yet support ResultSet updating
             if (it.id == 1) {
                 it.name = it.name + " Rocks!"
                 println("Changing name to ${it.name}")
             }
             /* */
            results.add(it.name)
        }
        assert results == ["GDO", "GPath", "GroovyMarkup"]
    }

    void testGStringToSqlConversion() {
        def foo = 'loincloth'
        def bar = 'wasteband'
        def gstring = "A narrow ${foo} supported by a ${bar}!!"
        def result = sql.asSql(gstring, gstring.values.toList())
        assert result == "A narrow ? supported by a ?!!"
    }

    void testNullHandling() {
        def (wh, ere, anotherfield) = [null, null, null]
        def query = "update tbl set wh=$wh, ere=$ere, anotherfield=$anotherfield where id=1"
        assert sql.asSql(query, sql.getParameters(query)) == 'update tbl set wh=null, ere=null, anotherfield=null where id=1'
    }

    void testExecuteUpdate() {
        def foo = 'food-drink'
        def food = 'food'
        def drink = 'drink'
        def bar = 'guinness'
        def expected = 0
        def result = sql.executeUpdate("update FOOD set type=? where name=?", [foo, bar])
        assert result == expected
        expected = 1
        result = sql.executeUpdate("insert into FOOD (type,name) values (${food},${bar})")
        assert result == expected
        result = sql.executeUpdate("insert into FOOD (type,name) values (${drink},${bar})")
        assert result == expected
        result = sql.executeUpdate("insert into FOOD (type,name) values ('drink','guinness')")
        assert result == expected
        expected = 3
        result = sql.executeUpdate("update FOOD set type=? where name=?", [foo, bar])
        assert result == expected
    }

    void testDataSetWithRows() {
        def dataSet = new DataSet(sql, "FOOD")
        def rows = dataSet.rows()
        assert rows.size() == 5
        def results = []
        rows.each {results.add(it.name)}
        assert results == ["edam", "brie", "cheddar", "beer", "coffee"]
    }

    void testDataSetWithPaging() {
        def results = []
        def people = sql.dataSet("PERSON")
        people.each(2,1) { results.add(it.firstname) }
        assert results == ["Bob"]
    }

    void testDataSetPagingWithRows() {
        def dataSet = new DataSet(sql, "FOOD")
        def rows = dataSet.rows(2,2)

        //Checking to make sure I got two items back
        assert rows.size() == 2
        def results = []
        rows.each {results.add(it.name)}

        assert results == ["brie", "cheddar"]
    }

    void testDataSetWithFirstRow() {
        def dataSet = new DataSet(sql, "FOOD")
        def result = dataSet.firstRow()
        assert result != null
        assert result["name"] == "edam"
    }

    void testEachRowPaging() {
        def names = []
        sql.eachRow("select name from FOOD order by name", 2, 2) { row ->
            names << row.name
        }
        assert names.size() == 2
        assert names[0] == "brie"
        assert names[1] == "cheddar"
    }

    void testEachRowPagingWithParams() {
        def names = []
        sql.eachRow("select name from FOOD where name <> ? order by name", ['brie'], 2, 2) { row ->
            names << row.name
        }
        assert names.size() == 2
        assert names[0] == "cheddar"
        assert names[1] == "coffee"
    }

    void testEachRowPagingGString() {
        def name = "brie"
        def names = []
        sql.eachRow("select name from FOOD where name <> $name order by name", 2, 2) { row ->
            names << row.name
        }
        assert names.size() == 2
        assert names[0] == "cheddar"
        assert names[1] == "coffee"
    }

    void testRowsPaging() {
        def names = sql.rows("select name from FOOD order by name", 2, 2)
        assert names.size() == 2
        assert names[0] == ["NAME":"brie"]
        assert names[1] == ["NAME":"cheddar"]
    }

    void testRowsPagingWithParams() {
        def names = sql.rows("select name from FOOD where name <> ? order by name", ['brie'], 2, 2)
        assert names.size() == 2
        assert names[0] == ["NAME":"cheddar"]
        assert names[1] == ["NAME":"coffee"]
    }

    void testGStringRowsPaging() {
        def name = "brie"
        def names = sql.rows("select name from FOOD where name <> $name order by name", 2, 2)
        assert names.size() == 2
        assert names[0] == ["NAME":"cheddar"]
        assert names[1] == ["NAME":"coffee"]
    }

    void testNewInstanceMapMustContainNonNullUrl() {
        shouldFail(IllegalArgumentException) {
            Sql.newInstance(driver: DB_DRIVER.name, user: 'scott', password: 'tiger')
        }
        shouldFail(IllegalArgumentException) {
            Sql.newInstance(url: null, driver: DB_DRIVER.name, user: 'scott', password: 'tiger')
        }
    }

    void testNewInstanceMapShouldNotContainDriverAndDriverClassName() {
        shouldFail(IllegalArgumentException) {
            Sql.newInstance(driver: 'a', driverClassName: 'b')
        }
    }

    void testNewInstanceMapShouldNotHavePropertiesAndAccountInfo() {
        def args = [url: getURI(), user: DB_USER, password: DB_PASSWORD, properties: new Properties()]
        shouldFail(IllegalArgumentException) {
            Sql.newInstance(args)
        }
    }

    void testNewInstanceMapShouldRequireUserAndPasswordIfOneIsProvided() {
        shouldFail(IllegalArgumentException) {
            Sql.newInstance(url: getURI(), driver: DB_DRIVER.name, user: 'scott')
        }
        shouldFail(IllegalArgumentException) {
            Sql.newInstance(url: getURI(), driver: DB_DRIVER.name, password: 'tiger')
        }
    }

    void testNewInstanceMapNotDestructiveGROOVY5216() {
        String url = getURI()
        String driver = DB_DRIVER.name
        String user = DB_USER
        String password = DB_PASSWORD

        // First pass with user/password and no properties
        def args = [url: url, driver: driver, user: user, password: password]
        Sql.newInstance(args)
        assert args == [url: url, driver: driver, user: user, password: password]

        // Second pass with properties
        String url2 = getURI()
        def props = new Properties()
        props.user = user
        props.password = password
        def args2 = [url: url2, driver: driver, properties: props]
        Sql.newInstance(args2)
        assert args2 == [url: url2, driver:  driver, properties: [user: user, password:  password]]
    }

    void testWithQuoteEmbeddedInInlineComment_Groovy5898() {
        def query = """select *
from FOOD
-- An ' apostrophe
where type=:foo
"""
        def rows = sql.rows(query, foo: 'drink')
        assert rows.size() == 2
    }

}

class PersonDTO {
    def FIRSTNAME, LASTNAME, ID, LOCATION_ID, LOCATION_NAME
}

/*
 * Copyright 2003-2009 the original author or authors.
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

class SqlCompleteTest extends TestHelper {

    boolean personMetaClosureCalled = false
    boolean foodMetaClosureCalled = false

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
        def sql = createSql()
        def results = [:]
        sql.eachRow("select * from PERSON") {
            results.put(it.firstname, it['lastname'])
        }
        def expected = ["James": "Strachan", "Bob": "Mcwhirter", "Sam": "Pullara"]
        assert results == expected
        assert !personMetaClosureCalled
    }

    void testEachRowWithNamedParams() {
        def sql = createSql()
        def results = [:]
        sql.eachRow("select * from PERSON where firstname like :firstPat and lastname like ?.lastPat", [[firstPat:'%am%', lastPat:'%a%']]) {
            results.put(it.firstname, it['lastname'])
        }
        def expected = ["James": "Strachan", "Sam": "Pullara"]
        assert results == expected
        assert !personMetaClosureCalled
    }

    void testEachRowWithNamedOrdinalParams() {
        def lastPatHolder = new Expando()
        lastPatHolder.lastPat = '%a%'
        def sql = createSql()
        def results = [:]
        sql.eachRow("select * from PERSON where firstname like ?1.firstPat and lastname like ?2.lastPat", [[firstPat:'%am%'], lastPatHolder]) {
            results.put(it.firstname, it['lastname'])
        }
        def expected = ["James": "Strachan", "Sam": "Pullara"]
        assert results == expected
        assert !personMetaClosureCalled
    }

    void testEachRowWithStringAndClosure() {
        def sql = createSql()
        def results = [:]
        sql.eachRow("select * from PERSON", personMetaClosure) {
            results.put(it.firstname, it['lastname'])
        }
        def expected = ["James": "Strachan", "Bob": "Mcwhirter", "Sam": "Pullara"]
        assert results == expected
        assert personMetaClosureCalled
    }

    void testEachRowWithStringAndList() {
        def sql = createSql()
        def results = []
        sql.eachRow("select * from FOOD where type=? and name != ?", ["cheese", "edam"]) { results.add(it.name) }
        def expected = ["brie", "cheddar"]
        assert results == expected
        assert !foodMetaClosureCalled
    }

    void testEachRowWithStringAndListAndClosure() {
        def sql = createSql()
        def results = []
        sql.eachRow("select * from FOOD where type=? and name != ?", ["cheese", "edam"], foodMetaClosure) { results.add(it.name) }
        def expected = ["brie", "cheddar"]
        assert results == expected
        assert foodMetaClosureCalled
    }

    void testEachRowWithGString() {
        def sql = createSql()
        def foo = "drink"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo}") { results.add(it.name) }
        def expected = ["beer", "coffee"]
        assert results == expected
        assert !foodMetaClosureCalled
    }

    void testEachRowWithGStringAndClosure() {
        def sql = createSql()
        def foo = "drink"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo}", foodMetaClosure) { results.add(it.name) }
        def expected = ["beer", "coffee"]
        assert results == expected
        assert foodMetaClosureCalled
    }

    void testEachRowWithGString2Parameters() {
        def sql = createSql()
        def foo = "cheese"
        def bar = "edam"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { results.add(it.name) }
        def expected = ["brie", "cheddar"]
        assert results == expected
    }

    void testRowsWithString() {
        def sql = createSql()
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
        def sql = createSql()
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
        def sql = createSql()
        def result = sql.rows("select * from FOOD where type=? and name != ? order by name", ["cheese", "edam"])
        assert result.size() == 2
        assert result[0].name == "brie"
        assert result[0].type == "cheese"
        assert result[1].name == "cheddar"
        assert result[1].type == "cheese"
        assert !foodMetaClosureCalled
    }

    void testRowsWithStringAndListAndClosure() {
        def sql = createSql()
        def result = sql.rows("select * from FOOD where type=? and name != ? order by name", ["cheese", "edam"], foodMetaClosure)
        assert result.size() == 2
        assert result[0].name == "brie"
        assert result[0].type == "cheese"
        assert result[1].name == "cheddar"
        assert result[1].type == "cheese"
        assert foodMetaClosureCalled
    }

    void testRowsWithGString() {
        def sql = createSql()
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
        def sql = createSql()
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
        def sql = createSql();
        def row = sql.firstRow("select * from FOOD where type=? and name=?", ["cheese", "edam"])
        assert row.type == "cheese"
    }

    /** When no results, firstRow should return null  */
    void testFirstRowWithStringAndListNoResults() {
        def sql = createSql();
        def row = sql.firstRow("select * from FOOD where type=?", ["nothing"])
        assert row == null
    }

    void testFirstRowWithGString() {
        def sql = createSql()
        def foo = "drink"
        def result = sql.firstRow("select * from FOOD where type=${foo} order by name")
        assert result.name == "beer"
        assert result.type == "drink"
    }

    void testFirstRowShowingGStringCoercionToString() {
        def sql = createSql()
        def table = 'PERSON'
        GString query = "select * from $table"
        // table name can't be a parameter so make it a string
        def resultSet = sql.firstRow(query.toString())
        assert resultSet.containsKey('FIRSTNAME')
    }

    void testFirstRowShowingGStringEscaping() {
        def sql = createSql()
        def table = 'PERSON'
        // table name can't be a parameter so escape it
        def samPattern = 'Sa%'
        def resultSet = sql.firstRow("select * from ${Sql.expand(table)} where firstname like $samPattern")
        assert resultSet.containsKey('FIRSTNAME')
    }

    void testRowResultOtherMethods() {
        def sql = createSql()
        def resultSet = sql.firstRow("select * from PERSON")
        assert resultSet.containsKey('FIRSTNAME')
        assert resultSet.size() > 0
    }

    void testGroovyRowResultAsMapConstructor() {
        def sql = createSql()
        def resultSet = sql.firstRow('select * from PERSON')
        assert resultSet == [FIRSTNAME: 'James', LASTNAME: 'Strachan', ID: 1, LOCATION_ID: 10, LOCATION_NAME: 'London']
        def p = new PersonDTO(resultSet)
        assert p.FIRSTNAME == 'James'
    }

    void testDataSet() {
        def sql = createSql()
        def results = []
        def people = sql.dataSet("PERSON")
        people.each { results.add(it.firstname) }
        def expected = ["James", "Bob", "Sam"]
        assert results == expected
    }

    void testDataSetWithNotEqual() {
        def sql = createSql()
        def expected = ["James", "Sam"]
        def answer = []
        def people = sql.dataSet(Person)
        def list = people.findAll { it.firstname != 'Bob' }
        list.each{ answer << it.firstname }
        assert answer == expected
    }

    void testDataSetWithFindAllPredicate() {
        def sql = createSql()
        def results = []
        def food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { results.add(it.name) }
        assert results == ["edam", "brie", "cheddar"]
    }

    void testUpdatingDataSet() {
        def sql = createSql()
        def results = []
        def features = sql.dataSet("FEATURE")
        features.each {
            /** @todo HSQLDB doesn't yet support ResultSet updating
             if (it.id == 1) {
             it.name = it.name + " Rocks!"
             println("Changing name to ${it.name}")
             }
             */
            results.add(it.name)
        }
        def expected = ["GDO", "GPath", "GroovyMarkup"]
        assert results == expected
    }

    void testGStringToSqlConversion() {
        def foo = 'loincloth'
        def bar = 'wasteband'
        def sql = createSql()
        def expected = "A narrow ? supported by a ?!!"
        def gstring = "A narrow ${foo} supported by a ${bar}!!"
        def result = sql.asSql(gstring, gstring.values.toList())
        assert result == expected
    }

    void testExecuteUpdate() {
        def foo = 'food-drink'
        def food = 'food'
        def drink = 'drink'
        def bar = 'guinness'
        def sql = createSql();
        def expected = 0
        def result = sql.executeUpdate("update FOOD set type=? where name=?", [foo, bar]);
        assert result == expected
        expected = 1
        result = sql.executeUpdate("insert into FOOD (type,name) values (${food},${bar})");
        assert result == expected
        result = sql.executeUpdate("insert into FOOD (type,name) values (${drink},${bar})");
        assert result == expected
        result = sql.executeUpdate("insert into FOOD (type,name) values ('drink','guinness')");
        assert result == expected
        expected = 3
        result = sql.executeUpdate("update FOOD set type=? where name=?", [foo, bar]);
        assert result == expected
    }

    void testDataSetWithRows() {
        def sql = createSql()
        def dataSet = new DataSet(sql, "FOOD")
        def rows = dataSet.rows()

        //Expected names of the food items
        def expected = ["edam", "brie", "cheddar", "beer", "coffee"]

        //Checking to make sure I got one item back
        assert rows.size() == 5
        def results = []
        rows.each {results.add(it.name)}

        //Checking to make sure the results retrieved match the expected results
        assert results == expected
    }

    void testDataSetWithFirstRow() {
        def sql = createSql()
        def dataSet = new DataSet(sql, "FOOD")
        def result = dataSet.firstRow()
        assert result != null
        assert result["name"] == "edam"
    }
}

class PersonDTO {
    def FIRSTNAME, LASTNAME, ID, LOCATION_ID, LOCATION_NAME
}

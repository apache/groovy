package groovy.sql

import org.axiondb.jdbc.AxionDriver

class SqlRowsTest extends TestHelper {

    void testFirstRowWithPropertyName() {
        sql = createSql()

        results = sql.firstRow("select firstname, lastname from PERSON where id=1").firstname 
        expected = "James"
        assert results == expected
    }

    void testFirstRowWithPropertyNameAndParams() {
        sql = createSql()

        results = sql.firstRow("select firstname, lastname from PERSON where id=?", [1]).lastname 
        expected = "Strachan"
        assert results == expected
    }

    void testFirstRowWithPropertyNumber() {
        sql = createSql()

        results = sql.firstRow("select firstname, lastname from PERSON where id=1")[0] 
        expected = "James"
        assert results == expected
    }
    
    void testFirstRowWithPropertyNumberAndParams() {
        sql = createSql()

        results = sql.firstRow("select firstname, lastname from PERSON where id=?", [1])[0] 
        expected = "James"
        assert results == expected
    }
    
    void testAllRowsWithPropertyNumber() {
        sql = createSql()

        results = sql.rows("select firstname, lastname from PERSON where id=1 or id=2 order by id")
        assert results[0][0] == "James"
        assert results[0][1] == "Strachan"
        assert results[1][0] == "Bob"
        assert results[1][1] == "Mcwhirter"
    }

    void testAllRowsWithPropertyNumberAndParams() {
        sql = createSql()

        results = sql.rows("select firstname, lastname from PERSON where id=? or id=? order by id", [1,2])
        assert results[0][0] == "James"
        assert results[0][1] == "Strachan"
        assert results[1][0] == "Bob"
        assert results[1][1] == "Mcwhirter"
    }

    void testAllRowsWithPropertyName() {
        sql = createSql()

        results = sql.rows("select firstname, lastname from PERSON where id=1 or id=2 order by id")
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[1].firstname == "Bob"
        assert results[1].lastname == "Mcwhirter"
    }

    void testAllRowsWithPropertyNameAndParams() {
        sql = createSql()

        results = sql.rows("select firstname, lastname from PERSON where id=? or id=? order by id", [1,2])
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[1].firstname == "Bob"
        assert results[1].lastname == "Mcwhirter"
    }

}

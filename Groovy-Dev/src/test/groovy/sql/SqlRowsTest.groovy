package groovy.sql

class SqlRowsTest extends TestHelper {

    void testFirstRowWithPropertyName() {
        def sql = createSql()

        def results = sql.firstRow("select firstname, lastname from PERSON where id=1").firstname
        def expected = "James"
        assert results == expected
    }

    void testFirstRowWithPropertyNameAndParams() {
        def sql = createSql()

        def results = sql.firstRow("select firstname, lastname from PERSON where id=?", [1]).lastname
        def expected = "Strachan"
        assert results == expected
    }

    void testFirstRowWithPropertyNumber() {
        def sql = createSql()

        def results = sql.firstRow("select firstname, lastname from PERSON where id=1")[0]
        def expected = "James"
        assert results == expected
    }
    
    void testFirstRowWithPropertyNumberAndParams() {
        def sql = createSql()

        def results = sql.firstRow("select firstname, lastname from PERSON where id=?", [1])[0]
        def expected = "James"
        assert results == expected
    }
    
    void testAllRowsWithPropertyNumber() {
        def sql = createSql()

        def results = sql.rows("select firstname, lastname from PERSON where id=1 or id=2 order by id")
        assert results[0][0] == "James"
        assert results[0][1] == "Strachan"
        assert results[1][0] == "Bob"
        assert results[1][1] == "Mcwhirter"
    }

    void testAllRowsWithPropertyNumberAndParams() {
        def sql = createSql()

        def results = sql.rows("select firstname, lastname from PERSON where id=? or id=? order by id", [1,2])
        assert results[0][0] == "James"
        assert results[0][1] == "Strachan"
        assert results[1][0] == "Bob"
        assert results[1][1] == "Mcwhirter"
    }

    void testAllRowsWithPropertyName() {
        def sql = createSql()

        def results = sql.rows("select firstname, lastname from PERSON where id=1 or id=2 order by id")
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[1].firstname == "Bob"
        assert results[1].lastname == "Mcwhirter"
    }

    void testAllRowsWithGStringPropertyName() {
        def sql = createSql()
        def name = "James"
        def results = sql.rows("select firstname, lastname from PERSON where firstname = ${name}")
        assert results.size() == 1
        assert results[0].lastname == "Strachan"
    }

    void testAllRowsWithPropertyNameAndParams() {
        def sql = createSql()

        def results = sql.rows("select firstname, lastname from PERSON where id=? or id=? order by id", [1,2])
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[1].firstname == "Bob"
        assert results[1].lastname == "Mcwhirter"
    }

}

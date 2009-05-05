package groovy.sql

class SqlRowsTest extends TestHelper {

    protected Sql createSql() {
        Sql sql = super.createSql()

        ["JOINTESTA", "JOINTESTB"].each{ tryDrop(it) }
        sql.execute("create table JOINTESTA ( id integer, bid integer, name varchar)")
        sql.execute("create table JOINTESTB ( id integer, name varchar)")

        def jointesta = sql.dataSet("JOINTESTA")
        jointesta.add( id:1, bid:3, name:'A 1' )
        jointesta.add( id:2, bid:2, name:'A 2' )
        jointesta.add( id:3, bid:1, name:'A 3' )

        def jointestb = sql.dataSet("JOINTESTB")
        jointestb.add( id:1, name:'B 1' )
        jointestb.add( id:2, name:'B 2' )
        jointestb.add( id:3, name:'B 3' )
        return sql
    }

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

    void testAsRenaming() {
        def sql = createSql()

        def results = sql.rows("select firstname, lastname, firstname || ' ' || lastname as fullname from PERSON where id=1")
        System.err.println results[0]
        assert results[0].firstname == "James"
        assert results[0].lastname == "Strachan"
        assert results[0].fullname == "James Strachan"
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

    void testJoinsWithSameName_Groovy3320() {
        def sql = createSql()

        // First check it's ok
        sql.rows( "select a.id, a.name, b.id, b.name from jointesta as a join jointestb as b on ( a.bid = b.id )" ).eachWithIndex { row, idx ->
            System.err.println row
            assert row.size() == 2
        }
        // then check the aliases work now we are using getColumnLabel rather than getColumnName
        sql.rows( "select a.id as ai, a.name as an, b.id as bi, b.name as bn from jointesta as a join jointestb as b on ( a.bid = b.id )" ).each { row ->
            System.err.println row
            assert row.size() == 4
        }
    }

}

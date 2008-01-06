package groovy.sql

/**
 * This is more of a sample program than a unit test and is here as an easy
 * to read demo of GDO. The actual full unit test case is in SqlCompleteTest
 */
class SqlTest extends GroovyTestCase {

    private sql

    void setUp() {
        sql = createSql()
    }

    void testSqlQuery() {
        sql.eachRow("select * from PERSON") { println("Hello ${it.firstname} ${it.lastname}") }
    }

    void testQueryUsingColumnIndex() {
        def answer = null
        sql.eachRow("select count(*) from PERSON") { answer = it[0] }
        println "Found the count of ${answer}"
        assert answer == 3
    }

    void testQueryUsingNegativeColumnIndex() {
        def first = null
        def last = null
        sql.eachRow("select firstname, lastname from PERSON where firstname='James'") { row ->
            first = row[-2]
            last = row[-1]
        }
        println "Found name ${first} ${last}"
        assert first == "James"
        assert last == "Strachan"
    }

    void testSqlQueryWithWhereClause() {
        def foo = "drink"
        sql.eachRow("select * from FOOD where type=${foo}") { println("Drink ${it.name}") }
    }

    void testEachRowWithWhereClauseWith2Arguments() {
        def foo = "cheese"
        def bar = "edam"
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { println("Found cheese ${it.name}") }
    }

    void testFirstRowWithWhereClauseWith2Arguments() {
        def foo = "cheese"
        def bar = "edam"
        def result = sql.firstRow("select * from FOOD where type=${foo} and name != ${bar}")
        assert result.name == 'brie'
    }

    void testSqlQueryWithIncorrectlyQuotedDynamicExpressions() {
        def foo = "cheese"
        def bar = "edam"
        sql.eachRow("select * from FOOD where type='${foo}' and name != '${bar}'") { println("Found cheese ${it.name}") }
    }

    void testDataSet() {
        def people = sql.dataSet("PERSON")
        people.each { println("Hey ${it.firstname}") }
    }

    void testDataSetWithClosurePredicate() {
        def food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { println("Cheese ${it.name}") }
    }

    void testExecuteUpdate(){
        def foo='food-drink'
        def bar='guinness'
        def nRows = sql.executeUpdate("update FOOD set type=? where name=?",[foo,bar]);
        if (nRows == 0){
            sql.executeUpdate("insert into FOOD (type,name) values (${foo},${bar})");
        }
    }

    void testExecuteInsert() {
        def foo = 'food-drink'
        def bar = 'guiness'
        if (sql.dataSource.connection.metaData.supportsGetGeneratedKeys()) {
            def keys = sql.executeInsert('insert into FOOD (type,name) values (?,?)', [foo,bar])
            assert 1 == keys.size()
        } else {
            def count = sql.executeUpdate('insert into FOOD (type,name) values (?,?)', [foo,bar])
            assert 1 == count
        }
    }

    void testMetaData() {
      sql.eachRow('select * from PERSON') {
	       assert it[0] != null
	       assert it.getMetaData() != null
	  }
    }

    protected def createSql() {
        def ds = new org.hsqldb.jdbc.jdbcDataSource()
        ds.database = "jdbc:hsqldb:mem:foo" + getMethodName()
        ds.user = 'sa'
        ds.password = ''
        def sql = new Sql(ds)

        sql.execute("create table PERSON ( firstname varchar, lastname varchar )")
        sql.execute("create table FOOD ( type varchar, name varchar)")

        // now let's populate the datasets
        def people = sql.dataSet("PERSON")
        people.add( firstname:"James", lastname:"Strachan" )
        people.add( firstname:"Bob", lastname:"Mcwhirter" )
        people.add( firstname:"Sam", lastname:"Pullara" )

        def food = sql.dataSet("FOOD")
        food.add( type:"cheese", name:"edam" )
        food.add( type:"cheese", name:"brie" )
        food.add( type:"cheese", name:"cheddar" )
        food.add( type:"drink", name:"beer" )
        food.add( type:"drink", name:"coffee" )

        return sql
    }
}

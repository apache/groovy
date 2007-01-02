package groovy.sql

import org.axiondb.jdbc.AxionDataSource

/**
 * This is more of a sample program than a unit test and is here as an easy
 * to read demo of GDO. The actual full unit test case is in SqlCompleteTest
 */
class SqlTest extends GroovyTestCase {

    void testSqlQuery() {
        def sql = createSql()
        
        sql.eachRow("select * from PERSON") { println("Hello ${it.firstname} ${it.lastname}") }
    }
    
    void testQueryUsingColumnIndex() {
            def sql = createSql()

            def answer = null

            sql.eachRow("select count(*) from PERSON") { answer = it[0] }

            println "Found the count of ${answer}"

            assert answer == 3
        }
    
    void testQueryUsingNegativeColumnIndex() {
            def sql = createSql()

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
        def sql = createSql()
        
        def foo = "drink"
        sql.eachRow("select * from FOOD where type=${foo}") { println("Drink ${it.name}") }
    }
    
    void testSqlQueryWithWhereClauseWith2Arguments() {
        def sql = createSql()
        
        def foo = "cheese"
        def bar = "edam"
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { println("Found cheese ${it.name}") }
    }
    
    void testSqlQueryWithIncorrectlyQuotedDynamicExpressions() {
        def sql = createSql()
        
        def foo = "cheese"
        def bar = "edam"
        sql.eachRow("select * from FOOD where type='${foo}' and name != '${bar}'") { println("Found cheese ${it.name}") }
    }
    
    void testDataSet() {
        def sql = createSql()
        
        def people = sql.dataSet("PERSON")
        people.each { println("Hey ${it.firstname}") }
    }
    
    void testDataSetWithClosurePredicate() {
        def sql = createSql()
        
        def food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { println("Cheese ${it.name}") }
    }
    
    void testExecuteUpdate(){
        def foo='food-drink'
        def bar='guinness'
        def sql = createSql();
        def nRows = sql.executeUpdate("update FOOD set type=? where name=?",[foo,bar]);
        if(nRows == 0){
            sql.executeUpdate("insert into FOOD (type,name) values (${foo},${bar})");
            }
    }
    
    protected def createSql() {
        def dataSource = new AxionDataSource("jdbc:axiondb:foo" + getMethodName())
        def sql = new Sql(dataSource)
        
        sql.execute("create table PERSON ( firstname varchar, lastname varchar )")     
        sql.execute("create table FOOD ( type varchar, name varchar)")
        
        // now lets populate the datasets
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

package groovy.sql

import org.axiondb.jdbc.AxionDataSource

/**
 * This is more of a sample program than a unit test and is here as an easy
 * to read demo of GDO. The actual full unit test case is in SqlCompleteTest
 */
class SqlTest extends GroovyTestCase {

    void testSqlQuery() {
        sql = createSql()     
        
        sql.eachRow("select * from PERSON") { println("Hello ${it.firstname} ${it.lastname}") }
    }
    
    void testQueryUsingColumnIndex() {
	    	sql = createSql()
	    	
	    	answer = null
	    	
	    	sql.eachRow("select count(*) from PERSON") { answer = it[0] }
	    	
	    	println "Found the count of ${answer}"
	    	
	    	assert answer == 3
	    }
    
    void testQueryUsingNegativeColumnIndex() {
	    	sql = createSql()

		first = null
		last = null
		    	
	    	sql.eachRow("select firstname, lastname from PERSON where firstname='James'") { row |
	    		first = row[-2]
	    		last = row[-1]
	    	}
	    	
	    	println "Found name ${first} ${last}"
    	
	    	assert first == "James"
	    	assert last == "Strachan"
    }
    
    void testSqlQueryWithWhereClause() {
        sql = createSql()     
        
        foo = "drink"
        sql.eachRow("select * from FOOD where type=${foo}") { println("Drink ${it.name}") }
    }
    
    void testSqlQueryWithWhereClauseWith2Arguments() {
        sql = createSql()     
        
        foo = "cheese"
        bar = "edam"
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { println("Found cheese ${it.name}") }
    }
    
    void testSqlQueryWithIncorrectlyQuotedDynamicExpressions() {
        sql = createSql()     
        
        foo = "cheese"
        bar = "edam"
        sql.eachRow("select * from FOOD where type='${foo}' and name != '${bar}'") { println("Found cheese ${it.name}") }
    }
    
    void testDataSet() {
        sql = createSql()     
        
        people = sql.dataSet("PERSON")
        people.each { println("Hey ${it.firstname}") }
    }
    
    void testDataSetWithClosurePredicate() {
        sql = createSql()     
        
        food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { println("Cheese ${it.name}") }
    }
    
    void testExecuteUpdate(){
        foo='food-drink'
        bar='guinness'
        sql = createSql();
        nRows = sql.executeUpdate("update FOOD set type=? where name=?",[foo,bar]);
        if(nRows == 0){
            sql.executeUpdate("insert into FOOD (type,name) values (${foo},${bar})");
    		}
    }
    
    protected createSql() {
        dataSource = new AxionDataSource("jdbc:axiondb:foo" + getMethodName())
        sql = new Sql(dataSource)
        
        sql.execute("create table PERSON ( firstname varchar, lastname varchar )")     
        sql.execute("create table FOOD ( type varchar, name varchar)")
        
        // now lets populate the datasets
        people = sql.dataSet("PERSON")
        people.add( firstname:"James", lastname:"Strachan" )
        people.add( firstname:"Bob", lastname:"Mcwhirter" )
        people.add( firstname:"Sam", lastname:"Pullara" )
        
        food = sql.dataSet("FOOD")
        food.add( type:"cheese", name:"edam" )
        food.add( type:"cheese", name:"brie" )
        food.add( type:"cheese", name:"cheddar" )
        food.add( type:"drink", name:"beer" )
        food.add( type:"drink", name:"coffee" )
        
        return sql
    }
}

package groovy.gdo

import org.axiondb.jdbc.AxionDataSource

class SqlCompleteTest extends GroovyTestCase {

    void testSqlQuery() {
        sql = createSql()     
        
        results = [:]
        sql.queryEach("select * from PERSON") { results.put(it.firstname, it.lastname) }
        
        expected = ["James":"Strachan", "Bob":"Mcwhirter", "Sam":"Pullara"]
					
        assert results == expected
    }
    
    void testSqlQueryWithWhereClause() {
        sql = createSql()     
        
        foo = "drink"
        results = []
        sql.queryEach("select * from FOOD where type=${foo}") { results.add(it.name) }
        
        expected = ["beer", "coffee"]
        assert results == expected
    }
    
    void testSqlQueryWithWhereClauseWith2Arguments() {
        sql = createSql()     
        
        foo = "cheese"
        bar = "edam"
        results = []
        sql.queryEach("select * from FOOD where type=${foo} and name != ${bar}") { results.add(it.name) }
        
        expected = ["brie", "cheddar"]
        assert results == expected
    }

    void testDataSet() {
        sql = createSql()     
        
        results = []
        people = sql.dataSet("PERSON")
        people.each { results.add(it.firstname) }
        
        expected = ["James", "Bob", "Sam"]
        assert results == expected
    }
    
    void testDataSetWithClosurePredicate() {
        sql = createSql()     
        
        results = []
        food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { results.add(it.name) }
        
        expected = ["edam", "brie", "cheddar"]
        assert results == expected
    }
    
    protected createSql() {
        dataSource = new AxionDataSource("jdbc:axiondb:foo" + getName())
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

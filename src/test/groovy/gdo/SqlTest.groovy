package groovy.gdo

import org.axiondb.jdbc.AxionDataSource

class SqlTest extends GroovyTestCase {

    void testSqlQuery() {
        sql = createSql()     
        
        sql.queryEach("select * from PERSON") { println("Hello ${it.firstname} ${it.lastname}") }
    }
    
    void testSqlQueryWithWhereClause() {
        sql = createSql()     
        
        foo = 'drink'
        sql.queryEach("select * from FOOD where type=${foo}") { println("Drink ${it.name}") }
    }
    
    void testSqlQueryWithWhereClauseWith2Arguments() {
        sql = createSql()     
        
        foo = 'cheese'
        bar = 'edam'
        sql.queryEach("select * from FOOD where type=${foo} and name != ${bar}") { println("Found cheese ${it.name}") }
    }
    
    void testDataSets() {
        sql = createSql()     
        
        people = sql.dataSet("PERSON")
        people.each { println("Hey ${it.firstname}") }
        
        food = sql.dataSet('FOOD')
        food.findAll { it.type == 'cheese' }.each { println("Cheese ${it.name}") }
    }
    
    protected createSql() {
        dataSource = new AxionDataSource("jdbc:axiondb:foo" + getName())
        sql = new Sql(dataSource)
        
        sql.execute("create table PERSON ( firstname varchar, lastname varchar )")     
        sql.execute("create table FOOD ( type varchar, name varchar)")
        
        // now lets populate the datasets
        people = sql.dataSet('PERSON')
        people.add( firstname:'James', lastname:'Strachan' )
        people.add( firstname:'Bob', lastname:'Mcwhirter' )
        people.add( firstname:'Sam', lastname:'Pullara' )
        
        food = sql.dataSet('FOOD')
        food.add( type:'cheese', name:'edam' )
        food.add( type:'cheese', name:'brie' )
        food.add( type:'cheese', name:'cheddar' )
        food.add( type:'drink', name:'beer' )
        food.add( type:'drink', name:'coffee' )
        
        return sql
    }
}

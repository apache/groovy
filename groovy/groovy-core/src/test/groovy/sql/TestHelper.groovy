package groovy.sql

import org.axiondb.jdbc.AxionDataSource

class TestHelper extends GroovyTestCase {

    static counter = 1
    
    static Sql makeSql() {
        foo = new TestHelper()
        return foo.createSql()
    }
    
    protected createSql() {
        sql = newSql(getURI())
        
        sql.execute("create table PERSON ( firstname varchar, lastname varchar, id integer, location_id integer, location_name varchar )")     
        sql.execute("create table FOOD ( type varchar, name varchar)")
        sql.execute("create table FEATURE ( id integer, name varchar)")
        
        // now lets populate the datasets
        people = sql.dataSet("PERSON")
        people.add( firstname:"James", lastname:"Strachan", id:1, location_id:10, location_name:'London' )
        people.add( firstname:"Bob", lastname:"Mcwhirter", id:2, location_id:20, location_name:'Atlanta' )
        people.add( firstname:"Sam", lastname:"Pullara", id:3, location_id:30, location_name:'California' )
        
        food = sql.dataSet("FOOD")
        food.add( type:"cheese", name:"edam" )
        food.add( type:"cheese", name:"brie" )
        food.add( type:"cheese", name:"cheddar" )
        food.add( type:"drink", name:"beer" )
        food.add( type:"drink", name:"coffee" )
        
        features = sql.dataSet("FEATURE")
        features.add( id:1, name:'GDO' )
        features.add( id:2, name:'GPath' )
        features.add( id:3, name:'GroovyMarkup' )
        return sql
    }
    
    protected getURI() {
		answer = "jdbc:axiondb:foo"
		name = getName()
		if (name == null) { name = "" }
		name += counter++
		return answer + name
    }
    
    protected newSql(String uri) {
	    dataSource = new AxionDataSource(uri)
	    return new Sql(dataSource)
    }
}

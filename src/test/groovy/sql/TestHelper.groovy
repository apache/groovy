package groovy.sql

class TestHelper extends GroovyTestCase {

    static def counter = 1
    
    static Sql makeSql() {
        def foo = new TestHelper()
        return foo.createSql()
    }
    
    protected createEmptySql() {
        return newSql(getURI())
    }
    
    protected createSql() {
        def sql = newSql(getURI())
        
        ["PERSON", "FOOD", "FEATURE"].each{ tryDrop(it) }
        
        sql.execute("create table PERSON ( firstname varchar, lastname varchar, id integer, location_id integer, location_name varchar )")     
        sql.execute("create table FOOD ( type varchar, name varchar)")
        sql.execute("create table FEATURE ( id integer, name varchar)")
        
        // now let's populate the datasets
        def people = sql.dataSet("PERSON")
        people.add( firstname:"James", lastname:"Strachan", id:1, location_id:10, location_name:'London' )
        people.add( firstname:"Bob", lastname:"Mcwhirter", id:2, location_id:20, location_name:'Atlanta' )
        people.add( firstname:"Sam", lastname:"Pullara", id:3, location_id:30, location_name:'California' )
        
        def food = sql.dataSet("FOOD")
        food.add( type:"cheese", name:"edam" )
        food.add( type:"cheese", name:"brie" )
        food.add( type:"cheese", name:"cheddar" )
        food.add( type:"drink", name:"beer" )
        food.add( type:"drink", name:"coffee" )
        
        def features = sql.dataSet("FEATURE")
        features.add( id:1, name:'GDO' )
        features.add( id:2, name:'GPath' )
        features.add( id:3, name:'GroovyMarkup' )
        return sql
    }

    protected tryDrop(String tableName) {
        try {
           sql.execute("drop table $tableName")
        } catch(Exception e){}
    }

    protected getURI() {
		def answer = "jdbc:hsqldb:mem:foo"
		def name = getMethodName()
		if (name == null) {
            name = ""
        }
		name += counter++
		return answer + name
    }
    
    protected newSql(String uri) {
        // TODO once hsqldb 1.9.0 is out rename this
        // def ds = new org.hsqldb.jdbc.JDBCDataSource()
	    def ds = new org.hsqldb.jdbc.jdbcDataSource()
        ds.database = uri
        ds.user = 'sa'
        ds.password = ''
	    return new Sql(ds)
    }
}

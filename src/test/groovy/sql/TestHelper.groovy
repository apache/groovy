package groovy.sql

class TestHelper extends GroovyTestCase {
    TestHelper() {
        def testdb = System.getProperty("groovy.testdb.props")
        if (testdb && new File(testdb).exists()) {
            props = new Properties()
            new File(testdb).withReader { r ->
                props.load(r)
            }
        }
    }

    protected props = null
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

        def ignoreErrors = { Closure c ->
            try {
                c()
            } catch (java.sql.SQLException se) {}
        }
        ignoreErrors {
            sql.execute "drop table PERSON"
        }
        ignoreErrors {
            sql.execute "drop table FOOD"
        }
        ignoreErrors {
            sql.execute "drop table FEATURE"
        }

        sql.execute("create table PERSON ( firstname varchar(100), lastname varchar(100), id integer, location_id integer, location_name varchar(100) )")
        sql.execute("create table FOOD ( type varchar(100), name varchar(100))")
        sql.execute("create table FEATURE ( id integer, name varchar(100))")
        
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
        if (props && props."groovy.testdb.url")
            return props."groovy.testdb.url"
		def answer = "jdbc:hsqldb:mem:foo"
		def name = getMethodName()
		if (name == null) {
            name = ""
        }
		name += counter++
		return answer + name
    }
    
    protected newSql(String uri) {
        if (props) {
            def url = props."groovy.testdb.url"
            def driver = props."groovy.testdb.driver"
            def username = props."groovy.testdb.username"
            def password = props."groovy.testdb.password"
            if (!username && !password) return Sql.newInstance(url, driver)
            return Sql.newInstance(url, username, password, driver)
        }
        // TODO once hsqldb 1.9.0 is out rename this
        // def ds = new org.hsqldb.jdbc.JDBCDataSource()
	    def ds = new org.hsqldb.jdbc.jdbcDataSource()
        ds.database = uri
        ds.user = 'sa'
        ds.password = ''
	    return new Sql(ds)
    }
}

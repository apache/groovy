package groovy.sql

class SqlCompleteTest extends TestHelper {

    void testSqlQuery() {
        sql = createSql()     
        
        results = [:]
        sql.eachRow("select * from PERSON") { results.put(it.firstname, it.lastname) }
        
        expected = ["James":"Strachan", "Bob":"Mcwhirter", "Sam":"Pullara"]
					
        assert results == expected
    }
    
    void testSqlQueryWithWhereClause() {
        sql = createSql()     
        
        foo = "drink"
        results = []
        sql.eachRow("select * from FOOD where type=${foo}") { results.add(it.name) }
        
        expected = ["beer", "coffee"]
        assert results == expected
    }
    
    void testSqlQueryWithWhereClauseWith2Arguments() {
        sql = createSql()     
        
        foo = "cheese"
        bar = "edam"
        results = []
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { results.add(it.name) }
        
        expected = ["brie", "cheddar"]
        assert results == expected
    }

    void testSqlQueryWith2ParametersUsingQuestionMarkNotation() {
        sql = createSql()     
        
        results = []
        sql.eachRow("select * from FOOD where type=? and name != ?", ["cheese", "edam"]) { results.add(it.name) }
        
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
    
    void testUpdatingDataSet() {
        sql = createSql()     
        
        results = []
        features = sql.dataSet("FEATURE")
        features.each { 
            /** @todo Axion doesn't yet support ResultSet updating
            if (it.id == 1) {
                it.name = it.name + " Rocks!"
                println("Changing name to ${it.name}")
            }
            */
            results.add(it.name) 
        }
        
        expected = ["GDO", "GPath", "GroovyMarkup"]
        assert results == expected
    }
    
    void testGStringToSqlConversion(){
       foo = 'loincloth'
       bar = 'wasteband'
       sql = createSql()
       expected = "A narrow ? supported by a ?!!"
       gstring = "A narrow ${foo} supported by a ${bar}!!"
       result = sql.asSql(gstring, gstring.values.toList())
       assert result == expected
    }
    
    void testExecuteUpdate(){
        foo='food-drink'
        food = 'food'
        drink = 'drink'
        bar='guinness'
        sql = createSql();
        expected = 0
        result = sql.executeUpdate("update FOOD set type=? where name=?",[foo,bar]);
        assert result == expected
        expected  = 1
        result = sql.executeUpdate("insert into FOOD (type,name) values (${food},${bar})");
    	assert result == expected
        result = sql.executeUpdate("insert into FOOD (type,name) values (${drink},${bar})");
    	assert result == expected
        result = sql.executeUpdate("insert into FOOD (type,name) values ('drink','guinness')");
    	assert result == expected
        expected = 3
        result = sql.executeUpdate("update FOOD set type=? where name=?",[foo,bar]);
        assert result == expected
    }    
}

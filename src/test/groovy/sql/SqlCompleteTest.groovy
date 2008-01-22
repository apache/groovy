package groovy.sql

class SqlCompleteTest extends TestHelper {

    void testSqlQuery() {
        def sql = createSql()
        def results = [:]
        sql.eachRow("select * from PERSON") { results.put(it.firstname, it['lastname']) }
        def expected = ["James":"Strachan", "Bob":"Mcwhirter", "Sam":"Pullara"]
        assert results == expected
    }
    
    void testRowResultOtherMethods() {
        def sql = createSql()
        def resultSet = sql.firstRow("select * from PERSON")
        assert resultSet.containsKey('FIRSTNAME')
        assert resultSet.size() > 0
    }

    void testFirstRowGString() {
        def sql = createSql()
        def table = 'PERSON'
        GString query = "select * from $table"
        def resultSet = sql.firstRow(query.toString())
        assert resultSet.containsKey('FIRSTNAME')
    }

    void testGroovyRowResultAsMapConstructor() {
        def sql = createSql()
        def resultSet = sql.firstRow('select * from PERSON')
        assert resultSet == [FIRSTNAME:'James', LASTNAME:'Strachan', ID:1, LOCATION_ID:10, LOCATION_NAME:'London']
        def p = new PersonDTO(resultSet)
        assert p.FIRSTNAME == 'James'
    }

    void testRowsGString() {
        def sql = createSql()
        def table = 'PERSON'
        GString query = "select * from $table"
        def result = sql.rows(query.toString())
        assert result.size() == 3
    }

    void testSqlQueryWithWhereClause() {
        def sql = createSql()
        def foo = "drink"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo}") { results.add(it.name) }
        def expected = ["beer", "coffee"]
        assert results == expected
    }
    
    void testSqlQueryWithWhereClauseWith2Arguments() {
        def sql = createSql()
        def foo = "cheese"
        def bar = "edam"
        def results = []
        sql.eachRow("select * from FOOD where type=${foo} and name != ${bar}") { results.add(it.name) }
        def expected = ["brie", "cheddar"]
        assert results == expected
    }

    void testSqlQueryWith2ParametersUsingQuestionMarkNotation() {
        def sql = createSql()
        def results = []
        sql.eachRow("select * from FOOD where type=? and name != ?", ["cheese", "edam"]) { results.add(it.name) }
        def expected = ["brie", "cheddar"]
        assert results == expected
    }

    void testDataSet() {
        def sql = createSql()
        def results = []
        def people = sql.dataSet("PERSON")
        people.each { results.add(it.firstname) }
        def expected = ["James", "Bob", "Sam"]
        assert results == expected
    }
    
    void testDataSetWithClosurePredicate() {
        def sql = createSql()
        def results = []
        def food = sql.dataSet("FOOD")
        food.findAll { it.type == "cheese" }.each { results.add(it.name) }
        def expected = ["edam", "brie", "cheddar"]
        assert results == expected
    }
    
    void testUpdatingDataSet() {
        def sql = createSql()
        def results = []
        def features = sql.dataSet("FEATURE")
        features.each {
            /** @todo HSQLDB doesn't yet support ResultSet updating
            if (it.id == 1) {
                it.name = it.name + " Rocks!"
                println("Changing name to ${it.name}")
            }
            */
            results.add(it.name)
        }
        def expected = ["GDO", "GPath", "GroovyMarkup"]
        assert results == expected
    }
    
    void testGStringToSqlConversion(){
       def foo = 'loincloth'
       def bar = 'wasteband'
       def sql = createSql()
       def expected = "A narrow ? supported by a ?!!"
       def gstring = "A narrow ${foo} supported by a ${bar}!!"
       def result = sql.asSql(gstring, gstring.values.toList())
       assert result == expected
    }
    
    void testExecuteUpdate(){
        def foo='food-drink'
        def food = 'food'
        def drink = 'drink'
        def bar='guinness'
        def sql = createSql();
        def expected = 0
        def result = sql.executeUpdate("update FOOD set type=? where name=?",[foo,bar]);
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
    
    void testFirstRow() {
      def sql = createSql();
      def row = sql.firstRow("select * from FOOD where type=? and name=?", ["cheese", "edam"])
      assert row.type == "cheese"
    }
    
    /** When no results, firstRow should return null */
    void testFirstRowNoResults() {
      def sql = createSql();
      def row = sql.firstRow("select * from FOOD where type=?", ["nothing"])
      assert row == null
    }
    
    void testDataSetWithRows(){
      def sql = createSql()
      def dataSet = new DataSet(sql,"FOOD")
      def rows = dataSet.rows()

      //Expected names of the food items
      def expected = ["edam", "brie", "cheddar", "beer", "coffee"]
         
      //Checking to make sure I got one item back
      assert rows.size() == 5
      def results = []
      rows.each{results.add(it.name)}

      //Checking to make sure the results retrieved match the expected results
      assert results == expected
    }
        
    void testDataSetWithFirstRow(){
      def sql = createSql()
      def dataSet = new DataSet(sql,"FOOD")
      def result = dataSet.firstRow()
      assert result!=null
      assert result["name"]=="edam"
    }
}

class PersonDTO {
    def FIRSTNAME, LASTNAME, ID, LOCATION_ID, LOCATION_NAME
}

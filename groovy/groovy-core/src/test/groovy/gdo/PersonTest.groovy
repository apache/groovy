package groovy.gdo

class PersonTest extends GroovyTestCase {

    property type
    
    void testFoo() {
        persons = createDataSet()
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
		
        assertSql(blogs, "select * from person where lastName = 'Bloggs'")
    }

    void testWhereWithAndClause() {
        persons = createDataSet()
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
        
        bigBlogs = blogs.findAll { it.size > 100 }
		
        assertSql(bigBlogs, "select * from person where lastName = 'Bloggs' and size > 100")
    }

    void testWhereClosureWithAnd() {
        persons = createDataSet()
		
        blogs = persons.findAll { it.size < 10 && it.lastName == "Bloggs" }
		
        /** @todo bug in Groovy where the && is not parsed
        assertSql(blogs, "select * from person where size < 10 and lastName = 'Bloggs'")
        */
        assertSql(blogs, "select * from person where size < 10")
    }
 
    protected compareFn(value) {
        value > 1 && value < 10
    }
    
    protected assertSql(dataSet, expectedSql) {
        sql = dataSet.sql
        assert sql == expectedSql
    }
    
    protected createDataSet() {
        type = Person

        assert type != null : "failed to load Person class"
    
	    return new DataSet(type)
    }
}

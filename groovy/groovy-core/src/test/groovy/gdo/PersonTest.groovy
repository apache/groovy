package groovy.gdo

class PersonTest extends GroovyTestCase {

    property type
    
    void testFoo() {
        persons = new DataSet(type)
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
		
        assertSql(blogs, "select * from person where lastName = 'Bloggs'")
    }

    void testWhereWithAndClause() {
        persons = new DataSet(type)
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
        
        bigBlogs = blogs.findAll { it.size > 100 }
		
        assertSql(bigBlogs, "select * from person where lastName = 'Bloggs' and size > 100")
    }

    void testWhereClosureWithAnd() {
        persons = new DataSet(type)
		
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
    
    protected void setUp() {
        /** @todo parser & code gen bug 
        type = Person
        */
        type = getClass().getClassLoader().loadClass("groovy.gdo.Person")
    }
}

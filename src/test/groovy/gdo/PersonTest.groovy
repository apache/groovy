package groovy.gdo

class PersonTest extends GroovyTestCase {

    void testFoo() {
        persons = new DataSet(Person)
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
		
        assertSql(blogs, "select * from person where lastName = 'Bloggs'")
    }

    void testWhereWithAndClause() {
        persons = new DataSet(Person)
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
        
        bigBlogs = blogs.findAll { it.size > 100 }
		
        assertSql(bigBlogs, "select * from person where lastName = 'Bloggs' and size > 100")
    }

    void testWhereClosureWithAnd() {
        persons = new DataSet(Person)
		
        blogs = persons.findAll { return ((it.size < 10) && (it.lastName == "Bloggs")) }
		
        /** @todo bug
        assertSql(blogs, "select * from person where size < 10 and lastName = 'Bloggs'")
        */
        assertSql(blogs, "select * from person where size < 10")
    }
    
    protected assertSql(dataSet, expectedSql) {
        sql = dataSet.sql
        assert sql == expectedSql
    }
}

package groovy.gdo

class PersonTest extends GroovyTestCase {

    void testFoo() {
        persons = new DataSet(Person)
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
		
        assert blogs.sql == "select * from person where lastName = 'Bloggs'" : blogs.sql
    }

    void testWhereWithAndClause() {
        persons = new DataSet(Person)
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
        
        bigBlogs = blogs.findAll { it.size > 100 }
		
        assert bigBlogs.sql == "select * from person where lastName = 'Bloggs' and size > 100" : bigBlogs.sql
    }
}

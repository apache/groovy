package groovy.sql

import org.axiondb.jdbc.AxionDataSource

class PersonTest extends GroovyTestCase {

    property type
    
    void testFoo() {
        persons = createDataSet()
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
		
        assertSql(blogs, "select * from person where lastName = ?", ['Bloggs'])
    }

    void testWhereWithAndClause() {
        persons = createDataSet()
		
        blogs = persons.findAll { it.lastName == "Bloggs" }
        
        bigBlogs = blogs.findAll { it.size > 100 }
		
        assertSql(bigBlogs, "select * from person where lastName = ? and size > ?", ['Bloggs', 100])
    }

    void testWhereClosureWithAnd() {
        persons = createDataSet()
		
        blogs = persons.findAll { it.size < 10 && it.lastName == "Bloggs" }
		
        assertSql(blogs, "select * from person where size < ? and lastName = ?", [10, 'Bloggs'])
    }
 
    protected compareFn(value) {
        value > 1 && value < 10
    }
    
    protected assertSql(dataSet, expectedSql, expectedParams) {
        sql = dataSet.sql
        params = dataSet.parameters
        assert sql == expectedSql
        assert params == expectedParams
    }
    
    protected createDataSet() {
        type = Person
	
        assert type != null : "failed to load Person class"
    
        dataSource = createDataSource()
        sql = new Sql(dataSource)
        
        return sql.dataSet(type)
    }
    
    protected DataSource createDataSource() {
        return new AxionDataSource("jdbc:axiondb:foo" + getMethodName())
    }
    
}

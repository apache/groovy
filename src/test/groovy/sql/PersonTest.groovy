package groovy.sql

import javax.sql.DataSource

import org.axiondb.jdbc.AxionDataSource

class PersonTest extends GroovyTestCase {

    def type
    
    void testFoo() {
        def persons = createDataSet()
		
        def blogs = persons.findAll { it.lastName == "Bloggs" }
		
        assertSql(blogs, "select * from person where lastName = ?", ['Bloggs'])
    }

    void testWhereWithAndClause() {
        def persons = createDataSet()
		
        def blogs = persons.findAll { it.lastName == "Bloggs" }
        
        def bigBlogs = blogs.findAll { it.size > 100 }
		
        assertSql(bigBlogs, "select * from person where lastName = ? and size > ?", ['Bloggs', 100])
    }

    void testWhereClosureWithAnd() {
        def persons = createDataSet()
		
        def blogs = persons.findAll { it.size < 10 && it.lastName == "Bloggs" }
		
        assertSql(blogs, "select * from person where size < ? and lastName = ?", [10, 'Bloggs'])
    }
 
    protected def compareFn(value) {
        value > 1 && value < 10
    }
    
    protected def assertSql(dataSet, expectedSql, expectedParams) {
        def sql = dataSet.sql
        def params = dataSet.parameters
        assert sql == expectedSql
        assert params == expectedParams
    }
    
    protected DataSource createDataSource() {
        return new AxionDataSource("jdbc:axiondb:foo" + getMethodName())
    }
    
    protected def createDataSet() {
        type = Person

        assert type != null , "failed to load Person class"

        def dataSource = createDataSource()
        sql = new Sql(dataSource)

        return sql.dataSet(type)
    }


}

/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.sql

import javax.sql.DataSource

class PersonTest extends GroovyTestCase {

    DataSet people

    @Override
    protected void setUp() {
        super.setUp()
        people = createDataSet()
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        people.close()
    }

    void testFoo() {
        def blogs = people.findAll { it.lastName == "Bloggs" }
        assertSql(blogs, "select * from person where lastName = ?", ['Bloggs'])
    }

    void testWhereWithAndClause() {
        def blogs = people.findAll { it.lastName == "Bloggs" }
        def bigBlogs = blogs.findAll { it.size > 100 }
        assertSql(bigBlogs, "select * from person where lastName = ? and size > ?", ['Bloggs', 100])
    }

    void testWhereClosureWithAnd() {
        def blogs = people.findAll { it.size < 10 && it.lastName == "Bloggs" }
        assertSql(blogs, "select * from person where (size < ? and lastName = ?)", [10, 'Bloggs'])
    }

    void testComplex() {
        def persons = createDataSet()
        def blogs = persons.findAll { it.size < 10 && it.lastName == "Bloggs" }
        def complexBlogs = blogs.
                findAll { it.lastName < 'Zulu' || it.lastName > 'Alpha' }.
                findAll { it.age < 99 }.
                findAll { it.age > 5 }.
                sort { it.firstName }.reverse().
                findAll { it.firstName != 'Bert' }.
                sort { it.age }
        def expectedParams = [10, "Bloggs", "Zulu", "Alpha", 99, 5, "Bert"]
        def expectedSql = '''select * from person \
where (size < ? and lastName = ?) and (lastName < ? or lastName > ?) and age < ? and age > ? and firstName != ? \
order by firstName DESC, age'''

        assertSql(complexBlogs, expectedSql, expectedParams)
    }

    // GROOVY-5371 can be removed once GROOVY-5375 is completed and this is supported
    void testNonLiteralExpressionsCurrentlyNotSupported() {
        def cutoff = 10
        def message = shouldFail {
            people.findAll { it.size < cutoff && it.lastName == "Bloggs" }.rows()
        }
        assert message.contains("DataSet currently doesn't support arbitrary variables, only literals")
    }

    protected def assertSql(dataSet, expectedSql, expectedParams) {
        assert dataSet.sql == expectedSql
        assert dataSet.parameters == expectedParams
    }
    
    protected DataSource createDataSource() {
        def ds = new org.hsqldb.jdbc.jdbcDataSource()
        ds.database = "jdbc:hsqldb:mem:foo" + getMethodName()
        ds.user = 'sa'
        ds.password = ''
        return ds
    }

    protected createDataSet() {
        def type = Person
        assert type != null , "failed to load Person class"
        def dataSource = createDataSource()
        def sql = new Sql(dataSource)
        return sql.dataSet(type)
    }

}

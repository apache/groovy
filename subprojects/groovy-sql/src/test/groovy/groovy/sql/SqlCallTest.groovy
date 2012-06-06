/*
 * Copyright 2003-2012 the original author or authors.
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

import static groovy.sql.SqlTestConstants.*

/**
 * Test Sql transaction features using a Sql built from a connection
 *
 * @author Paul King
 */
class SqlCallTest extends GroovyTestCase {

    Sql sql

    protected Sql setUpSql() {
        DataSource ds = DB_DATASOURCE.newInstance(
                (DB_DS_KEY): DB_URL_PREFIX + getMethodName(),
                user: DB_USER,
                password: DB_PASSWORD)
        return new Sql(ds.connection)
    }

    protected tryDrop(String tableName) {
        try {
           sql.execute("DROP TABLE $tableName".toString())
        } catch(Exception e){ }
    }

    @Override
    void setUp() {
        sql = setUpSql()
        ["PERSON"].each{ tryDrop(it) }

        sql.execute("CREATE TABLE person ( id INTEGER, firstname VARCHAR(10), lastname VARCHAR(10), PRIMARY KEY (id))")

        // populate some data
        def people = sql.dataSet("PERSON")
        people.add(id: 1, firstname: "James", lastname: "Strachan")
        people.add(id: 2, firstname: "Bob", lastname: "Mcwhirter")
        people.add(id: 3, firstname: "Sam", lastname: "Pullara")
        people.add(id: 4, firstname: "Jean", lastname: "Gabin")
        people.add(id: 5, firstname: "Lino", lastname: "Ventura")
    }

    @Override
    void tearDown() {
        super.tearDown()
        sql.close()
    }

    void testBuiltinStoredProcedureQuery() {
        def pi = sql.firstRow("call PI()")['@p0']
        assert pi.toString().startsWith('3.14159')
    }

    void testSelectWithFunction() {
        def result = sql.firstRow("select firstname, lastname, CHAR_LENGTH(firstname) as firstsize from PERSON")
        assert result.firstname == 'James' && result.firstsize == 5
    }

}

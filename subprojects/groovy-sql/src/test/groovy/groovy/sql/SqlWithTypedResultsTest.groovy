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

/**
 * @author Thomas Heller
 * @version $Revision$
 */
class SqlWithTypedResultsTest extends SqlHelperTestCase {

    void testSqlQuery() {
        def sql = createEmptySql()

        sql.execute("create table groovytest ( anint INTEGER, astring VARCHAR(10) )");

        def groovytest = sql.dataSet("groovytest")
        groovytest.add(anint: 1, astring: "Groovy")
        groovytest.add(anint: 2, astring: "rocks")

        Integer id

        sql.eachRow("SELECT * FROM groovytest ORDER BY anint") {
            println "found ${it.astring} for id ${it.anint}"
            id = it.anint
        }

        assert id == 2
        sql.close()
    }
}


/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.sql

class SqlWithTypedResultsTest extends SqlHelperTestCase {

    void testSqlQuery() {
        def sql = createEmptySql()
        sql.execute("create table groovytest ( anint INTEGER, astring VARCHAR(10) )");

        sql.dataSet("groovytest").with {
            add(anint: 1, astring: "Groovy")
            add(anint: 2, astring: "rocks")
        }
        Integer id
        sql.eachRow("SELECT * FROM groovytest ORDER BY anint") {
            println "found ${it.astring} for id ${it.anint}"
            id = it.anint
        }
        assert id == 2
        sql.close()
    }

    void testSqlQueryWithBatch() {
        // uncomment to see logging (or set FINEST in <JRE_HOME>/lib/logging.properties)
//        def logger = java.util.logging.Logger.getLogger('groovy.sql')
//        logger.level = java.util.logging.Level.FINE
//        logger.addHandler(new java.util.logging.ConsoleHandler(level: java.util.logging.Level.FINE))
        def sql = createEmptySql()
        sql.execute("create table groovytest ( anint INTEGER, astring VARCHAR(10) )");

        sql.dataSet("groovytest").with {
            withBatch(3) {
                add(anint: 1, astring: "Groovy")
                add(anint: 2, astring: "rocks")
                add(anint: 3, astring: "the")
                add(anint: 4, astring: "casbah")
            }
        }
        assert sql.rows("SELECT * FROM groovytest").size() == 4
        // end result the same as if no batching was in place but logging should show:
        // FINE: Successfully executed batch with 3 command(s)
        // FINE: Successfully executed batch with 1 command(s)
        sql.close()
    }
}


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
package groovy.bugs

import groovy.sql.SqlHelperTestCase
import groovy.test.GroovyTestCase

class ForAndSqlBug extends GroovyTestCase {

    void testBugInNormalMethod() {
        def sql = SqlHelperTestCase.makeSql()
        def li = ["a", "b"]
        for (x in li) {
            sql.eachRow("SELECT count(*) FROM FOOD") { e ->
                println " ${x}"
                assert x != null
            }
        }
        sql.close()
    }

    void testBugInsideScript() {
        assertScript '''
            import groovy.sql.SqlHelperTestCase
            def sql = SqlHelperTestCase.makeSql()
            def li = ["a", "b"]
            for (x in li) {
                sql.eachRow("SELECT count(*) FROM FOOD") { e ->
                    println " ${x}"
                    assert x != null
                }
            }
            sql.close()
        '''
    }

}

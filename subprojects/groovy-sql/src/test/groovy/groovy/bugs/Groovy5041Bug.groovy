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

import groovy.test.GroovyTestCase

class Groovy5041Bug extends GroovyTestCase {
    void testAICParameter() {
        // GROOVY-5041
        assertScript """
            import java.sql.Connection
            import groovy.sql.Sql

            class GrailsPrecondition {

                Connection getConnection() { database?.connection?.wrappedConnection }

                Sql getSql() {
                    if (!connection) return null

                    if (!sql) {
                        sql = new Sql(connection) {
                            protected void closeResources(Connection c) {
                                // do nothing, let Liquibase close the connection
                            }
                        }
                    }

                    sql
                }
            }
            def x = new GrailsPrecondition()
            try {
                x.sql
                assert false
            } catch (MissingPropertyException mpe) {
                assert true
            }
        """
        assertScript """
            class Foo {
              def call(){}
              def Foo(x){}
            }

            def x = 1
            def caller = new Foo(x) {
               def call(){x}
            }
            assert caller.call() == 1
            x = 2
            assert caller.call() == 2
        """
    }
}

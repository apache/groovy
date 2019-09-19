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

import groovy.test.GroovyShellTestCase
import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

class SqlSTCTest extends GroovyShellTestCase {

    @Override
    GroovyShell createNewShell() {
        def config = new CompilerConfiguration().addCompilationCustomizers(new ASTTransformationCustomizer(TypeChecked))
        new GroovyShell(config)
    }

    void testEachRow() {
        shell.evaluate '''
            def test(groovy.sql.Sql sql) { 
                sql.eachRow('SELECT * FROM FOO', { println it.columnCount }) { 
                    java.sql.Date date = it.getDate(1); println it[1] 
                } 
            }
        '''
    }

    void testEach() {
        shell.evaluate '''
            def test(groovy.sql.DataSet ds) { 
                ds.each { java.sql.Date date = it.getDate(1); println it[1] } 
            }
        '''
    }

    void testQuery() {
        shell.evaluate '''
            def test(groovy.sql.Sql sql) { 
                sql.query('SELECT * FROM FOO') { java.sql.Date date = it.getDate(1) } 
            }
        '''
    }

    void testRows() {
        shell.evaluate '''
            def test(groovy.sql.Sql sql) { 
                sql.rows('SELECT * FROM FOO') { println it.columnCount } 
            }
        '''
    }

    void testAsList() {
        shell.evaluate '''
            class CustomSql extends groovy.sql.Sql {
                CustomSql(groovy.sql.Sql sql) {
                    super(sql)
                }
                def printColumnCount(String sql, java.sql.ResultSet rs) {
                    this.asList(sql, rs) { println it.columnCount }
                }
            }
            def test(groovy.sql.Sql sql, java.sql.ResultSet rs) {
                new CustomSql(sql).printColumnCount('SELECT * FROM FOO', rs)
            }
        '''
    }

    void testWithStatement() {
        shell.evaluate '''
            def test(groovy.sql.Sql sql) {
                sql.withStatement { it.maxRows = 10 }
            }
        '''
    }
}
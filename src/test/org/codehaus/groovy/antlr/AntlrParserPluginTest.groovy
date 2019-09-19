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
package org.codehaus.groovy.antlr

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.Statement

import static org.codehaus.groovy.control.CompilePhase.CONVERSION

/**
 * Test for AntlrParserPlugin.
 */
class AntlrParserPluginTest extends GroovyTestCase {

    void testInnerClassLineNumbers() {

        def result = new AstBuilder().buildFromString CONVERSION, false, '''
            new Object() {

            }
        '''

        assert result[2].getClass() == InnerClassNode
        assert result[2].lineNumber == 2
        assert result[2].lastLineNumber == 4
        assert result[2].columnNumber == 26
        assert result[2].lastColumnNumber == 14
    }

    void testEnumLineNumbers() {
        def result = new AstBuilder().buildFromString CONVERSION, false, '''
            enum Color {

            }
        '''

        assert result[1].getClass() == ClassNode
        assert result[1].lineNumber == 2
        assert result[1].lastLineNumber == 4
        assert result[1].columnNumber == 13
        assert result[1].lastColumnNumber == 14
    }

    void testStatementAfterLabel() {
        def result = new AstBuilder().buildFromString CONVERSION, false, '''
            def method() {
                label:
                    assert i == 9
            }
        '''

        assert result[1].getClass() == ClassNode
        MethodNode method = result[1].getMethods('method')[0]
        Statement statement = method.code.statements[0]
        assert statement.lineNumber == 4
        assert statement.lastLineNumber == 4
        assert statement.columnNumber == 21
        assert statement.lastColumnNumber == 34
        assert statement.statementLabel == 'label'
    }

}

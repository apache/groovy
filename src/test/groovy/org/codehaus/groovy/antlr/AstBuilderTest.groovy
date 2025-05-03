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

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.junit.Test

final class AstBuilderTest {

    private static ASTNode buildAST(boolean statementsOnly = false, String source) {
        List<ASTNode> nodes = new AstBuilder().buildFromString(CompilePhase.CONVERSION, statementsOnly, source)
        if (statementsOnly) {
            assert nodes.size() == 1
            return nodes[0] // stmts
        }
        nodes[1] // type
    }

    @Test
    void testStatementsOnly() {
        def node = buildAST true, '''
            println 'hello world'
        '''

        assert node instanceof BlockStatement
        assert node.statements[0] instanceof ExpressionStatement
    }

    @Test
    void testAnonymousInnerClass() {
        ClassNode node = buildAST '''\
            new Object() {

            }
        '''

        node = node.innerClasses.next()

        assert node.lineNumber == 1
        assert node.columnNumber == 26 // TODO: 13
        assert node.lastLineNumber == 3
        assert node.lastColumnNumber == 14
    }

    // GROOVY-11642
    @Test
    void testInnerClass1() {
        ClassNode outer = buildAST '''\
            class Outer {
                protected class Inner {
                }
            }
        '''

        def inner = outer.innerClasses.next()

        assert inner.lineNumber == 2
        assert inner.columnNumber == 17
        assert inner.lastLineNumber == 3
        assert inner.lastColumnNumber == 18
    }

    // GROOVY-11642
    @Test
    void testInnerClass2() {
        ClassNode outer = buildAST '''\
            class Outer {
                @Deprecated class Inner {
                }
            }
        '''

        def inner = outer.innerClasses.next()

        assert inner.lineNumber == 2
        assert inner.columnNumber == 17
        assert inner.lastLineNumber == 3
        assert inner.lastColumnNumber == 18
    }

    @Test
    void testClass() {
        ClassNode node = buildAST '''\
            public class C {
            }
        '''

        assert node.lineNumber == 1
        assert node.columnNumber == 13
        assert node.lastLineNumber == 2
        assert node.lastColumnNumber == 14
    }

    @Test
    void testEnum() {
        ClassNode node = buildAST '''\
            public enum E {
            }
        '''

        assert node.lineNumber == 1
        assert node.columnNumber == 13
        assert node.lastLineNumber == 2
        assert node.lastColumnNumber == 14
    }

    @Test
    void testField() {
        ClassNode node = buildAST '''\
            class C {
                @Deprecated
                protected
                int f =
                123
            }
        '''

        def field = node.getField('f')

        assert field.lineNumber == 2
        assert field.columnNumber == 17
        assert field.lastLineNumber == 5
        assert field.lastColumnNumber == 20
    }

    @Test
    void testMethod() {
        ClassNode node = buildAST '''\
            class C {
                @Deprecated
                protected
                void
                m(){
                }
            }
        '''

        def method = node.getMethod('m')

        assert method.lineNumber == 2
        assert method.columnNumber == 17
        assert method.lastLineNumber == 6
        assert method.lastColumnNumber == 18
    }

    // GROOVY-8426
    @Test
    void testMethodBlock() {
        ClassNode node = buildAST '''
            def method() {
                'return value'

            }
        '''

        Statement statement = node.getMethod('method').code

        assert statement.lineNumber == 2
        assert statement.columnNumber == 26
        assert statement.lastLineNumber == 5
        assert statement.lastColumnNumber == 14
    }

    @Test
    void testStatementAfterLabel() {
        ClassNode node = buildAST '''
            def method() {
                label:
                    assert i == 9
            }
        '''

        Statement statement = node.getMethod('method').code.statements[0]

        assert statement.lineNumber == 4
        assert statement.columnNumber == 21
        assert statement.lastLineNumber == 4
        assert statement.lastColumnNumber == 34
        assert statement.statementLabels[0] == 'label'
    }
}

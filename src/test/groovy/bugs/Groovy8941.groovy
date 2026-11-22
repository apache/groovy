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
package bugs

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

/**
 * GROOVY-8941: a command chain {@code process test { ... }} must parse as
 * {@code this.process(this.test({...}))} whether or not the nested command
 * uses parentheses on {@code file(...)}.
 */
final class Groovy8941 {

    @Test
    void testNestedCommandChainWithoutFileParenthesesIsProcessCall() {
        assertProcessTestCall('''
            process test{
                output:
                file 'output.txt' into b, c
                'echo hello'
            }
        ''')
    }

    @Test
    void testNestedCommandChainWithFileParenthesesIsProcessCall() {
        assertProcessTestCall('''
            process test{
                output:
                file('output.txt') into b, c
                'echo hello'
            }
        ''')
    }

    private static void assertProcessTestCall(String src) {
        def cu = new CompilationUnit(new CompilerConfiguration())
        cu.addSource('Groovy8941Script.groovy', src)
        cu.compile(Phases.CONVERSION)
        def run = cu.ast.modules[0].classes[0].getDeclaredMethods('run')[0]
        def stmts = ((BlockStatement) run.code).statements
        def last = stmts[-1]
        def expr = last instanceof ExpressionStatement ? last.expression : last
        assert expr instanceof MethodCallExpression
        assert expr.methodAsString == 'process'
        assert expr.objectExpression.variable == 'this'
        def arg0 = expr.arguments.expressions[0]
        assert arg0 instanceof MethodCallExpression
        assert arg0.methodAsString == 'test'
        assert arg0.objectExpression.variable == 'this'
    }
}

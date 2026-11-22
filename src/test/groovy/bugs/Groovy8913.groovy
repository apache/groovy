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

import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

/**
 * GROOVY-8913: a partially parenthesized binary expression such as
 * {@code (a.b) + c()} must be parsed as addition, not as a cast of
 * a unary plus expression.
 */
final class Groovy8913 {

    @Test
    void testPartiallyParenthesizedBinaryExpressionIsAddition() {
        def src = '''
            def a = [b: 1]
            def c = { 2 }
            def x = (a.b) + c()
        '''
        def cu = new CompilationUnit(new CompilerConfiguration())
        cu.addSource('Groovy8913Script.groovy', src)
        cu.compile(Phases.CONVERSION)
        def run = cu.ast.modules[0].classes[0].getDeclaredMethods('run')[0]
        def stmts = ((BlockStatement) run.code).statements
        def decl = stmts.find { s ->
            s instanceof ExpressionStatement && s.expression instanceof DeclarationExpression &&
                ((DeclarationExpression) s.expression).leftExpression.variable == 'x'
        }
        def right = ((DeclarationExpression) decl.expression).rightExpression
        assert right instanceof BinaryExpression
        assert right.operation.text == '+'
        assert !(right instanceof CastExpression)
    }

    @Test
    void testPartiallyParenthesizedBinaryExpressionEvaluates() {
        def src = '''
            def a = [b: 1]
            def c = { 2 }
            def x = (a.b) + c()
            assert x == 3
        '''
        new GroovyShell().evaluate(src)
    }
}

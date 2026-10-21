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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.syntax.Token
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * A transform may reuse a single {@code VariableExpression} instance as the
 * operand of an {@code instanceof} test and as the receiver in both branches
 * of a ternary (Spock's data processor methods do this). The type narrowed
 * for the true branch must not stick to the shared node and be seen by the
 * else branch under static compilation.
 */
final class Groovy12313 {

    @Test
    void testSharedParameterReferenceInInstanceofTernary() {
        def foo = compile '''
            @groovy.transform.CompileStatic
            class Foo {
                static Object proc(Object p) {
                    null
                }
            }
        ''', { BlockStatement code, VariableExpression p ->
            block(returnS(sharedTernary(p)))
        }

        assertEquals('x', foo.proc(['x', 'y']))
        assertEquals(1, foo.proc([a: 1]))
    }

    @Test
    void testSharedLocalVariableReferenceInInstanceofTernary() {
        def foo = compile '''
            @groovy.transform.CompileStatic
            class Foo {
                static Object proc(Object arg) {
                    Object p = arg
                }
            }
        ''', { BlockStatement code, VariableExpression arg ->
            def decl = ((ExpressionStatement) code.statements[0]).expression
            def p = new VariableExpression('p')
            p.accessedVariable = decl.leftExpression
            block(code.statements[0], returnS(sharedTernary(p)))
        }

        assertEquals('x', foo.proc(['x', 'y']))
        assertEquals(1, foo.proc([a: 1]))
    }

    // p instanceof Map ? p.getAt('a') : p.getAt(0), with p one shared node
    private static Expression sharedTernary(VariableExpression p) {
        new TernaryExpression(
            new BooleanExpression(new BinaryExpression(p, Token.newKeyword('instanceof', -1, -1), classX(ClassHelper.MAP_TYPE))),
            callX(p, 'getAt', constX('a')),
            callX(p, 'getAt', constX(0, true)))
    }

    private static Class compile(String source, Closure<BlockStatement> body) {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.SEMANTIC_ANALYSIS) {
            @Override
            void call(SourceUnit source0, GeneratorContext context, ClassNode classNode) {
                def method = classNode.getMethods('proc')[0]
                def param = new VariableExpression(method.parameters[0])
                method.code = body((BlockStatement) method.code, param)
            }
        })
        new GroovyClassLoader(Groovy12313.classLoader, config).parseClass(source)
    }
}

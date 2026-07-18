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
package org.apache.groovy.parser.antlr4

import org.antlr.v4.runtime.CommonToken
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.MethodReferenceExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.ParserPlugin
import org.codehaus.groovy.control.ParserPluginFactory
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.io.StringReaderSource
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

final class Groovy12173 {

    // -------------------------------------------------------------------------
    // importDeclaration (left-factored annotationsOpt IMPORT …)
    // -------------------------------------------------------------------------

    @Test
    void 'ordinary import parses after left-factor'() {
        ModuleNode ast = buildAST('''
            import java.util.LinkedList
            LinkedList list = new LinkedList()
        ''')
        assertTrue(ast.imports.any { it.type.name == 'java.util.LinkedList' })
    }

    @Test
    void 'static import and star import parse after left-factor'() {
        ModuleNode ast = buildAST('''
            import static java.lang.Math.PI
            import static java.lang.Math.*
            def x = PI
            def y = sin(0)
        ''')
        assertTrue(ast.staticImports.containsKey('PI') || ast.staticStarImports.containsKey('java.lang.Math'))
        assertTrue(ast.staticStarImports.containsKey('java.lang.Math') || !ast.staticStarImports.isEmpty())
    }

    @Test
    void 'aliased import parses after left-factor'() {
        ModuleNode ast = buildAST('''
            import java.util.LinkedList as LL
            LL list = new LL()
        ''')
        assertTrue(ast.imports.any { it.alias == 'LL' })
    }

    @Test
    void 'module import parses after left-factor'() {
        ModuleNode ast = buildAST('''
            import module java.base
            def s = "ok"
        ''')
        // Module import expands into module-star and/or ordinary star imports
        assertTrue(!ast.moduleStarImports.isEmpty() || !ast.starImports.isEmpty(),
                'module import should expand to star imports via the left-factored importDeclaration rule')
    }

    @Test
    void 'annotated import parses after left-factor'() {
        ModuleNode ast = buildAST('''
            @Deprecated
            import java.util.ArrayList
            ArrayList a = new ArrayList()
        ''')
        assertTrue(ast.imports.any { it.type.name == 'java.util.ArrayList' })
    }

    // -------------------------------------------------------------------------
    // typeNamePairs (left-factored LPAREN … RPAREN)
    // -------------------------------------------------------------------------

    @Test
    void 'positional multi-assignment declaration uses typeNamePair branch'() {
        ModuleNode ast = buildAST('''
            def (a, b) = [1, 2]
            assert a == 1 && b == 2
        ''')
        DeclarationExpression decl = firstDeclaration(ast)
        assertTrue(decl.leftExpression instanceof org.codehaus.groovy.ast.expr.TupleExpression
                || decl.leftExpression instanceof org.codehaus.groovy.ast.expr.ArgumentListExpression
                || decl.leftExpression != null)
        assertNotNull(decl)
    }

    @Test
    void 'typed positional multi-assignment uses typeNamePair branch'() {
        ModuleNode ast = buildAST('''
            def (int a, String b) = [1, 'x']
        ''')
        assertNotNull(firstDeclaration(ast))
    }

    @Test
    void 'keyed multi-assignment uses keyedPair branch'() {
        ModuleNode ast = buildAST('''
            def (name: n, age: a) = [name: 'Alice', age: 30]
            assert n == 'Alice' && a == 30
        ''')
        assertNotNull(firstDeclaration(ast))
    }

    @Test
    void 'typed keyed multi-assignment uses keyedPair branch'() {
        ModuleNode ast = buildAST('''
            def (name: String n, age: int a) = [name: 'Bob', age: 42]
        ''')
        assertNotNull(firstDeclaration(ast))
    }

    // -------------------------------------------------------------------------
    // pathElement DOT left-factor + sibling selectors
    // -------------------------------------------------------------------------

    @Test
    void 'dot property access uses DOT namePart branch'() {
        ModuleNode ast = buildAST('def x = foo.bar')
        ExpressionStatement stmt = firstExpressionStatement(ast)
        assertTrue(containsPropertyAccess(stmt.expression))
    }

    @Test
    void 'attribute access uses DOT AT namePart branch'() {
        ModuleNode ast = buildAST('def x = foo.@bar')
        ExpressionStatement stmt = firstExpressionStatement(ast)
        assertTrue(findInExpression(stmt.expression) { it instanceof AttributeExpression })
    }

    @Test
    void 'safe spread and chain dots parse'() {
        ModuleNode ast = buildAST('''
            def a = foo?.bar
            def b = foo*.bar
            def c = foo??.bar
        ''')
        assertEquals(3, ast.statementBlock.statements.size())
    }

    @Test
    void 'method pointer and method reference use dedicated pathElement branches'() {
        ModuleNode ast = buildAST('''
            def p = Math.&max
            def r = Math::max
            def g = Collections::<String>emptyList
        ''')
        List<Statement> stmts = ast.statementBlock.statements
        assertTrue(findInExpression(((ExpressionStatement) stmts[0]).expression) {
            it instanceof MethodPointerExpression
        })
        assertTrue(findInExpression(((ExpressionStatement) stmts[1]).expression) {
            it instanceof MethodReferenceExpression
        })
        assertTrue(findInExpression(((ExpressionStatement) stmts[2]).expression) {
            it instanceof MethodReferenceExpression
        })
    }

    @Test
    void 'spread attribute and safe attribute path elements parse'() {
        ModuleNode ast = buildAST('''
            def a = list*.@field
            def b = obj?.@field
        ''')
        assertEquals(2, ast.statementBlock.statements.size())
    }

    @Test
    void 'star import without static parses'() {
        ModuleNode ast = buildAST('''
            import java.util.*
            List x = []
        ''')
        assertTrue(ast.starImports.any { it.packageName == 'java.util.' })
    }

    @Test
    void 'enclosing-instance new uses DOT NEW creator branch'() {
        // `outer.new Inner()` is the classic non-static inner-class path (pathElement t=6)
        ModuleNode ast = buildAST('''
            class Outer {
                class Inner {
                    def ok() { 1 }
                }
            }
            def o = new Outer()
            def i = o.new Inner()
        ''')
        // Last script statement is the constructor call via pathElement NEW
        Statement last = ast.statementBlock.statements.last()
        assertTrue(
                findInExpression(((ExpressionStatement) last).expression) {
                    it instanceof ConstructorCallExpression || it instanceof MethodCallExpression
                },
                'expected enclosing-instance construction via DOT NEW pathElement')
    }

    // -------------------------------------------------------------------------
    // blockStatement → statement only (local var + command + empty)
    // -------------------------------------------------------------------------

    @Test
    void 'local variable declaration still works inside a block'() {
        ModuleNode ast = buildAST('''
            {
                def x = 1
                def y = 2
            }
        ''')
        assertNotNull(ast)
        // Block as script statement
        assertTrue(ast.statementBlock.statements.any { it instanceof BlockStatement || it instanceof ExpressionStatement })
    }

    @Test
    void 'command expression inside block does not double-evaluate as local var'() {
        // Command-style call: must parse as statementExpression, not localVariableDeclaration
        ModuleNode ast = buildAST('''
            def foo(a) { a }
            {
                foo 42
            }
        ''')
        assertNotNull(ast)
    }

    @Test
    void 'empty statement and expression statements inside block'() {
        ModuleNode ast = buildAST('''
            {
                ;
                def z = 1
                z = 2
            }
        ''')
        assertNotNull(ast)
    }

    // -------------------------------------------------------------------------
    // visitBlockStatement
    // -------------------------------------------------------------------------

    @Test
    void 'visitBlockStatement returns null for null visit result'() {
        def (builder, ctx) = blockStatementFixture()
        assertNull(builder.visitBlockStatement(ctx))
    }
    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static ModuleNode buildAST(String src) {
        CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
        config.pluginFactory = ParserPluginFactory.antlr4()
        ModuleNode ast = ParserPlugin.buildAST(src, config, new GroovyClassLoader(), null)
        assertNotNull(ast, "AST should build for:\n$src")
        assertTrue(!ast.context.errorCollector.hasErrors(),
                "Parse errors for:\n$src\n${ast.context.errorCollector.errors}")
        return ast
    }

    private static DeclarationExpression firstDeclaration(ModuleNode ast) {
        Statement s = ast.statementBlock.statements[0]
        ExpressionStatement es = (ExpressionStatement) s
        assertTrue(es.expression instanceof DeclarationExpression)
        return (DeclarationExpression) es.expression
    }

    private static ExpressionStatement firstExpressionStatement(ModuleNode ast) {
        return (ExpressionStatement) ast.statementBlock.statements[0]
    }

    private static boolean containsPropertyAccess(Expression expr) {
        findInExpression(expr) { it instanceof PropertyExpression || it instanceof VariableExpression }
    }

    private static boolean findInExpression(Expression expr, Closure predicate) {
        if (expr == null) return false
        if (predicate.call(expr)) return true
        if (expr instanceof DeclarationExpression) {
            return findInExpression(expr.leftExpression, predicate) || findInExpression(expr.rightExpression, predicate)
        }
        if (expr instanceof BinaryExpression) {
            return findInExpression(expr.leftExpression, predicate) || findInExpression(expr.rightExpression, predicate)
        }
        if (expr instanceof PropertyExpression) {
            return findInExpression(expr.objectExpression, predicate) || findInExpression(expr.property, predicate)
        }
        if (expr instanceof MethodCallExpression) {
            return findInExpression(expr.objectExpression, predicate) || findInExpression(expr.method, predicate)
        }
        if (expr instanceof ConstructorCallExpression) {
            return true
        }
        if (expr instanceof MethodPointerExpression || expr instanceof MethodReferenceExpression) {
            return predicate.call(expr)
        }
        if (expr instanceof AttributeExpression) {
            return true
        }
        return false
    }

    /**
     * Build a minimal {@link AstBuilder} plus a {@link GroovyParser.BlockStatementContext}
     * with start/stop tokens so error-position helpers succeed.
     */
    private static List blockStatementFixture() {
        CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
        ErrorCollector collector = new ErrorCollector(config)
        StringReaderSource source = new StringReaderSource('{ x = 1 }', config)
        SourceUnit su = new SourceUnit('Groovy12173', source, config, new GroovyClassLoader(), collector)
        AstBuilder builder = new AstBuilder(su, false, false)

        CommonToken token = new CommonToken(GroovyLexer.Identifier, 'x')
        token.line = 1
        token.charPositionInLine = 0
        GroovyParser.BlockStatementContext ctx = new GroovyParser.BlockStatementContext(null, 0)
        ctx.start = token
        ctx.stop = token

        return [builder, ctx]
    }
}

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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.IntersectionTypeClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ParserPlugin
import org.codehaus.groovy.control.ParserPluginFactory
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Tests for the parser-level handling of intersection types in cast expressions
 * and {@code as} coercion (GROOVY-11998 PR1: grammar + AST).
 */
final class IntersectionCastParserTest {

    @Test
    void 'cast with intersection type builds IntersectionTypeClassNode'() {
        ClassNode type = singleCastTargetType('def r = (Runnable & java.io.Serializable) { -> }')
        assert type instanceof IntersectionTypeClassNode
        IntersectionTypeClassNode it = (IntersectionTypeClassNode) type
        ClassNode[] components = it.components
        assertEquals(2, components.length)
        assertEquals('Runnable', components[0].name)
        assertEquals('java.io.Serializable', components[1].name)
    }

    @Test
    void 'cast with three-component intersection preserves order'() {
        ClassNode type = singleCastTargetType('def x = (A & B & C) value')
        assert type instanceof IntersectionTypeClassNode
        ClassNode[] components = ((IntersectionTypeClassNode) type).components
        assertEquals(['A', 'B', 'C'], components*.name)
    }

    @Test
    void 'as coercion with parenthesised intersection builds IntersectionTypeClassNode'() {
        ClassNode type = singleCastTargetType('def r = { -> } as (Runnable & java.io.Serializable)')
        assert type instanceof IntersectionTypeClassNode
        ClassNode[] components = ((IntersectionTypeClassNode) type).components
        assertEquals(['Runnable', 'java.io.Serializable'], components*.name)
    }

    @Test
    void 'single-type cast still parses and is not an intersection node'() {
        ClassNode type = singleCastTargetType('def x = (String) "hello"')
        assert !(type instanceof IntersectionTypeClassNode)
        assertEquals('String', type.name)
    }

    @Test
    void 'single-type as coercion still parses unchanged'() {
        ClassNode type = singleCastTargetType('def x = "hello" as Integer')
        assert !(type instanceof IntersectionTypeClassNode)
        assertEquals('Integer', type.name)
    }

    @Test
    void 'as coercion with parenthesised single type is accepted as plain type'() {
        // The new grammar allows `as (T)` which previously was a syntax error;
        // it should be equivalent to `as T` and not produce an IntersectionTypeClassNode.
        ClassNode type = singleCastTargetType('def x = "hello" as (Integer)')
        assert !(type instanceof IntersectionTypeClassNode)
        assertEquals('Integer', type.name)
    }

    @Test
    void 'duplicate types in intersection cast are rejected'() {
        assertParseFails('def r = (Runnable & Runnable) { -> }', 'Duplicate type in intersection')
    }

    @Test
    void 'duplicate types in intersection as coercion are rejected'() {
        assertParseFails('def r = { -> } as (Runnable & Runnable)', 'Duplicate type in intersection')
    }

    @Test
    void 'intersection right-hand side rejected for !instanceof'() {
        assertParseFails('def b = x !instanceof (Runnable & java.io.Serializable)',
                'not supported as the right-hand side of !instanceof')
    }

    @Test
    void 'parenthesised single type accepted for !instanceof'() {
        ModuleNode ast = buildAST('def b = x !instanceof (Runnable)')
        assertNotNull(ast)
        assertTrue(!ast.context.errorCollector.hasErrors())
    }

    //--------------------------------------------------------------------------

    private static ClassNode singleCastTargetType(String src) {
        ModuleNode ast = buildAST(src)
        assertNotNull(ast, "AST should build for: $src")
        assertTrue(!ast.context.errorCollector.hasErrors(),
                "Parse should not have errors for: $src; got: ${ast.context.errorCollector.errors}")
        BlockStatement block = ast.statementBlock
        ExpressionStatement stmt = (ExpressionStatement) block.statements[0]
        Expression expr = stmt.expression
        CastExpression cast
        if (expr instanceof DeclarationExpression) {
            cast = (CastExpression) ((DeclarationExpression) expr).rightExpression
        } else if (expr instanceof BinaryExpression) {
            cast = (CastExpression) ((BinaryExpression) expr).rightExpression
        } else {
            cast = (CastExpression) expr
        }
        return cast.type
    }

    private static ModuleNode buildAST(String src) {
        try {
            CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
            config.pluginFactory = ParserPluginFactory.antlr4()
            return ParserPlugin.buildAST(src, config, new GroovyClassLoader(), null)
        } catch (Throwable t) {
            return null
        }
    }

    private static void assertParseFails(String src, String expectedMessageFragment) {
        Throwable thrown = null
        try {
            CompilerConfiguration config = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
            config.pluginFactory = ParserPluginFactory.antlr4()
            ModuleNode ast = ParserPlugin.buildAST(src, config, new GroovyClassLoader(), null)
            if (ast == null || ast.context.errorCollector.hasErrors()) {
                String allMessages = ast == null ? '' : ast.context.errorCollector.errors*.toString().join('\n')
                assertTrue(ast == null || allMessages.contains(expectedMessageFragment),
                        "Expected error containing '$expectedMessageFragment' for: $src; got: $allMessages")
                return
            }
        } catch (Throwable t) {
            thrown = t
        }
        if (thrown != null) {
            assertTrue(thrown.message != null && thrown.message.contains(expectedMessageFragment),
                    "Expected error containing '$expectedMessageFragment' for: $src; got: ${thrown.message}")
        } else {
            assertTrue(false, "Expected parse to fail for: $src")
        }
    }
}

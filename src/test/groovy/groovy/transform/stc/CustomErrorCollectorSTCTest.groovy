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
package groovy.transform.stc

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Unit tests for custom error collector.
 */
final class CustomErrorCollectorSTCTest {

    @Test
    void testShouldNotFail() {
        CompilerConfiguration c = new CompilerConfiguration()
        c.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                def visitor = new StaticTypeCheckingVisitor(source, classNode)
                visitor.visitClass(classNode)
            }
        })
        GroovyShell shell = new GroovyShell(c)
        shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate('int x = new Object()')
        }

        c = new CompilerConfiguration()
        c.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                def visitor = new StaticTypeCheckingVisitor(source, classNode)
                visitor.typeCheckingContext.pushErrorCollector(new ErrorCollector(c))
                visitor.visitClass(classNode)
            }
        })
        shell = new GroovyShell(c)
        shouldFail(GroovyCastException) {
            shell.evaluate('int x = new Object()')
        }
    }

    @Test
    void testAddErrorAfterPoppingLastCollectorUsesSourceCollector() {
        CompilerConfiguration c = new CompilerConfiguration()
        c.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                def visitor = new StaticTypeCheckingVisitor(source, classNode)
                visitor.typeCheckingContext.popErrorCollector()
                visitor.visitClass(classNode)
            }
        })
        GroovyShell shell = new GroovyShell(c)
        shouldFail(MultipleCompilationErrorsException) {
            shell.evaluate('int x = new Object()')
        }
    }

    @Test
    void testPatternDominanceWarningAfterPoppingLastCollector() {
        CompilerConfiguration c = new CompilerConfiguration()
        List warnings = []
        c.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                def visitor = new StaticTypeCheckingVisitor(source, classNode)
                visitor.typeCheckingContext.popErrorCollector()
                visitor.visitClass(classNode)
                if (source.errorCollector.warnings != null) warnings.addAll(source.errorCollector.warnings)
            }
        })
        new GroovyShell(c).evaluate('''
            def m(Object o) {
                switch (o) {
                    case Number n  -> 'number'
                    case Integer i -> 'integer'
                    default        -> 'other'
                }
            }
            assert m(1) == 'number'
        ''')
        assert warnings.any { it.message.contains('dominated by a preceding case label') }
    }

    @Test
    void testFallThroughWhenSwitchSelectorNoLongerMatchesAndNoSwitchExpression() {
        CompilerConfiguration c = new CompilerConfiguration()
        c.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                def visitor = new StaticTypeCheckingVisitor(source, classNode)
                def selector = new ConstantExpression('text')
                def first = new CaseStatement(new ConstantExpression('text'), new ExpressionStatement(new ConstantExpression(1)))
                def second = new CaseStatement(new ConstantExpression('integer'), new ExpressionStatement(new ConstantExpression(2)))
                def statement = new SwitchStatement(selector, [first, second], EmptyStatement.INSTANCE)
                visitor.typeCheckingContext.pushEnclosingSwitchStatement(statement)
                // the statement no longer owns the selector that was pushed, and no
                // switch expression is enclosing the case
                statement.expression = new ConstantExpression('replaced')
                assert visitor.typeCheckingContext.enclosingSwitchExpression == null
                visitor.visitCaseStatement(first)
                visitor.typeCheckingContext.popEnclosingSwitchStatement()
            }
        })
        new GroovyShell(c).evaluate('true')
    }
}

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
package org.apache.groovy.macrolib

import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.fail

/**
 * Tests the {@code DO} macro: it rewrites {@code DO(name in expr, ...) { body }}
 * into the nested {@code Comprehensions.bind} chain, and rejects malformed
 * comprehensions with sourced compile errors.
 */
final class DoMacroTest {

    @Test
    void singleGenerator_optional() {
        assertScript '''
            assert DO(a in Optional.of(2)) { Optional.of(a + 1) }.get() == 3
        '''
    }

    @Test
    void multiGenerator_optional_and_shortCircuit() {
        assertScript '''
            assert DO(a in Optional.of(2),
                      b in Optional.of(3)) { Optional.of(a + b) }.get() == 5

            assert !DO(a in Optional.empty(),
                       b in Optional.of(3)) { Optional.of(a + b) }.present
        '''
    }

    @Test
    void laterGeneratorSeesEarlierBinding() {
        assertScript '''
            // b's source expression depends on a -> proves generator scoping
            assert DO(a in Optional.of(2),
                      b in Optional.of(a + 10)) { Optional.of(a + b) }.get() == 14
        '''
    }

    @Test
    void streamCartesian() {
        assertScript '''
            import java.util.stream.Stream
            def r = DO(a in Stream.of(1, 2),
                       b in Stream.of(10, 20)) { Stream.of(a + b) }
            assert r.toList() == [11, 21, 12, 22]
        '''
    }

    @Test
    void awaitable_usesThenCompose() {
        assertScript '''
            import groovy.concurrent.Awaitable
            import static org.apache.groovy.runtime.async.AsyncSupport.await

            def result = DO(a in Awaitable.of(2),
                            b in Awaitable.of(3)) { Awaitable.of(a + b) }
            assert await(result) == 5
        '''
    }

    @Test
    void structuralUserType() {
        assertScript '''
            import java.util.function.Function

            class Box {
                final Object v
                Box(Object v) { this.v = v }
                Box flatMap(Function f) { (Box) f.apply(v) }
                Box map(Function f) { new Box(f.apply(v)) }
            }

            def r = DO(a in new Box(2),
                       b in new Box(40)) { new Box(a + b) }
            assert r.v == 42
        '''
    }

    @Test
    void monadicAnnotatedUserType() {
        assertScript '''
            import groovy.transform.Monadic
            import java.util.function.Function

            @Monadic(bind = 'chain', map = 'transform')
            class Res {
                final Object v
                Res(Object v) { this.v = v }
                Res chain(Function f) { (Res) f.apply(v) }
                Res transform(Function f) { new Res(f.apply(v)) }
            }

            def r = DO(a in new Res(2),
                       b in new Res(3)) { new Res(a * b) }
            assert r.v == 6
        '''
    }

    @Test
    void structuralCarrierWithUntypedFlatMapWorksAtRuntime() {
        // Regression: the dispatcher's adaptClosure used to wrap the closure as a
        // java.util.function.Function when the carrier's flatMap declared an
        // Object parameter (the untyped-Groovy default), because
        // pt.isAssignableFrom(Function.class) is true for pt == Object. The
        // user's body typically calls c.call(x) expecting Closure semantics,
        // which fails against a Function wrapper. Untyped Object must now fall
        // through to asType, leaving the Closure unchanged.
        assertScript '''
            class Box {
                final int v
                Box(int v) { this.v = v }
                def flatMap(c) { c.call(v) }
                def map(c) { new Box(c.call(v)) }
            }
            def r = DO(a in new Box(2), b in new Box(3)) { new Box(a + b) }
            assert r.v == 5
        '''
    }

    @Test
    void rejectsGeneratorWithoutIn() {
        assertCompileError('DO(a, b in Optional.of(1)) { Optional.of(a) }',
            "DO generator must have the form 'name in expression'")
    }

    @Test
    void rejectsMissingClosureBody() {
        assertCompileError('def x = DO(a in Optional.of(1))', 'DO requires')
    }

    @Test
    void rejectsBodyClosureWithParameters() {
        assertCompileError('DO(a in Optional.of(1)) { x -> Optional.of(x) }',
            'must not declare parameters')
    }

    @Test
    void syntheticNodesCarrySourcePositions() {
        // Macro-emitted nodes default to (line, col) = (-1, -1); positionless
        // nodes are silently dropped by some STC paths (notably
        // StaticTypeCheckingVisitor.addStaticTypeError), and they break IDE
        // navigation. Every synthetic Comprehensions.bind call and lambda the
        // macro creates must inherit positions from the user's source.
        def script = '''
def r = DO(a in Optional.of(2),
           b in Optional.of(3)) { Optional.of(a + b) }
'''.trim()
        def nodes = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, script)
        def bindCalls = []
        def lambdas = []
        def visitor = new CodeVisitorSupport() {
            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                if (call.methodAsString == 'bind') bindCalls << call
                super.visitMethodCallExpression(call)
            }
            @Override
            void visitClosureExpression(ClosureExpression closure) {
                lambdas << closure
                super.visitClosureExpression(closure)
            }
        }
        nodes.findAll { it instanceof BlockStatement }.each { it.visit(visitor) }

        // Two generators => two nested Comprehensions.bind calls, both positionful.
        assert bindCalls.size() == 2
        bindCalls.each { call ->
            assert call.lineNumber > 0 : "synthetic bind call missing line: ${call.text}"
            assert call.columnNumber > 0
        }
        // The macro emits one lambda per generator (the user's body closure is
        // consumed for its statements, not its ClosureExpression); both
        // synthetic lambdas must carry positions.
        assert lambdas.size() == 2
        lambdas.each { lambda ->
            assert lambda.lineNumber > 0 : "synthetic lambda missing line"
            assert lambda.columnNumber > 0
        }
    }

    private static void assertCompileError(String script, String expectedMessage) {
        try {
            new GroovyShell().parse(script)
            fail("Expected a compilation error containing: $expectedMessage")
        } catch (MultipleCompilationErrorsException e) {
            assert e.message.contains(expectedMessage)
        }
    }
}

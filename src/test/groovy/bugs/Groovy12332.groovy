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

import org.antlr.v4.runtime.ParserRuleContext
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A finished class must not keep its parse tree or the parser's prediction
 * cache alive. Both are large for one big class and used to stay reachable
 * through class generation.
 *
 * <p>Parse context used to be stored on the {@link Parameter} itself. One
 * parameter that still holds it keeps the whole CST, because a rule context's
 * parent chain is the file. The walk that used to clear that metadata missed
 * closure parameters in instance initializers and in default parameter values.
 */
final class Groovy12332 {

    private static final List<String> PARSE_METADATA_KEYS = [
            '_PARAMETER_CONTEXT',
            '_PARAMETER_MODIFIER_MANAGER',
    ]

    private ModuleNode heldModule

    // GROOVY-12332
    @Test
    void testFinishedClassDoesNotPinParseGraph() {
        def unit = compile('''
            @groovy.transform.CompileStatic
            class C {
                int r
                { r = { int x -> x }(1) }
                int m(int a, String b) {
                    def c = { int x -> x + a }
                    c(1) + (b == null ? 0 : b.length()) + r
                }
            }
        ''')
        heldModule = unit.AST.modules[0]
        assertNoRetainedParseMetadata(heldModule, 'compile-static initializer', ['a', 'b', 'x'])

        def atn = currentParserAtn()
        assertNotNull(atn, 'precondition: the parser cache exists after a compile')
        def atnRef = new WeakReference(atn)
        clearParserWrapperSoftReference()
        atn = null

        boolean collected = false
        for (int i = 0; i < 50 && !collected; i++) {
            System.gc()
            collected = atnRef.get() == null
            if (!collected) Thread.sleep(10)
        }
        assertTrue(collected, 'the prediction cache must not stay strongly reachable from the finished class')
        assertNotNull(heldModule.classes[0].name)
    }

    // GROOVY-12332
    @ParameterizedTest(name = '{0}')
    @MethodSource('shapes')
    void testShapeDropsParameterParseMetadata(String name, String source, List<String> parameterNames) {
        assertScript(source)
        assertNoRetainedParseMetadata(compile(source).AST.modules[0], name, parameterNames)
    }

    static List<Arguments> shapes() {
        [
            Arguments.of('method closure', '''
                class C {
                    int m(int a, String b) {
                        def c = { int x -> x + a }
                        c(1) + (b == null ? 0 : b.length())
                    }
                }
                assert new C().m(2, null) == 3
            ''', ['a', 'b', 'x']),
            Arguments.of('instance initializer closure', '''
                class C {
                    int r
                    { r = { int x -> x }(1) }
                }
                assert new C().r == 1
            ''', ['x']),
            Arguments.of('nested initializer closures', '''
                class C {
                    int r
                    { r = { int x -> { int y -> y }(x) }(1) }
                }
                assert new C().r == 1
            ''', ['x', 'y']),
            Arguments.of('method default closure', '''
                class C {
                    def m(f = { int x -> x }) { f(1) }
                }
                assert new C().m() == 1
            ''', ['f', 'x']),
            Arguments.of('constructor default closure', '''
                class C {
                    int r
                    C(f = { int x -> x }) { r = f(5) }
                }
                assert new C().r == 5
            ''', ['f', 'x']),
            Arguments.of('field initializer closure', '''
                class C {
                    def c = { int x -> x }
                    int m() { c(7) }
                }
                assert new C().m() == 7
            ''', ['x']),
            Arguments.of('static initializer closure', '''
                class C {
                    static int r
                    static { r = { int x -> x }(4) }
                }
                assert C.r == 4
            ''', ['x']),
            Arguments.of('inner class initializer closure', '''
                class O {
                    class I {
                        int r
                        { r = { int x -> x }(1) }
                    }
                }
                assert new O().new I().r == 1
            ''', ['x']),
            Arguments.of('record component and default closure', '''
                record R(int a, String b = 'z') {
                    def m(Closure f = { int x -> x + a }) { f(1) }
                }
                assert new R(2).m() == 3
                assert new R(2).b == 'z'
            ''', ['a', 'b', 'f', 'x']),
            Arguments.of('record component default closure', '''
                record R(int a = { int x -> x }(3)) {}
                assert new R().a == 3
            ''', ['a', 'x']),
            Arguments.of('explicit lambda', '''
                class C {
                    int m() {
                        def f = (int x) -> x + 1
                        f(1)
                    }
                }
                assert new C().m() == 2
            ''', ['x']),
            Arguments.of('script closure', '''
                def f = { int x -> x + 1 }
                assert f(1) == 2
            ''', ['x']),
            Arguments.of('compile-static initializer closure', '''
                @groovy.transform.CompileStatic
                class C {
                    int r
                    { r = { int x -> x }(1) }
                    int m(int a, String b) {
                        def c = { int x -> x + a }
                        c(1) + (b == null ? 0 : b.length()) + r
                    }
                }
                assert new C().m(2, null) == 4
            ''', ['a', 'b', 'x']),
        ]
    }

    private static CompilationUnit compile(String source) {
        def unit = new CompilationUnit()
        // Not C.groovy: a script body plus `class C` in a file named C is a duplicate class.
        unit.addSource('Shape.groovy', source)
        unit.compile()
        unit
    }

    private static void assertNoRetainedParseMetadata(ModuleNode module, String name, List<String> parameterNames) {
        List<String> hits = []
        List<Parameter> parameters = []
        def scanner = new ParseMetadataScanner(hits, parameters)
        if (module.statementBlock != null) module.statementBlock.visit(scanner)
        for (ClassNode classNode : module.classes) {
            scanner.visitClass(classNode)
        }
        for (String parameterName : parameterNames) {
            assertTrue(parameters.any { it.name == parameterName },
                    name + ': walk did not reach parameter ' + parameterName)
        }
        assertTrue(hits.isEmpty(), name + ' still carries parse metadata:\n' + hits.join('\n'))
    }

    /**
     * Reaches parameters the old post-parse walk did not: object initializers,
     * parameter default values, and parameters stashed in node metadata
     * (the record header). Also rejects the two metadata keys on every AST
     * node the walk touches, not only on parameters.
     */
    private static final class ParseMetadataScanner extends ClassCodeVisitorSupport {

        private final List<String> hits
        private final List<Parameter> parameters
        private final Set<ASTNode> seen = Collections.newSetFromMap(new IdentityHashMap<>())
        private final Set<ClassNode> visitedClasses = Collections.newSetFromMap(new IdentityHashMap<>())

        private ParseMetadataScanner(List<String> hits, List<Parameter> parameters) {
            this.hits = hits
            this.parameters = parameters
        }

        @Override
        protected SourceUnit getSourceUnit() {
            null
        }

        @Override
        void visitClass(ClassNode node) {
            if (node == null || !visitedClasses.add(node)) return
            note(node)
            super.visitClass(node)
        }

        @Override
        protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
            note(node)
            if (node.parameters != null) {
                for (Parameter parameter : node.parameters) note(parameter)
            }
            super.visitConstructorOrMethod(node, isConstructor)
        }

        @Override
        void visitField(FieldNode node) {
            note(node)
            super.visitField(node)
        }

        @Override
        void visitProperty(PropertyNode node) {
            note(node)
            super.visitProperty(node)
        }

        @Override
        void visitClosureExpression(ClosureExpression expression) {
            note(expression)
            if (expression.parameters != null) {
                for (Parameter parameter : expression.parameters) note(parameter)
            }
            super.visitClosureExpression(expression)
        }

        @Override
        protected void visitStatement(Statement statement) {
            note(statement)
        }

        private void note(ASTNode node) {
            if (node == null || !seen.add(node)) return
            if (node instanceof Parameter) parameters.add((Parameter) node)
            Map metaDataMap = node.metaDataMap
            if (metaDataMap == null) return
            for (String key : PARSE_METADATA_KEYS) {
                if (metaDataMap.containsKey(key)) {
                    hits.add(describe(node) + ' still has ' + key)
                }
            }
            for (Object value : metaDataMap.values()) {
                if (value instanceof ParserRuleContext) {
                    hits.add(describe(node) + ' retains ' + value.getClass().simpleName)
                } else if (value instanceof Parameter) {
                    note((Parameter) value)
                } else if (value instanceof Parameter[]) {
                    for (Parameter parameter : (Parameter[]) value) note(parameter)
                }
            }
            if (node instanceof Parameter) {
                Parameter parameter = (Parameter) node
                if (parameter.hasInitialExpression()) parameter.initialExpression.visit(this)
            }
        }

        private static String describe(ASTNode node) {
            if (node instanceof Parameter) return 'parameter ' + ((Parameter) node).name
            return node.getClass().simpleName
        }
    }

    private static Object currentParserAtn() {
        def wrapper = parserWrapperSoftReference()?.get()
        if (wrapper == null) return null
        def atnField = wrapper.getClass().getDeclaredField('atn')
        atnField.accessible = true
        atnField.get(wrapper)
    }

    private static void clearParserWrapperSoftReference() {
        parserWrapperSoftReference()?.clear()
    }

    private static SoftReference parserWrapperSoftReference() {
        def manager = Class.forName('org.apache.groovy.parser.antlr4.internal.atnmanager.ParserAtnManager').INSTANCE
        def refField = Class.forName('org.apache.groovy.parser.antlr4.internal.atnmanager.AtnManager')
                .getDeclaredField('atnWrapperSoftReference')
        refField.accessible = true
        (SoftReference) refField.get(manager)
    }
}

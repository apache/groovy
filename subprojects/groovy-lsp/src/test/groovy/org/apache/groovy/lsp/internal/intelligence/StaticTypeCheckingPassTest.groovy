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
package org.apache.groovy.lsp.internal.intelligence

import groovy.transform.TypeChecked
import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

final class StaticTypeCheckingPassTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void applyNullAndEmptyUnitAreNoOps() {
        StaticTypeCheckingPass.apply(null)
        StaticTypeCheckingPass.apply(new CompilationUnit())
        StaticTypeCheckingPass.bindTargets(null)
    }

    @Test
    void uncompiledSourceHasNoModule() {
        def unit = new CompilationUnit()
        unit.addSource('Empty.groovy', 'class Empty {}')
        StaticTypeCheckingPass.apply(unit)
    }

    @Test
    void typeCheckedCallIsBound() {
        def unit = unitOf('Hello.groovy', '''\
            @groovy.transform.TypeChecked
            class Hello {
                def foo(String name) { foo("x") }
            }
            '''.stripIndent())
        def call = firstCall(moduleOf(unit), 'foo')
        assert call?.methodTarget != null
        assert call.methodTarget.name == 'foo'
    }

    @Test
    void compileStaticCallIsBound() {
        def unit = unitOf('Hello.groovy', '''\
            @groovy.transform.CompileStatic
            class Hello {
                def foo(String name) { foo("x") }
            }
            '''.stripIndent())
        assert firstCall(moduleOf(unit), 'foo')?.methodTarget != null
    }

    @Test
    void methodLevelTypeCheckedCallIsBound() {
        def unit = unitOf('Hello.groovy', '''\
            class Hello {
                @groovy.transform.TypeChecked
                def foo(String name) { foo("x") }
                def bar() { bar() }
            }
            '''.stripIndent())
        def module = moduleOf(unit)
        assert firstCall(module, 'foo')?.methodTarget != null
        assert firstCall(module, 'bar')?.methodTarget == null
    }

    @Test
    void skipModeClassIsNotBound() {
        def unit = unitOf('Hello.groovy', '''\
            import groovy.transform.TypeChecked
            import groovy.transform.TypeCheckingMode
            @TypeChecked(TypeCheckingMode.SKIP)
            class Hello {
                def foo(String name) { foo("x") }
            }
            '''.stripIndent())
        assert firstCall(moduleOf(unit), 'foo')?.methodTarget == null
    }

    @Test
    void unannotatedCallIsNotBound() {
        def unit = unitOf('Hello.groovy', '''\
            class Hello {
                def foo(String name) { foo("x") }
            }
            '''.stripIndent())
        assert firstCall(moduleOf(unit), 'foo')?.methodTarget == null
    }

    @Test
    void typeCheckedLocalUsesInferredTypeOnHover() {
        fixture = new LspFixture()
        def src = '''\
            @groovy.transform.TypeChecked
            class Hello {
                def foo() {
                    def n = "x"
                    n
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def hover = fixture.server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(3, 12))).get()
        assert hover != null
        def value = hover.contents.isRight() ? hover.contents.getRight().value : hover.contents.getLeft()
        assert value.toString().contains('String')
    }

    @Test
    void typeCheckedUnknownCallAddsAnError() {
        def unit = unitOf('Hello.groovy', '''\
            @groovy.transform.TypeChecked
            class Hello {
                def foo() { unknownMethod() }
            }
            '''.stripIndent())
        assert unit.errorCollector.errorCount > 0
    }

    @Test
    void throwingClassDoesNotDropTheUnit() {
        def unit = unitOf('Hello.groovy', '''\
            @groovy.transform.TypeChecked
            class Hello {
                def foo(String name) { foo("x") }
            }
            '''.stripIndent())
        def source = unit.iterator().next()
        def call = firstCall(source.AST, 'foo')
        source.AST.classes.add(null)
        source.AST.classes.add(new ExplodingClassNode())
        StaticTypeCheckingPass.apply(unit)
        assert call?.methodTarget != null
    }

    @Test
    void bindTargetsCopiesMethodNodeMetadataOnly() {
        def unit = unitOf('Hello.groovy', '''\
            @groovy.transform.TypeChecked
            class Hello {
                def foo(String name) { foo("x") }
            }
            '''.stripIndent())
        def module = moduleOf(unit)
        def call = firstCall(module, 'foo')
        def bound = call.methodTarget
        assert bound != null
        StaticTypeCheckingPass.apply(unit)
        assert call.methodTarget == bound
        call.methodTarget = null
        call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, 'not-a-method')
        StaticTypeCheckingPass.bindTargets(module.classes[0])
        assert call.methodTarget == null
        call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, bound)
        StaticTypeCheckingPass.bindTargets(module.classes[0])
        assert call.methodTarget == bound
    }

    private static CompilationUnit unitOf(String name, String src) {
        def unit = new CompilationUnit(CompilerSettings.defaults().toConfiguration())
        unit.addSource(name, src)
        try {
            unit.compile(Phases.SEMANTIC_ANALYSIS)
        } catch (CompilationFailedException ignored) {
        }
        StaticTypeCheckingPass.apply(unit)
        unit
    }

    private static ModuleNode moduleOf(CompilationUnit unit) {
        unit.iterator().next().AST
    }

    private static MethodCallExpression firstCall(ModuleNode module, String name) {
        def call = null
        AstQuery.walk(module, { node, ctx ->
            if (node instanceof MethodCallExpression && node.methodAsString == name && call == null) {
                call = node
            }
        })
        call
    }

    private static final class ExplodingClassNode extends ClassNode {
        ExplodingClassNode() {
            super('Explode', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
            addAnnotation(new AnnotationNode(ClassHelper.make(TypeChecked)))
        }

        @Override
        List<MethodNode> getMethods() {
            throw new IllegalStateException('boom')
        }
    }
}

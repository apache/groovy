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
package org.apache.groovy.lsp.internal.feature

import java.nio.file.Path
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.util.Uris
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.PackageNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.io.TempDir
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT
import static org.objectweb.asm.Opcodes.ACC_INTERFACE
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC

import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.compile.Identifiers
import org.apache.groovy.lsp.internal.compile.ImportSupport
import org.apache.groovy.lsp.internal.compile.PackageGuess
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.nio.file.Files

final class MetalsInspiredTest {
    private static final PositionEncoding UTF16 = PositionEncoding.UTF16


    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void unusedImportIsDetectedFromSource() {
        def compiler = new GroovyCompiler()
        def uri = URI.create('file:///tmp/Unused.groovy')
        def src = 'import java.util.concurrent.ConcurrentHashMap\nclass Hello {}\n'
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, src)],
                [], CompilerSettings.defaults(),
                MetalsInspiredTest.classLoader)
        def unused = ImportSupport.unused(snapshot.get(uri))
        assert unused
        assert ImportSupport.unusedDiagnostics(snapshot.get(uri), PositionEncoding.UTF16)
        compiler.close()
    }

    @Test
    void unusedImportIsHintedAndRemovable() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.concurrent.ConcurrentHashMap
            class Hello {}
            '''.stripIndent())
        def hints = fixture.publishedDiagnostics().findAll {
            DiagnosticConverter.diagnosticMessage(it) == 'Unused import'
        }
        assert hints
        assert hints[0].tags
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        hints[0].range, new CodeActionContext(hints))).get()
        assert actions.any { it.getRight().title == 'Remove unused imports' }
        def onlyFixAll = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        hints[0].range, new CodeActionContext(hints, ['source.fixAll']))).get()
        assert onlyFixAll.every { it.isRight() && it.getRight().kind == 'source.fixAll' }
        assert onlyFixAll.any { it.getRight().title == 'Remove unused imports' }
        def onlyOrganize = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        hints[0].range, new CodeActionContext(hints, ['source.organizeImports']))).get()
        assert onlyOrganize.every { it.isRight() && !it.getRight().kind.startsWith('source.generate') }
        assert onlyOrganize.every { it.isRight() && it.getRight().kind != 'source.fixAll' }
    }

    @Test
    void onTypeOutdentsStructuralBraceButNotStringBrace() {
        fixture = new LspFixture()
        def closer = fixture.open('Hello.groovy', 'class Hello {\n    }\n')
        def outdent = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(closer),
                        new FormattingOptions(4, true), new Position(1, 5), '}')).get()
        assert outdent
        assert outdent[0].newText == ''
        def quoted = fixture.open('Quoted.groovy', 'def s = "foo}"\n')
        def skipped = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(quoted),
                        new FormattingOptions(4, true), new Position(0, 13), '}')).get()
        assert skipped.isEmpty()
    }

    @Test
    void implementAbstractMethodsInsertsOverrideStub() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            interface Named { String name() }
            class Hello implements Named {
            }
            '''.stripIndent())
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(1, 6), new Position(1, 11)),
                        new CodeActionContext([]))).get()
        def impl = actions.find { it.getRight().title == 'Implement abstract methods' }
        assert impl != null
        def edit = impl.getRight().edit.changes.values().flatten()[0]
        assert edit.newText.contains('name()')
        assert edit.newText.contains('@Override')
    }

    @Test
    void createMissingClassStub() {
        fixture = new LspFixture()
        def uri = fixture.open('Use.groovy', 'class Use {}\n')
        def diagnostic = new Diagnostic()
        diagnostic.range = new Range(new Position(0, 0), new Position(0, 1))
        diagnostic.message = Either.forLeft('unable to resolve class Widget')
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri), diagnostic.range,
                        new CodeActionContext([diagnostic]))).get()
        assert actions.any { it.getRight().title == 'Create class Widget' }
        def stub = actions.find { it.getRight().title == 'Create class Widget' }.getRight().edit.changes.values().flatten()[0]
        assert stub.newText.contains('class Widget')
    }

    @Test
    void insertInferredTypeReplacesDef() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def name = "x"
            }
            '''.stripIndent())
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(1, 8), new Position(1, 12)),
                        new CodeActionContext([]))).get()
        def inferred = actions.find { it.getRight().title.startsWith('Insert inferred type') }
        assert inferred != null
        assert inferred.getRight().edit.changes.values().flatten()[0].newText in ['String', 'GString']
    }

    @Test
    void packageGuessFromConventionalRoot() {
        def root = Files.createTempDirectory('groovy-lsp-pkg')
        Files.createDirectories(root.resolve('src/main/groovy/com/acme'))
        def file = root.resolve('src/main/groovy/com/acme/Hello.groovy')
        Files.writeString(file, 'class Hello {}\n')
        assert PackageGuess.fromUri(file.toUri(), [root.toUri()], []) == 'com.acme'
        assert PackageGuess.fromUri(null, [], []) == ''
    }

    @Test
    void identifierWordCountUsesBoundaries() {
        assert Identifiers.count('import foo.Bar\nBar x', 'Bar') == 2
        assert Identifiers.count('import foo.Bar\nclass Hello {}', 'Bar') == 1
        assert !Identifiers.containsWord('Barrier', 'Bar')
        assert Identifiers.count(null, 'Bar') == 0
    }

    @Test
    void onTypeNewlineIndentsAfterBrace() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n\n}\n')
        def edits = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(uri),
                        new FormattingOptions(4, true), new Position(1, 0), '\n')).get()
        assert edits
        assert edits[0].newText == '    '
    }

    @Test
    void hoverFallsBackToIdentifierWhenCompileMisses() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        def hover = fixture.server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(0, 8))).get()
        assert hover != null
    }

    @Test
    void importedNamePrefersAlias() {
        assert ImportSupport.importedName(null) == null
    }

    @Test
    void overrideCompletionInsertsOverrideStub() {
        fixture = new LspFixture()
        def src = '''\
            interface Named { String name() }
            class Hello implements Named {
                
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def list = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(2, 4))).get()
        def items = list.getRight().items
        def override = items.find { it.detail?.startsWith('override') && it.label == 'name' }
        assert override != null
        assert override.insertText.contains('@Override')
        assert override.insertText.contains('name()')
    }

    @Test
    void namedArgumentCompletionUsesBoundParameter() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo(String name, int age) { foo("x", 1) }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def line = src.split('\n')[1]
        def at = line.indexOf('foo("x"') + 4
        def list = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, at))).get()
        def labels = list.getRight().items*.label
        assert 'name' in labels
        assert 'age' in labels
    }

    @Test
    void convertToNamedArgumentsRewritesCall() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo(String name, int age) { }
                def bar() { foo("a", 1) }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(2, 16), new Position(2, 19)),
                        new CodeActionContext([]))).get()
        def named = actions.find { it.isRight() && it.getRight().title == 'Convert to named arguments' }
        assert named != null
        assert named.getRight().kind == 'refactor.rewrite'
        def text = named.getRight().edit.changes.values().flatten()[0].newText
        assert text.contains('name:')
        assert text.contains('age:')
    }

    @Test
    void insertInferredTypeForLocal() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def bar() {
                    def n = "x"
                }
            }
            '''.stripIndent())
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(2, 12), new Position(2, 13)),
                        new CodeActionContext([]))).get()
        def inferred = actions.find { it.getRight().title.startsWith('Insert inferred type') }
        assert inferred != null
        assert inferred.getRight().edit.changes.values().flatten()[0].newText in ['String', 'GString']
    }

    @Test
    void importGroupFolds() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.concurrent.ConcurrentHashMap
            import java.util.ArrayList
            class Hello {}
            '''.stripIndent())
        def folds = fixture.server.textDocumentService.foldingRange(
                new FoldingRangeRequestParams(new TextDocumentIdentifier(uri))).get()
        assert folds.any { it.kind == 'imports' && it.endLine > it.startLine }
    }

    @Test
    void gotoSuperMethodFindsOverrideTarget() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Base { def foo() { } }
            class Hello extends Base { def foo() { } }
            '''.stripIndent())
        def locations = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.gotoSuperMethod', [uri, 1, 32])).get()
        assert locations
        assert locations[0].range.start.line == 0
    }

    @Test
    void addAllUnambiguousImportsWhenTwoHits() {
        fixture = new LspFixture()
        fixture.open('a/Widget.groovy', 'package a\nclass Widget {}\n')
        fixture.open('b/Gadget.groovy', 'package b\nclass Gadget {}\n')
        def uri = fixture.open('Use.groovy', 'package c\nclass Use {}\n')
        def widget = new Diagnostic()
        widget.range = new Range(new Position(0, 0), new Position(0, 1))
        widget.message = Either.forLeft('unable to resolve class Widget')
        def gadget = new Diagnostic()
        gadget.range = new Range(new Position(0, 0), new Position(0, 1))
        gadget.message = Either.forLeft('unable to resolve class Gadget')
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri), widget.range,
                        new CodeActionContext([widget, gadget]))).get()
        assert actions.any { it.getRight().title == 'Add all unambiguous imports' }
    }
    @Test
    void contributeWithoutSnapshotIsEmpty() {
        def document = new TextDocument(fileUri('file:///tmp/cov-none.groovy'), 'groovy', 1, 'class Use {}\n')
        assert new CodeActionService().contribute(document, null, [], null, UTF16, [], []).isEmpty()
        def actions = new CodeActionService().collect(document, null, [unresolved('demo.Widget')], null, UTF16, [], [])
        assert titles(actions) == ['Organize imports']
        assert CodeActionService.unresolvedClassName(null) == null
        assert CodeActionService.unresolvedClassName('unable to resolve class demo.Widget') == 'demo.Widget'
    }

    @Test
    void packageActionInsertsOrReplaces(@TempDir Path folder) {
        def root = folder.resolve('src/main/groovy/com/acme')
        Files.createDirectories(root)
        def bare = root.resolve('Hello.groovy')
        def bareUri = fileUri(bare)
        def bareSnap = compileOf(bareUri, 'class Hello {}\n')
        def inserted = new CodeActionService().contribute(bareSnap.get(bareUri).toTextDocument(), bareSnap, [],
                null, UTF16, [folder.toUri()], [])
        def add = inserted.find { it.isRight() && it.getRight().title == 'Add package com.acme' }
        assert add != null
        assert editText(add) == 'package com.acme\n\n'

        def wrong = root.resolve('Wrong.groovy')
        def wrongUri = fileUri(wrong)
        def wrongSnap = compileOf(wrongUri, 'package wrong\nclass Hello {}\n')
        def replaced = new CodeActionService().contribute(wrongSnap.get(wrongUri).toTextDocument(), wrongSnap, [],
                null, UTF16, [folder.toUri()], [])
        def fix = replaced.find { it.isRight() && it.getRight().title == 'Add package com.acme' }
        assert fix != null
        assert editText(fix) == 'package com.acme'

        def otherUri = fileUri(root.resolve('Other.groovy'))
        def module = new ModuleNode((SourceUnit) null)
        module.setPackage(new PackageNode('wrong'))
        def hello = new ClassNode('Hello', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        hello.lineNumber = 2
        hello.columnNumber = 1
        hello.lastLineNumber = 2
        hello.lastColumnNumber = 12
        module.addClass(hello)
        def compiled = new CompiledDocument(otherUri, 1, 'package wrong\nclass Hello {}\n', module, null, null)
        def skipped = new CodeActionService().contribute(compiled.toTextDocument(), snapshot(compiled), [],
                null, UTF16, [folder.toUri()], [])
        assert titles(skipped).every { !it.startsWith('Add package') }
    }

    @Test
    void inferredTypeSkipsUnresolvedAndEmbeddedDef() {
        def uri = fileUri('file:///tmp/cov-infer.groovy')
        def src = '''\
            class Hello {
                def bare
                def bar() {
                    String typed = "x"
                    def loose
                }
            }
            '''.stripIndent()
        def snap = compileOf(uri, src)
        def actions = new CodeActionService().contribute(snap.get(uri).toTextDocument(), snap, [], null, UTF16, [], [])
        assert titles(actions).every { !it.startsWith('Insert inferred type') }

        def owner = new ClassNode('Hello', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def field = owner.addField('name', ACC_PUBLIC, ClassHelper.STRING_TYPE, null)
        field.lineNumber = 1
        field.columnNumber = 1
        field.lastLineNumber = 1
        field.lastColumnNumber = 5
        def module = new ModuleNode((SourceUnit) null)
        module.addClass(owner)
        def text = 'name defdef\n'
        def compiled = new CompiledDocument(fileUri('file:///tmp/cov-defdef.groovy'), 1, text, module, null, null)
        def embedded = new CodeActionService().contribute(compiled.toTextDocument(), snapshot(compiled), [],
                null, UTF16, [], [])
        assert titles(embedded).every { !it.startsWith('Insert inferred type') }
    }

    @Test
    void namedArgumentsCoverStaticConstructorAndGuards() {
        def uri = fileUri('file:///tmp/cov-named.groovy')
        def src = '''\
            class Hello {
                Hello(String name) {}
                static void go(String name) {}
                void foo(String name) {}
                void bar() {
                    go("a")
                    new Hello("b")
                    foo(name: "a")
                }
            }
            '''.stripIndent()
        def snap = compileOf(uri, src)
        def calls = []
        AstQuery.walk(snap.get(uri).module) { node, ctx ->
            if (node instanceof MethodCallExpression || node instanceof StaticMethodCallExpression
                    || node instanceof ConstructorCallExpression) {
                calls << node
            }
        }
        def foo = calls.find { it instanceof MethodCallExpression && it.methodAsString == 'foo' }
        assert foo != null
        assert foo.arguments instanceof TupleExpression
        assert foo.arguments.expressions.any { it instanceof MapExpression }
        assert MethodBinding.resolveMethod(foo, snap)?.name == 'foo'
        assert calls.any { it instanceof StaticMethodCallExpression && it.method == 'go' }
        assert calls.any { it instanceof ConstructorCallExpression }
        def actions = new CodeActionService().contribute(snap.get(uri).toTextDocument(), snap, [], null, UTF16, [], [])
        def named = actions.findAll { it.isRight() && it.getRight().title == 'Convert to named arguments' }
        assert named.size() == 2
        assert named.every { editText(it).contains('name:') }

        def guarded = guardedNamedCalls()
        def guardedActions = new CodeActionService().contribute(guarded.toTextDocument(), snapshot(guarded), [],
                null, UTF16, [], [])
        def converted = guardedActions.findAll { it.isRight() && it.getRight().title == 'Convert to named arguments' }
        assert converted.size() == 1
        assert editText(converted[0]) == 'name: '
    }

    @Test
    void createClassUsesSimpleNameAtEndWithoutNewline() {
        def text = 'class Use {}'
        def uri = fileUri('file:///tmp/cov-create.groovy')
        def module = new ModuleNode((SourceUnit) null)
        def type = new ClassNode('Use', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        type.lineNumber = 1
        type.columnNumber = 7
        type.lastLineNumber = 1
        type.lastColumnNumber = 10
        module.addClass(type)
        def compiled = new CompiledDocument(uri, 1, text, module, null, null)
        def actions = new CodeActionService().contribute(compiled.toTextDocument(), snapshot(compiled),
                [unresolved('widget'), unresolved('com.acme.Widget')], null, UTF16, [], [])
        def creates = titles(actions).findAll { it.startsWith('Create class') }
        assert creates == ['Create class Widget']
        def created = actions.find { it.isRight() && it.getRight().title == 'Create class Widget' }
        assert editText(created).contains('class Widget')
        assert created.getRight().edit.changes.values().collectMany { it }[0].range.start.line == 0
        assert created.getRight().edit.changes.values().collectMany { it }[0].range.start.character == text.length()
    }

    @Test
    void abstractMembersFilterSyntheticGroovyObjectAndBodies() {
        def snippets = new MethodNode('run', ACC_PUBLIC, ClassHelper.int_TYPE,
                [new Parameter(ClassHelper.STRING_TYPE, 'name'), new Parameter(ClassHelper.int_TYPE, 'age')] as Parameter[],
                ClassNode.EMPTY_ARRAY, null)
        def snippet = CodeActionService.overrideSnippet(snippets)
        assert snippet.contains('int run(')
        assert snippet.contains('String name, int age')
        def objectMethod = new MethodNode('box', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert CodeActionService.overrideSnippet(objectMethod).startsWith('@Override\ndef box(')
        def voidMethod = new MethodNode('go', ACC_PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert CodeActionService.overrideSnippet(voidMethod).contains('void go(')

        def groovyObject = new ClassNode('groovy.lang.GroovyObject', ACC_INTERFACE | ACC_ABSTRACT, ClassHelper.OBJECT_TYPE)
        groovyObject.addMethod('ping', ACC_PUBLIC | ACC_ABSTRACT, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        def host = new ClassNode('Host', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        host.addInterface(groovyObject)
        assert host.abstractMethods.any { it.name == 'ping' && it.declaringClass.name == 'groovy.lang.GroovyObject' }
        assert CodeActionService.missingAbstracts(host).every { it.name != 'ping' }

        def box = new ClassNode('Box', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def hidden = box.addMethod('hidden', ACC_PUBLIC | ACC_ABSTRACT | ACC_SYNTHETIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        hidden.synthetic = true
        assert box.abstractMethods.any { it.name == 'hidden' }
        assert CodeActionService.missingAbstracts(box).every { it.name != 'hidden' }

        def face = new ClassNode('Face', ACC_INTERFACE | ACC_ABSTRACT, ClassHelper.OBJECT_TYPE)
        face.addMethod('run', ACC_PUBLIC | ACC_ABSTRACT, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        def body = new ClassNode('Body', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        body.addInterface(face)
        body.addMethod('run', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new EmptyStatement())
        assert body.abstractMethods.any { it.name == 'run' && it.abstract }
        assert CodeActionService.missingAbstracts(body).every { it.declaringClass?.name != 'Face' }

        def pending = new ClassNode('Pending', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        pending.addInterface(face)
        assert CodeActionService.missingAbstracts(pending)
        def module = new ModuleNode((SourceUnit) null)
        module.addClass(face)
        module.addClass(pending)
        def compiled = new CompiledDocument(fileUri('file:///tmp/cov-pending.groovy'), 1, 'class Pending {}\n',
                module, null, null)
        def actions = new CodeActionService().contribute(compiled.toTextDocument(), snapshot(compiled), [],
                new Range(new Position(0, 0), new Position(0, 4)), UTF16, [], [])
        assert titles(actions).every { it != 'Implement abstract methods' }
    }

    @Test
    void organizeImportsSortsAndRejectsBadSpans() {
        def uri = fileUri('file:///tmp/cov-imports.groovy')
        def src = '''\
            import java.util.concurrent.ConcurrentHashMap
            import java.util.ArrayList
            class Hello {}
            '''.stripIndent()
        def snap = compileOf(uri, src)
        def sorted = new CodeActionService().collect(snap.get(uri).toTextDocument(), snap, null, null, UTF16, [], [])
        def organize = sorted.find { it.isRight() && it.getRight().title == 'Organize imports' }
        assert organize.getRight().edit != null
        def edited = editText(organize)
        assert edited.indexOf('import java.util.ArrayList') < edited.indexOf('import java.util.concurrent.ConcurrentHashMap')
        assert titles(sorted).every { !it.startsWith('Add import') }
        assert titles(sorted).every { !it.startsWith('Create class') }

        def service = new CodeActionService()
        assert service.organizeImports(null, UTF16).isEmpty()
        def noModule = new CompiledDocument(fileUri('file:///tmp/cov-nomodule.groovy'), 1, 'import java.util.List\n',
                null, null, null)
        assert service.organizeImports(noModule, UTF16).isEmpty()

        def unpositioned = importDocument(fileUri('file:///tmp/cov-unpos.groovy'), 'import java.util.List\n', -1, 1, -1, 1)
        assert service.organizeImports(unpositioned, UTF16).isEmpty()

        def beyond = importDocument(fileUri('file:///tmp/cov-beyond.groovy'), 'import java.util.List\n', 40, 1, 40, 5)
        assert service.organizeImports(beyond, UTF16)[0].newText.contains('import java.util.List')

        def inverted = importDocument(fileUri('file:///tmp/cov-invert.groovy'), 'import java.util.List\n', 1, 10, 1, 3)
        assert service.organizeImports(inverted, UTF16)[0].newText.contains('import java.util.List')
    }

    @Test
    void importFixesUseQualifiedNamesAndSkipSamePackage() {
        def widgetUri = fileUri('file:///tmp/cov-demo-widget.groovy')
        def sameUri = fileUri('file:///tmp/cov-demo-use.groovy')
        def otherUri = fileUri('file:///tmp/cov-other-use.groovy')
        def snap = compileOf([
                new TextDocument(widgetUri, 'groovy', 1, 'package demo\nclass Widget {}\n'),
                new TextDocument(sameUri, 'groovy', 1, 'package demo\nclass Use {}\n'),
                new TextDocument(otherUri, 'groovy', 1, 'package other\nclass Use {}\n')
        ])
        assert snap.types().bySimpleName('Widget').size() == 1
        def service = new CodeActionService()
        def same = service.collect(snap.get(sameUri).toTextDocument(), snap, [unresolved('Widget')],
                null, UTF16, [], [])
        assert titles(same).every { it != 'Add import for demo.Widget' }
        def qualified = service.collect(snap.get(otherUri).toTextDocument(), snap, [unresolved('demo.Widget')],
                null, UTF16, [], [])
        assert titles(qualified).contains('Add import for demo.Widget')
    }

    private static CompiledDocument guardedNamedCalls() {
        def owner = new ClassNode('Hello', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def name = new Parameter(ClassHelper.STRING_TYPE, 'name')
        def dollar = new Parameter(ClassHelper.STRING_TYPE, '$name')
        owner.addMethod('go', ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE, [name] as Parameter[],
                ClassNode.EMPTY_ARRAY, null)
        def hidden = owner.addMethod('hidden', ACC_PUBLIC, ClassHelper.VOID_TYPE, [dollar] as Parameter[],
                ClassNode.EMPTY_ARRAY, null)
        def plain = owner.addMethod('plain', ACC_PUBLIC, ClassHelper.VOID_TYPE, [name] as Parameter[],
                ClassNode.EMPTY_ARRAY, null)
        def shown = owner.addMethod('shown', ACC_PUBLIC, ClassHelper.VOID_TYPE, [name] as Parameter[],
                ClassNode.EMPTY_ARRAY, null)
        def mapCall = new StaticMethodCallExpression(owner, 'go', new MapExpression([
                new MapEntryExpression(new ConstantExpression('name'), new ConstantExpression('a'))
        ]))
        def dollarCall = new MethodCallExpression(new VariableExpression('this'), 'hidden',
                new ArgumentListExpression(new ConstantExpression('a')))
        dollarCall.methodTarget = hidden
        def plainCall = new MethodCallExpression(new VariableExpression('this'), 'plain',
                new ArgumentListExpression(new ConstantExpression('a')))
        plainCall.methodTarget = plain
        def arg = new ConstantExpression('a')
        arg.lineNumber = 1
        arg.columnNumber = 8
        arg.lastLineNumber = 1
        arg.lastColumnNumber = 2
        def inverted = new MethodCallExpression(new VariableExpression('this'), 'shown',
                new ArgumentListExpression(arg))
        inverted.methodTarget = shown
        def block = new BlockStatement()
        block.addStatement(new ExpressionStatement(mapCall))
        block.addStatement(new ExpressionStatement(dollarCall))
        block.addStatement(new ExpressionStatement(plainCall))
        block.addStatement(new ExpressionStatement(inverted))
        owner.addMethod('bar', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block)
        def module = new ModuleNode((SourceUnit) null)
        module.addClass(owner)
        new CompiledDocument(fileUri('file:///tmp/cov-guard.groovy'), 1, 'abcdefghijklmnop', module, null, null)
    }

    private static CompiledDocument importDocument(URI uri, String text, int line, int column, int lastLine, int lastColumn) {
        def module = new ModuleNode((SourceUnit) null)
        module.addImport('List', ClassHelper.LIST_TYPE)
        def imp = module.imports[0]
        imp.lineNumber = line
        imp.columnNumber = column
        imp.lastLineNumber = lastLine
        imp.lastColumnNumber = lastColumn
        def type = new ClassNode('Hello', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        type.lineNumber = 1
        module.addClass(type)
        new CompiledDocument(uri, 1, text, module, null, null)
    }

    private CompilationSnapshot compileOf(URI uri, String text) {
        compileOf([new TextDocument(uri, 'groovy', 1, text)])
    }

    private CompilationSnapshot compileOf(List<TextDocument> documents) {
        def compiler = new GroovyCompiler()
        try {
            return compiler.compile(documents, [], CompilerSettings.defaults(), getClass().classLoader)
        } finally {
            compiler.close()
        }
    }

    private static CompilationSnapshot snapshot(CompiledDocument... documents) {
        def map = [:]
        documents.each { map[it.uri] = it }
        new CompilationSnapshot(map)
    }

    private static URI fileUri(String path) {
        Uris.normalize(URI.create(path))
    }

    private static URI fileUri(Path path) {
        Uris.normalize(path.toUri())
    }

    private static Diagnostic unresolved(String name) {
        def diagnostic = new Diagnostic()
        diagnostic.range = new Range(new Position(0, 0), new Position(0, 1))
        diagnostic.message = Either.forLeft('unable to resolve class ' + name)
        diagnostic
    }

    private static List<String> titles(List actions) {
        actions.findAll { it.isRight() }.collect { it.getRight().title }
    }

    private static String editText(def action) {
        action.getRight().edit.changes.values().collectMany { it }[0].newText
    }

    @Test
    void codeActionsForPackageNamedArgsAndGeneration(@TempDir Path folder) {
        def root = folder.resolve('workspace')
        def src = root.resolve('src/main/groovy/com/acme')
        Files.createDirectories(src)
        def source = '''\
            class Hello {
                private String hidden
                String left
                String right
                void ping(String name, int count) {}
                void use() {
                    ping('a', 1)
                    new Hello('a', 'b')
                    Hello.util(1, 2)
                }
                static void util(int left, int right) { }
                Hello(String left, String right) {
                    this.left = left
                }
            }
            '''.stripIndent()
        def file = src.resolve('Hello.groovy')
        Files.writeString(file, 'package com.other\n' + source)
        fixture = new LspFixture()
        fixture.server.context.addWorkspaceFolder(root.toUri())
        def uri = fixture.open(file.toString(), 'package com.other\n' + source)
        def snapshot = fixture.server.context.snapshot
        def compiled = snapshot.get(uri)
        def service = new CodeActionService()
        def actions = service.collect(compiled.toTextDocument(), snapshot, null,
                new Range(new Position(0, 0), new Position(30, 0)), UTF16,
                fixture.server.context.workspaceFolders(), [])
        assert actions.any { it.isRight() && it.getRight().title.contains('com.acme') }
        assert actions.any { it.isRight() && it.getRight().title == 'Generate getters and setters' }
        assert actions.any { it.isRight() && it.getRight().title.startsWith('Convert to named arguments') }

        def missingPackage = src.resolve('Bare.groovy')
        def bare = 'class Bare {}\n'
        Files.writeString(missingPackage, bare)
        def bareUri = fixture.open(missingPackage.toString(), bare)
        def bareActions = service.collect(fixture.server.context.snapshot.get(bareUri).toTextDocument(),
                fixture.server.context.snapshot, [],
                new Range(new Position(0, 0), new Position(1, 1)), UTF16,
                fixture.server.context.workspaceFolders(), [])
        assert bareActions.any { it.isRight() && it.getRight().title == 'Add package com.acme' }

        def unsorted = '''\
            import static java.lang.Math.PI
            import java.util.Map
            import java.util.List
            class Sorted {
                List a
                Map b
                def n = PI
            }
            '''.stripIndent()
        def unsortedUri = fixture.open('Sorted.groovy', unsorted)
        def unsortedDoc = fixture.server.context.snapshot.get(unsortedUri)
        def organized = service.organizeImports(unsortedDoc, UTF16)
        assert organized
        assert organized[0].newText.indexOf('import java.util.List') < organized[0].newText.indexOf('import static')
        def sorted = '''\
            import java.util.List
            import java.util.Map
            import static java.lang.Math.PI
            class Already {
                List a
                Map b
                def n = PI
            }
            '''.stripIndent()
        def sortedUri = fixture.open('Already.groovy', sorted)
        assert service.organizeImports(fixture.server.context.snapshot.get(sortedUri), UTF16).isEmpty()
        assert service.organizeImports(null, UTF16).isEmpty()
        assert CodeActionService.unresolvedClassName(null) == null
        assert CodeActionService.unresolvedClassName('nope') == null
        assert CodeActionService.unresolvedClassName('unable to resolve class com.acme.Widget') == 'com.acme.Widget'

        def lower = new Diagnostic(new Range(new Position(0, 0), new Position(0, 1)), 'unable to resolve class widget')
        def dotted = new Diagnostic(new Range(new Position(0, 0), new Position(0, 1)), 'unable to resolve class com.acme.Widget')
        def created = service.collect(compiled.toTextDocument(), snapshot, [lower, dotted],
                new Range(new Position(0, 0), new Position(1, 1)), UTF16, [], [])
        assert !created.any { it.isRight() && it.getRight().title == 'Create class widget' }
        assert created.any { it.isRight() && it.getRight().title == 'Create class Widget' }

        def emptyUri = fixture.open('Empty.groovy', '')
        def emptyDoc = fixture.server.context.snapshot.get(emptyUri)
        if (emptyDoc != null) {
            def onEmpty = service.collect(emptyDoc.toTextDocument(), fixture.server.context.snapshot,
                    [dotted], new Range(new Position(0, 0), new Position(0, 0)), UTF16, [], [])
            assert onEmpty.any { it.isRight() && it.getRight().title == 'Create class Widget' }
        }

        def params = new CodeActionParams(new TextDocumentIdentifier(uri),
                new Range(new Position(0, 0), new Position(2, 1)), new CodeActionContext([]))
        assert fixture.server.textDocumentService.codeAction(params).get() != null
    }

}

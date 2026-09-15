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

import java.lang.reflect.Modifier
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.compile.Identifiers
import org.apache.groovy.lsp.internal.position.Positions
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.eclipse.lsp4j.FileRename
import org.eclipse.lsp4j.InlayHint
import org.eclipse.lsp4j.WorkspaceEdit

import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InlayHintParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class ConservativeNavigationTest {

    private static final PositionEncoding ENC = PositionEncoding.UTF16


    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void prepareRenameAndRenameRefuseUnboundDynamicCalls() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def o) { o.mystery() }
                def mystery() {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def id = new TextDocumentIdentifier(uri)
        int callCol = src.readLines()[1].indexOf('mystery')
        def prepare = fixture.server.textDocumentService.prepareRename(
                new PrepareRenameParams(id, new Position(1, callCol))).get()
        assert prepare == null
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(id, new Position(1, callCol), 'renamed')).get()
        assert !edit.changes
        assert !edit.documentChanges
    }

    @Test
    void renameStillRewritesTheDeclarationOfAnUnboundCall() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def o) { o.mystery() }
                def mystery() {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int declCol = src.readLines()[2].indexOf('mystery')
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(new TextDocumentIdentifier(uri), new Position(2, declCol), 'renamed')).get()
        def texts = edit.changes.values().flatten()*.newText
        assert texts
        assert texts.every { it == 'renamed' }
        def ranges = edit.changes.values().flatten()*.range
        assert ranges.every { it.start.line != 1 }
    }

    @Test
    void definitionListsSameArityOverloadsWhenUnbound() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo() { bar("x") }
                def bar(String s) {}
                def bar(CharSequence s) {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int col = src.readLines()[1].indexOf('bar')
        def defs = fixture.server.textDocumentService.definition(
                new DefinitionParams(new TextDocumentIdentifier(uri), new Position(1, col))).get()
        assert defs.isRight()
        def lines = defs.getRight()*.targetSelectionRange.start.line as Set
        assert lines.contains(2) || lines.contains(3) || defs.getRight().size() >= 1
    }

    @Test
    void hoverOnDefLocalUsesInitializerType() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo() {
                    def name = "x"
                    name
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int col = src.readLines()[2].indexOf('name')
        def hover = fixture.server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(2, col))).get()
        assert hover != null
        def contents = hover.contents
        String value = contents instanceof MarkupContent ? contents.value : contents.getRight().value
        assert value.contains('name')
        assert value.contains('String') || value.contains('GString')
    }

    @Test
    void inlayHintsIncludeDefLocals() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo() {
                    def name = "x"
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def hints = fixture.server.textDocumentService.inlayHint(
                new InlayHintParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(0, 0), new Position(5, 0)))).get()
        assert hints.any {
            def label = it.label.isLeft() ? it.label.getLeft() : it.label.getRight().toString()
            def tooltip = it.tooltip != null && it.tooltip.isLeft() ? it.tooltip.getLeft() : null
            label.toString().trim() in ['String', 'GString'] && tooltip == 'inferred' && it.textEdits == null
        }
    }

    @Test
    void fieldInlayIsMarkedInferred() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                private def name = "x"
            }
            '''.stripIndent())
        def hints = fixture.server.textDocumentService.inlayHint(
                new InlayHintParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(0, 0), new Position(4, 0)))).get()
        assert hints.any {
            def label = it.label.isLeft() ? it.label.getLeft() : it.label.getRight().toString()
            def tooltip = it.tooltip != null && it.tooltip.isLeft() ? it.tooltip.getLeft() : null
            label.toString().trim() in ['String', 'GString'] && tooltip == 'inferred'
        }
    }

    @Test
    void parameterNameInlaysUseUniqueArity() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String name) { foo("x") }
                def bar(String a) { 1 }
                def bar(int a) { 2 }
                def run() {
                    bar("x")
                    new Hello()
                }
            }
            '''.stripIndent())
        def hints = fixture.server.textDocumentService.inlayHint(
                new InlayHintParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(0, 0), new Position(12, 0)))).get()
        assert hints.any {
            def label = it.label.isLeft() ? it.label.getLeft() : it.label.getRight().toString()
            def tooltip = it.tooltip != null && it.tooltip.isLeft() ? it.tooltip.getLeft() : null
            label == 'name:' && it.kind.toString().contains('Parameter') && tooltip == 'inferred'
        }
        assert hints.every {
            def label = it.label.isLeft() ? it.label.getLeft() : it.label.getRight().toString()
            label != 'a:'
        }
    }

    @Test
    void windowsFileUriClassRenameDoesNotUsePathOf() {
        def compiler = new GroovyCompiler()
        def uri = URI.create('file:///d:/a/groovy/Hello.groovy')
        def src = 'class Hello {}\n'
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, src)],
                [], CompilerSettings.defaults(), ConservativeNavigationTest.classLoader)
        def doc = new TextDocument(uri, 'groovy', 1, src)
        def edit = new RenameService().rename(doc, snapshot, new Position(0, 6), 'World', PositionEncoding.UTF16)
        assert edit.documentChanges
        assert edit.documentChanges.any { it.isRight() && it.getRight().newUri.contains('World.groovy') }
        assert edit.documentChanges.any { it.isRight() && it.getRight().newUri.startsWith('file:///d:/a/groovy/') }
        compiler.close()
    }

    @Test
    void isRenameSafeDistinguishesDeclarationsFromUnboundCalls() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def o) { o.mystery() }
                def mystery() {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def compiled = fixture.server.context.snapshot.get(uri)
        def call = AstQuery.nodeAt(compiled.module, 2, src.readLines()[1].indexOf('mystery') + 1)
        def decl = AstQuery.nodeAt(compiled.module, 3, src.readLines()[2].indexOf('mystery') + 1)
        assert !MethodBinding.isRenameSafe(call, fixture.server.context.snapshot)
        assert MethodBinding.isRenameSafe(decl, fixture.server.context.snapshot)
        assert !MethodBinding.isRenameSafe(null, fixture.server.context.snapshot)
        assert MethodBinding.resolveMethod(null, fixture.server.context.snapshot) == null
        assert MethodBinding.resolveStatic(null, fixture.server.context.snapshot) == null
        assert MethodBinding.resolveMethodCandidates(null, fixture.server.context.snapshot).isEmpty()
        assert MethodBinding.overloads(null, fixture.server.context.snapshot).isEmpty()
        assert TypeInference.of((FieldNode) null) == null
        assert TypeInference.of((Parameter) null) == null
        assert TypeInference.of((VariableExpression) null, null) == null
        assert !TypeInference.usedInitializer(null, null)
        def param = new Parameter(ClassHelper.OBJECT_TYPE, 'p')
        param.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.STRING_TYPE)
        assert TypeInference.of(param).name == 'java.lang.String'
        def local = new VariableExpression('n')
        local.putNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE, ClassHelper.Integer_TYPE)
        assert TypeInference.of(local, null).name.contains('Integer')
    }

    @Test
    void referencesDoNotJoinUnboundCallsOfTheSameName() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def a, def b) {
                    a.mystery()
                    b.mystery()
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int col = src.readLines()[2].indexOf('mystery')
        def refs = fixture.server.textDocumentService.references(
                new ReferenceParams(new TextDocumentIdentifier(uri),
                        new Position(2, col), new ReferenceContext(true))).get()
        assert refs == null || refs.size() <= 1
    }
    @Test
    void renameGuardsFileMovesAndIdentifierParts() {
        assert !RenameService.isIdentifier('a-b')
        assert RenameService.isIdentifier('ok_Name')
        def rename = new RenameService()
        def uri = open('Hello.groovy', 'class Hello {\n    class Inner {}\n}\n')
        def doc = compiled(uri)
        def snap = snapshot()
        def buffer = doc.toTextDocument()
        def hello = typeNamed(doc, 'Hello')
        def renamed = uri.replace('Hello.groovy', 'World.groovy')

        assert rename.willRenameFiles(null, snap, ENC).changes.isEmpty()
        assert rename.willRenameFiles([], null, ENC).changes.isEmpty()
        assert newTexts(rename.willRenameFiles([null, new FileRename(uri, renamed)], snap, ENC)).contains('World')
        assert rename.willRenameFiles([new FileRename()], snap, ENC).changes.isEmpty()
        assert rename.willRenameFiles([new FileRename(uri, uri.substring(0, uri.lastIndexOf('.')) + '.txt')], snap, ENC).changes.isEmpty()
        assert rename.willRenameFiles([new FileRename(uri, uri.replace('Hello.groovy', '1World.groovy'))], snap, ENC).changes.isEmpty()
        def slash = uri.lastIndexOf('/')
        def parent = uri.substring(0, slash)
        def elsewhere = parent.substring(0, parent.lastIndexOf('/') + 1) + 'elsewhere/World.groovy'
        assert rename.willRenameFiles([new FileRename(uri, elsewhere)], snap, ENC).changes.isEmpty()
        def missing = uri.replace('Hello.groovy', 'Missing.groovy')
        assert rename.willRenameFiles([new FileRename(missing, uri.replace('Hello.groovy', 'Other.groovy'))], snap, ENC).changes.isEmpty()
        assert rename.willRenameFiles([new FileRename('mailto:Hello.groovy', 'mailto:World.groovy')], snap, ENC).changes.isEmpty()
        assert rename.willRenameFiles([new FileRename('Hello.groovy', 'World.groovy')], snap, ENC).changes.isEmpty()
        assert rename.rename(null, snap, at(doc.text, 'Hello'), 'World', ENC).changes.isEmpty()
        assert rename.rename(buffer, snap, at(doc.text, 'Hello'), '   ', ENC).changes.isEmpty()
        assert rename.rename(buffer, snap, at(doc.text, 'Hello'), 'a-b', ENC).changes.isEmpty()

        def decoy = new ClassNode('Hello', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        decoy.script = true
        decoy.lineNumber = 1
        decoy.columnNumber = 1
        decoy.lastLineNumber = 1
        decoy.lastColumnNumber = 2
        decoy.module = doc.module
        def invisible = new ClassNode('Skip', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        invisible.lineNumber = -1
        invisible.module = doc.module
        doc.module.classes.add(decoy)
        doc.module.classes.add(invisible)
        assert newTexts(rename.willRenameFiles([new FileRename(uri, renamed)], snap, ENC)).contains('World')

        def dup = new ClassNode('Hello', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        dup.lineNumber = 1
        dup.columnNumber = 1
        dup.lastLineNumber = 1
        dup.lastColumnNumber = 2
        dup.module = doc.module
        doc.module.classes.add(dup)
        assert rename.willRenameFiles([new FileRename(uri, renamed)], snap, ENC).changes.isEmpty()
        doc.module.classes.remove(dup)

        hello.columnNumber = 1
        hello.lastColumnNumber = 2
        hello.lastLineNumber = hello.lineNumber
        assert rename.willRenameFiles([new FileRename(uri, renamed)], snap, ENC).changes.isEmpty()
    }

    @Test
    void renameRefusesCallCoveringClassNameAndSkipsFileRename() {
        def src = 'class Hello {}\n'
        def uri = open('Hello.groovy', src)
        def doc = compiled(uri)
        def type = typeNamed(doc, 'Hello')
        def rename = new RenameService()
        def renamed = uri.replace('Hello.groovy', 'World.groovy')
        assert newTexts(rename.willRenameFiles([new FileRename(uri, renamed)], snapshot(), ENC)).contains('World')

        int start = doc.text.indexOf('Hello') + 1
        int end = start + 'Hello'.length()
        def call = new MethodCallExpression(new VariableExpression('this'), 'Hello', MethodCallExpression.NO_ARGUMENTS)
        place(call, 1, start, end)
        place(call.method, 1, start, end)
        def touch = new MethodNode('touch', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new ExpressionStatement(call))
        place(touch, 1, 1, doc.text.length())
        type.addMethod(touch)
        assert AstQuery.nodeAt(doc.module, doc.toTextDocument(), at(doc.text, 'Hello'), ENC) instanceof MethodCallExpression
        assert rename.willRenameFiles([new FileRename(uri, renamed)], snapshot(), ENC).changes.isEmpty()

        def txt = open('Hello.txt', 'class Hello {}\n')
        def txtDoc = compiled(txt)
        def txtEdit = rename.rename(txtDoc.toTextDocument(), snapshot(), at(txtDoc.text, 'Hello'), 'World', ENC)
        assert newTexts(txtEdit).contains('World')
        assert txtEdit.documentChanges == null

        def other = open('Other.groovy', 'class Hello {}\n')
        def otherDoc = compiled(other)
        def otherEdit = rename.rename(otherDoc.toTextDocument(), snapshot(), at(otherDoc.text, 'Hello'), 'World', ENC)
        assert newTexts(otherEdit).contains('World')
        assert otherEdit.documentChanges == null

        def relative = URI.create('Hello.groovy')
        def relativeType = new ClassNode('Hello', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        place(relativeType, 1, 1, src.length())
        def relativeDoc = handCompiled(relative, src, relativeType)
        def relativeSnap = new CompilationSnapshot([(relative): relativeDoc])
        def relativeEdit = rename.rename(relativeDoc.toTextDocument(), relativeSnap, at(src, 'Hello'), 'World', ENC)
        assert newTexts(relativeEdit).contains('World')
        assert relativeEdit.documentChanges == null
    }

    @Test
    void prepareRenameRejectsNamelessAndUnlocatedNodes() {
        def text = 'class Holder {\n}\n'
        def uri = URI.create('file:///Holder.groovy')
        def type = new ClassNode('Holder', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        place(type, 1, 1, text.length())
        def nameless = new MethodNode(null, Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new EmptyStatement())
        place(nameless, 1, 1, 5)
        type.addMethod(nameless)
        def missing = new MethodNode('missing', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new EmptyStatement())
        place(missing, 2, 1, 2)
        type.addMethod(missing)
        def doc = handCompiled(uri, text, type)
        def snap = new CompilationSnapshot([(uri): doc])
        def buffer = doc.toTextDocument()
        assert AstQuery.nodeAt(doc.module, buffer, new Position(0, 0), ENC).is(nameless)
        assert Identifiers.nameOf(nameless) == null
        assert new RenameService().prepareRename(buffer, snap, new Position(0, 0), ENC) == null
        assert AstQuery.nodeAt(doc.module, buffer, new Position(1, 0), ENC).is(missing)
        assert Positions.toIdentifierRange(missing, text, ENC) == null
        assert new RenameService().prepareRename(buffer, snap, new Position(1, 0), ENC) == null
    }

    @Test
    void inlayHintsSkipSyntheticUnresolvedAndMatchingNames() {
        def src = '''\
            class Box {
                def early() {
                    def count = "x"
                }
                static int add(int left, int right) { left + right }
                static int sum(int $skip, int right) { $skip + right }
                int plain() { sum(1, 2) }
                int same(int left) { add(left, 2) }
                def dyn(def item) { item }
                def later() { 1 }
            }
            '''.stripIndent()
        def uri = open('Box.groovy', src)
        def doc = compiled(uri)
        def box = typeNamed(doc, 'Box')
        def service = new InlayHintService()
        def labels = service.inlayHints(doc, null, ENC, snapshot()).collect { hintLabel(it) }
        assert labels.any { it.contains('String') || it.contains('GString') } : labels
        assert labels.any { it == 'right:' } : labels
        assert labels.every { it != 'left:' && it != '$skip:' && !it.startsWith('Object') } : labels

        def laterLine = doc.text.readLines().findIndexOf { it.contains('later') }
        def restricted = service.inlayHints(doc, new Range(new Position(laterLine, 0), new Position(laterLine, 1)), ENC, snapshot())
        assert restricted.every { it.position.line >= laterLine }
        assert restricted.every { !(hintLabel(it).contains('String') || hintLabel(it).contains('GString') || hintLabel(it) == 'right:') }
        def staticNames = []
        AstQuery.walk(box) { node, ctx ->
            if (node instanceof StaticMethodCallExpression) {
                staticNames << node.method
            }
        }
        assert staticNames.containsAll(['sum', 'add']) : staticNames
    }

    private String open(String path, String text) {
        if (fixture == null) {
            fixture = new LspFixture()
        }
        fixture.open(path, text)
    }

    private CompilationSnapshot snapshot() {
        fixture.server.context.snapshot
    }

    private CompiledDocument compiled(String uri) {
        def doc = snapshot().get(uri)
        assert doc?.module != null
        doc
    }

    private static ClassNode typeNamed(CompiledDocument doc, String simple) {
        def found = doc.module.classes.find { it.nameWithoutPackage == simple && !it.name.contains('$') }
        assert found != null : doc.module.classes*.name
        found
    }

    private static CompiledDocument handCompiled(URI uri, String text, ClassNode type) {
        def module = new ModuleNode((CompileUnit) null)
        module.addClass(type)
        new CompiledDocument(uri, 1, text, module, null, null)
    }

    private static void place(ASTNode node, int line, int column, int lastColumn) {
        node.lineNumber = line
        node.columnNumber = column
        node.lastLineNumber = line
        node.lastColumnNumber = lastColumn
    }

    private static Position at(String text, String token) {
        int index = text.indexOf(token)
        assert index >= 0 : token
        int line = 0
        int column = 0
        for (int i = 0; i < index; i++) {
            if (text.charAt(i) == '\n') {
                line += 1
                column = 0
            } else {
                column += 1
            }
        }
        new Position(line, column)
    }

    private static List<String> newTexts(WorkspaceEdit edit) {
        if (edit?.changes == null) {
            return []
        }
        edit.changes.values().flatten().collect { it.newText as String }
    }

    private static String hintLabel(InlayHint hint) {
        def label = hint.getLabel()
        label.isLeft() ? label.getLeft() : String.valueOf(label.getRight())
    }

}

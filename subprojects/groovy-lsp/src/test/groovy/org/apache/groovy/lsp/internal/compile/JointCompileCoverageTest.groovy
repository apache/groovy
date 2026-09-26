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
package org.apache.groovy.lsp.internal.compile

import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter
import org.apache.groovy.lsp.internal.feature.CodeActionService
import org.apache.groovy.lsp.internal.feature.HoverService
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.util.Uris
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

import javax.tools.ToolProvider

final class JointCompileCoverageTest {

    @TempDir
    Path folder

    @Test
    void diskJavaIsJointCompiledAndDiagnosticsArePublished() {
        assumeJavac()
        def javaFile = folder.resolve('Widget.java')
        Files.writeString(javaFile, '''\
            public class Widget {
                public static int COUNT = 1;
                public String id() { return "w"; }
                public static class Inner {}
            }
            interface Named { String name(); }
            enum Kind { A, B }
            record Point(int x, int y) {}
            '''.stripIndent())
        def broken = folder.resolve('Broken.java')
        Files.writeString(broken, 'public class Broken {')
        def groovyUri = Uris.normalize(folder.resolve('Use.groovy').toUri())
        def compiler = new GroovyCompiler()
        try {
            def snapshot = compiler.compile(
                    [new TextDocument(groovyUri, 'groovy', 1, 'class Use { Widget w; Widget.Inner i }')],
                    [javaFile, broken, folder.resolve('missing.java')],
                    CompilerSettings.defaults(),
                    JointCompileCoverageTest.classLoader)
            def javaUri = Uris.normalize(javaFile.toUri())
            assert snapshot.javaSymbols().type('Widget') != null
            assert snapshot.javaSymbols().members('Widget', 'id')
            assert snapshot.javaSymbols().members('Widget', 'COUNT')
            assert snapshot.javaSymbols().type('Widget$Inner') != null
            assert snapshot.javaSymbols().type('Named') != null
            assert snapshot.javaSymbols().type('Kind') != null
            assert snapshot.javaSymbols().type('Point') != null
            assert snapshot.types().byName('Widget') != null
            def brokenDoc = snapshot.get(Uris.normalize(broken.toUri()))
            assert brokenDoc != null
            assert brokenDoc.javaMessages
            assert brokenDoc.toTextDocument().languageId == 'java'
            def converter = new DiagnosticConverter()
            def diagnostics = converter.convert(brokenDoc, PositionEncoding.UTF16)
            assert diagnostics.every { it.source == 'javac' }
            def use = snapshot.get(groovyUri)
            assert use.errors.every { !it.toString().contains('unable to resolve class Widget') }
        } finally {
            compiler.close()
        }
    }

    @Test
    void overlayAndDiskShareTheSameJavaUri() {
        assumeJavac()
        def javaFile = folder.resolve('Shared.java')
        Files.writeString(javaFile, 'public class Shared { public int stale = 1; }\n')
        def uri = Uris.normalize(javaFile.toUri())
        def compiler = new GroovyCompiler()
        try {
            def snapshot = compiler.compile(
                    [new TextDocument(uri, 'java', 1, 'public class Shared { public int fresh = 2; }\n'),
                     new TextDocument(folder.resolve('G.groovy').toUri(), 'groovy', 1, 'class G { Shared s }')],
                    [javaFile],
                    CompilerSettings.defaults(),
                    JointCompileCoverageTest.classLoader)
            assert snapshot.javaSymbols().type('Shared') != null
        } finally {
            compiler.close()
        }
    }

    @Test
    void lspJavaCompilerGuardsAndEmptyOverlays() {
        assumeJavac()
        def compiler = new LspJavaCompiler(null)
        compiler.compile(null, null)
        assert compiler.index() == JavaSymbolIndex.EMPTY
        compiler.compile(['/no/such/File.java', null], new CompilationUnit())
        def created = compiler.createCompiler(new CompilerConfiguration())
        assert created.is(compiler)
    }

    @Test
    void javaSymbolIndexNullGuards() {
        assert JavaSymbolIndex.of(null, null) == JavaSymbolIndex.EMPTY
        assert JavaSymbolIndex.EMPTY.type(null) == null
        assert JavaSymbolIndex.EMPTY.typesNamed(null).isEmpty()
        assert JavaSymbolIndex.EMPTY.members(null, 'x').isEmpty()
        assert JavaSymbolIndex.EMPTY.members('Owner', null).isEmpty()
        assert JavaSymbolIndex.EMPTY.types().isEmpty()
    }

    @Test
    void identifiersWordAroundEdges() {
        assert Identifiers.wordAround(null, 0) == ''
        assert Identifiers.wordAround('', 0) == ''
        assert Identifiers.wordAround('   ', 1) == ''
        assert Identifiers.wordAround('foo', 99) == 'foo'
        assert Identifiers.wordAround('foo', -1) == 'foo'
    }

    @Test
    void compiledDocumentJavaLanguageAndMessages() {
        def uri = URI.create('file:///tmp/A.java')
        def base = new CompiledDocument(uri, 1, 'class A {}', null, null, null)
        assert base.toTextDocument().languageId == 'java'
        def extra = [new JavaMessage(1, 1, 1, 2, 'cannot find symbol', true),
                     new JavaMessage(2, 1, 2, 2, 'note', false)]
        def with = base.withJavaMessages(extra)
        assert with.javaMessages.size() == 2
        assert with.javaMessages[0].error()
        assert with.javaMessages[0].text() == 'cannot find symbol'
        assert with.withJavaMessages(null).javaMessages.isEmpty()
        def converter = new DiagnosticConverter()
        def diagnostics = converter.convert(with, PositionEncoding.UTF16)
        assert diagnostics.size() == 2
        assert diagnostics[0].source == 'javac'
        assert diagnostics[0].severity.toString().contains('Error')
    }

    @Test
    void hoverFindsUniqueTypeName() {
        assumeJavac()
        def javaFile = folder.resolve('Only.java')
        Files.writeString(javaFile, 'public class Only {}\n')
        def groovyUri = Uris.normalize(folder.resolve('H.groovy').toUri())
        def compiler = new GroovyCompiler()
        try {
            def snapshot = compiler.compile(
                    [new TextDocument(groovyUri, 'groovy', 1, 'class H { Only o }')],
                    [javaFile],
                    CompilerSettings.defaults(),
                    JointCompileCoverageTest.classLoader)
            def document = snapshot.get(groovyUri).toTextDocument()
            def line = document.text.readLines().findIndexOf { it.contains('Only') }
            def column = document.text.readLines()[line].indexOf('Only') + 1
            def hover = new HoverService().hover(document, snapshot, new Position(line, column), PositionEncoding.UTF16)
            assert hover != null
            def markdown = hover.contents.getRight().value
            assert markdown.contains('Only')
        } finally {
            compiler.close()
        }
    }

    @Test
    void scannerFindsJavaSources() {
        Files.writeString(folder.resolve('A.groovy'), 'class A {}')
        Files.writeString(folder.resolve('B.java'), 'class B {}')
        def found = new WorkspaceScanner().scan([folder.toUri()], [])
        assert found.any { it.fileName.toString() == 'B.java' }
        assert found.any { it.fileName.toString() == 'A.groovy' }
    }

    @Test
    void addJavaFileIgnoresNonFileUris() {
        def compiler = new GroovyCompiler()
        try {
            def snapshot = compiler.compile(
                    [new TextDocument(URI.create('untitled:Scratch.java'), 'java', 1, 'public class Scratch {}'),
                     new TextDocument(folder.resolve('G.groovy').toUri(), 'groovy', 1, 'class G {}')],
                    null,
                    CompilerSettings.defaults(),
                    JointCompileCoverageTest.classLoader)
            assert snapshot.get(URI.create('untitled:Scratch.java')) != null
        } finally {
            compiler.close()
        }
    }

    @Test
    void codeActionsTolerateNullDocument() {
        assert new CodeActionService().collect(null, null, null, null, null, null, null).isEmpty()
    }

    @Test
    void compilationSnapshotEmptyJavaSymbols() {
        assert CompilationSnapshot.EMPTY.javaSymbols() == JavaSymbolIndex.EMPTY
        assert new CompilationSnapshot(Map.of(), null).javaSymbols() == JavaSymbolIndex.EMPTY
    }

    private static void assumeJavac() {
        Assumptions.assumeTrue(ToolProvider.systemJavaCompiler != null, 'jdk.compiler is required')
    }
}

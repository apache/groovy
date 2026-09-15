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
package org.apache.groovy.lsp.internal

import org.apache.groovy.lsp.GroovyLanguageServer
import org.apache.groovy.lsp.GroovyLanguageServerLauncher
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.compile.Identifiers
import org.apache.groovy.lsp.internal.compile.ImportSupport
import org.apache.groovy.lsp.internal.compile.PackageGuess
import org.apache.groovy.lsp.internal.compile.TypeIndex
import org.apache.groovy.lsp.internal.compile.WorkspaceScanner
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.position.Positions
import org.apache.groovy.lsp.internal.protocol.ClientFeatures
import org.apache.groovy.lsp.internal.util.GroovyKeywords
import org.apache.groovy.lsp.internal.util.Uris
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.apache.groovy.lsp.spi.GroovyLspExtension
import org.apache.groovy.lsp.spi.GroovyLspSession
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.DeclarationCapabilities
import org.eclipse.lsp4j.DefinitionCapabilities
import org.eclipse.lsp4j.DocumentSymbolCapabilities
import org.eclipse.lsp4j.GeneralClientCapabilities
import org.eclipse.lsp4j.ImplementationCapabilities
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentClientCapabilities
import org.eclipse.lsp4j.TypeDefinitionCapabilities
import org.eclipse.lsp4j.WindowClientCapabilities
import org.eclipse.lsp4j.WorkspaceClientCapabilities
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.WorkspaceFolder
import org.junit.jupiter.api.Test

import java.nio.file.Files

final class HelperCoverageTest {

    @Test
    void clientFeaturesReadOptionalFlags() {
        assert !ClientFeatures.definitionLinks(null)
        assert !ClientFeatures.typeDefinitionLinks(null)
        assert !ClientFeatures.implementationLinks(null)
        assert !ClientFeatures.declarationLinks(null)
        assert !ClientFeatures.hierarchicalDocumentSymbols(null)
        assert !ClientFeatures.applyEdit(null)
        assert !ClientFeatures.workDoneProgress(null)
        def caps = new ClientCapabilities()
        assert !ClientFeatures.definitionLinks(caps)
        def text = new TextDocumentClientCapabilities()
        text.definition = new DefinitionCapabilities(false, true)
        text.typeDefinition = new TypeDefinitionCapabilities(false, true)
        text.implementation = new ImplementationCapabilities(false, true)
        text.declaration = new DeclarationCapabilities(false, true)
        def symbols = new DocumentSymbolCapabilities()
        symbols.hierarchicalDocumentSymbolSupport = true
        text.documentSymbol = symbols
        caps.textDocument = text
        def workspace = new WorkspaceClientCapabilities()
        workspace.applyEdit = true
        caps.workspace = workspace
        def window = new WindowClientCapabilities()
        window.workDoneProgress = true
        caps.window = window
        assert ClientFeatures.workDoneProgress(caps)
        assert ClientFeatures.definitionLinks(caps)
        assert ClientFeatures.typeDefinitionLinks(caps)
        assert ClientFeatures.implementationLinks(caps)
        assert ClientFeatures.declarationLinks(caps)
        assert ClientFeatures.hierarchicalDocumentSymbols(caps)
        assert ClientFeatures.applyEdit(caps)
        assert ClientFeatures.workDoneProgress(caps)
    }

    @Test
    void identifiersCountAndNameOf() {
        assert Identifiers.count(null, 'x') == 0
        assert Identifiers.count('foo', null) == 0
        assert Identifiers.count('foo', '') == 0
        assert Identifiers.containsWord('foo bar foo', 'foo')
        assert !Identifiers.containsWord('foobar', 'foo')
        assert Identifiers.count('foo foo', 'foo') == 2
        assert Identifiers.nameOf(null) == null
        assert Identifiers.nameOf(new ASTNode()) == null
        assert Identifiers.nameOf(new VariableExpression('x')) == 'x'
        assert Identifiers.nameOf(new Parameter(ClassHelper.STRING_TYPE, 'p')) == 'p'
        def method = new MethodNode('m', 0, ClassHelper.VOID_TYPE, new Parameter[0], ClassNode.EMPTY_ARRAY, null)
        assert Identifiers.nameOf(method) == 'm'
        def field = new FieldNode('f', 0, ClassHelper.STRING_TYPE, ClassHelper.OBJECT_TYPE, null)
        assert Identifiers.nameOf(field) == 'f'
        def property = new PropertyNode('q', 0, ClassHelper.STRING_TYPE, ClassHelper.OBJECT_TYPE, null, null, null)
        assert Identifiers.nameOf(property) == 'q'
        assert Identifiers.nameOf(new ClassNode('demo.Hello', 0, ClassHelper.OBJECT_TYPE)) == 'Hello'
        def call = new MethodCallExpression(new VariableExpression('this'), 'foo', MethodCallExpression.NO_ARGUMENTS)
        assert Identifiers.nameOf(call) == 'foo'
        def stat = new StaticMethodCallExpression(ClassHelper.OBJECT_TYPE, 'bar', MethodCallExpression.NO_ARGUMENTS)
        assert Identifiers.nameOf(stat) == 'bar'
        def prop = new PropertyExpression(new VariableExpression('this'), 'name')
        assert Identifiers.nameOf(prop) == 'name'
        assert Identifiers.nameOf(new ClassExpression(ClassHelper.STRING_TYPE)) == 'String'
        assert Identifiers.nameOf(new ConstructorCallExpression(ClassHelper.OBJECT_TYPE, MethodCallExpression.NO_ARGUMENTS)) == 'Object'
    }

    @Test
    void packageGuessFromConventionalRoots() {
        def root = Files.createTempDirectory('pkg-guess')
        def src = root.resolve('src').resolve('main').resolve('groovy').resolve('demo')
        Files.createDirectories(src)
        def file = src.resolve('Hello.groovy')
        Files.writeString(file, 'class Hello {}')
        assert PackageGuess.fromUri(file.toUri(), [root.toUri()], []) == 'demo'
        assert PackageGuess.fromUri(null, [root.toUri()], []) == ''
        assert PackageGuess.fromUri(URI.create('untitled:1'), [root.toUri()], []) == ''
        assert PackageGuess.fromUri(file.toUri(), null, []) == ''
        assert PackageGuess.fromUri(root.toUri(), [root.toUri()], []) == ''
    }

    @Test
    void compilerSettingsAndTypeIndexEdges() {
        def missing = new CompilerSettings(['/no/such/groovy-lsp-cp'], null, 3, true, true)
        def loader = missing.createClassLoader(HelperCoverageTest.classLoader)
        assert loader != null
        loader.close()
        def on = new CompilerSettings(['.'], ['src'], 3, true, true)
        def config = on.toConfiguration()
        assert !config.disabledGlobalASTTransformations.contains('groovy.grape.GrabAnnotationTransformation')
        on.createClassLoader(HelperCoverageTest.classLoader).withCloseable { urls ->
            assert urls.URLs.length >= 1
        }
        assert TypeIndex.of(null) == TypeIndex.EMPTY
        assert TypeIndex.EMPTY.bySimpleName(null).isEmpty()
        assert TypeIndex.EMPTY.bySimpleName('X').isEmpty()
        assert TypeIndex.EMPTY.uniqueBySimpleName('X') == null
        assert TypeIndex.EMPTY.byName(null) == null
        assert TypeIndex.EMPTY.matchingPrefix(null, 1).isEmpty()
        def compiler = new GroovyCompiler()
        def uri = URI.create('file:///tmp/Idx.groovy')
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, 'interface I {}\nenum E { A }\nclass Idx {}\n')],
                [], CompilerSettings.defaults(), HelperCoverageTest.classLoader)
        def index = snapshot.types()
        assert index.byName('Idx') != null
        assert index.uniqueBySimpleName('Idx') != null
        assert index.matchingPrefix('id', 10).any { it.simpleName == 'Idx' }
        assert snapshot.get(uri.toString()).module != null
        compiler.close()
    }

    @Test
    void workspaceScannerSkipsGitAndCollectsGvy() {
        def root = Files.createTempDirectory('scan-gvy')
        Files.writeString(root.resolve('A.gvy'), 'class A {}')
        Files.createDirectories(root.resolve('.git'))
        Files.writeString(root.resolve('.git').resolve('B.groovy'), 'class B {}')
        Files.createDirectories(root.resolve('out'))
        Files.writeString(root.resolve('out').resolve('C.groovy'), 'class C {}')
        def files = new WorkspaceScanner().scan([root.toUri(), URI.create('https://example.test/')], [])
        assert files.any { it.fileName.toString() == 'A.gvy' }
        assert files.every { !it.toString().contains("${File.separator}.git${File.separator}") }
        assert files.every { !it.toString().contains("${File.separator}out${File.separator}") }
        assert new WorkspaceScanner().scan(null, null).isEmpty()
    }

    @Test
    void importSupportNullsAndPackageName() {
        assert ImportSupport.allImports(null).isEmpty()
        assert ImportSupport.packageName(null) == ''
        assert ImportSupport.packageOf(null) == ''
        assert ImportSupport.packageOf('Foo') == ''
        assert !ImportSupport.needsImport(null, 'java.lang.String')
        assert ImportSupport.addImport(null, 'x.Y', PositionEncoding.UTF16).isEmpty()
        assert ImportSupport.unused(null).isEmpty()
        assert ImportSupport.unusedDiagnostics(null, PositionEncoding.UTF16).isEmpty()
        assert ImportSupport.removeUnused(null, PositionEncoding.UTF16).isEmpty()
    }

    @Test
    void extensionHostIsolatesThrowingPlugins() {
        def boom = new GroovyLspExtension() {
            void configure(CompilerConfiguration configuration) { throw new IllegalStateException('c') }
            void afterCompile(unit) { throw new IllegalStateException('a') }
            List extraDiagnostics(URI uri, String text) { throw new IllegalStateException('d') }
            List extraCodeActions(URI uri, List diagnostics, Range range) { throw new IllegalStateException('x') }
            List<String> commands() { ['boom'] }
            Object executeCommand(String command, List arguments, GroovyLspSession session) {
                throw new IllegalStateException('e')
            }
        }
        def host = ExtensionHost.of(null, boom)
        host.configure(new CompilerConfiguration())
        host.afterCompile(null)
        assert host.extraDiagnostics(null).isEmpty()
        def doc = new CompiledDocument(
                URI.create('file:///tmp/X.groovy'), 1, 'class X {}', null, null, null)
        assert host.extraDiagnostics(doc).isEmpty()
        def td = new TextDocument(URI.create('file:///tmp/X.groovy'), 'groovy', 1, 'class X {}')
        assert host.extraCodeActions(td, [],
                new Range(new Position(0, 0), new Position(0, 1))).isEmpty()
        assert host.executeCommand('boom', [], new GroovyLspSession() {
            void applyEdit(WorkspaceEdit edit) {}
        }) == null
        assert host.executeCommand(null, [], null) == null
        assert ExtensionHost.of((GroovyLspExtension[]) null) == ExtensionHost.none()
        assert 'groovy.lsp.stc' in ExtensionHost.discover(null).ids()
        assert host == ExtensionHost.of(boom)
        assert host.hashCode() == ExtensionHost.of(boom).hashCode()
        assert host.all().size() == 1
        def defaults = new GroovyLspExtension() {}
        assert defaults.id() == defaults.class.name
        defaults.configure(new CompilerConfiguration())
        defaults.afterCompile(null)
        assert defaults.extraDiagnostics(URI.create('file:///tmp/X.groovy'), '').isEmpty()
        assert defaults.extraDiagnostics(URI.create('file:///tmp/X.groovy'), '', null).isEmpty()
        assert defaults.extraCodeActions(URI.create('file:///tmp/X.groovy'), [], null).isEmpty()
        assert defaults.extraCodeActions(URI.create('file:///tmp/X.groovy'), 'class X {}', [], null).isEmpty()
        assert defaults.commands().isEmpty()
        assert defaults.executeCommand('x', [], null) == null
        defaults.initialized(null)
        defaults.shutdown()
        def session = new GroovyLspSession() {
            void applyEdit(WorkspaceEdit edit) {}
        }
        assert session.documentText(URI.create('file:///tmp/X.groovy')) == null
        assert session.workspaceFolders().isEmpty()
        assert session.classpath().isEmpty()
        assert session.sourcePaths().isEmpty()
        assert session.extraSettings().isEmpty()
        assert session.extraSetting('x') == null
        assert session.positionEncoding() == 'utf-16'
        def blank = new GroovyLspExtension() {
            String id() { '' }
            List extraCodeActions(URI uri, String sourceText, List diagnostics, Range range) { null }
        }
        assert blank.class.name in ExtensionHost.of(blank).ids()
        def blankDoc = new TextDocument(URI.create('file:///tmp/X.groovy'), 'groovy', 1, 'class X {}')
        assert ExtensionHost.of(blank).extraCodeActions(blankDoc, [],
                new Range(new Position(0, 0), new Position(0, 1))).isEmpty()
    }

    @Test
    void languageServerInitializeEncodingsAndExit() {
        def server = new GroovyLanguageServer()
        def params = new InitializeParams()
        params.rootUri = Files.createTempDirectory('lsp-root').toUri().toString()
        params.workspaceFolders = [new WorkspaceFolder(params.rootUri, 'root')]
        def caps = new ClientCapabilities()
        def general = new GeneralClientCapabilities()
        general.positionEncodings = ['utf-8', 'utf-16']
        caps.general = general
        params.capabilities = caps
        def result = server.initialize(params).get()
        assert result.capabilities.positionEncoding == 'utf-8'
        server.initialized(null)
        server.context.applyEdit(null)
        server.context.applyEdit(new WorkspaceEdit())
        assert server.context.diagnosticsFor(null).isEmpty()
        assert server.context.compiler != null
        assert server.context.scanner != null
        assert server.context.diagnostics != null
        assert server.context.completions != null
        assert server.context.hovers != null
        assert server.context.navigation != null
        assert server.context.symbols != null
        assert server.context.rename != null
        server.context.snapshot = CompilationSnapshot.EMPTY
        assert server.context.initialized
        server.shutdown().get()
        assert server.context.recompile() != null
        server.exit()
        assert server.context.exitCode == 0
        def other = new GroovyLanguageServer()
        other.exit()
        assert other.context.exitCode == 1
        def rootOnly = new GroovyLanguageServer()
        def rootParams = new InitializeParams()
        rootParams.rootUri = params.rootUri
        rootParams.capabilities = new ClientCapabilities()
        rootOnly.initialize(rootParams).get()
        assert rootOnly.context.positionEncoding == PositionEncoding.UTF16
        rootOnly.shutdown().get()
    }

    @Test
    void launcherParseAndRunWithoutWaiting() {
        def parsed = GroovyLanguageServerLauncher.ListArgs.parse(null)
        assert !parsed.help && parsed.port == null
        def socket = GroovyLanguageServerLauncher.ListArgs.parse(['--stdio', '--socket', '0', '-v'] as String[])
        assert socket.port == 0
        assert socket.version
        def out = new ByteArrayOutputStream()
        def code = GroovyLanguageServerLauncher.run(new ByteArrayInputStream(new byte[0]), out, false)
        assert code == null || code == 0
    }

    @Test
    void positionEncodingsAndUrisRemaining() {
        assert PositionEncoding.fromProtocol(null) == null
        assert PositionEncoding.fromProtocol('nope') == null
        assert PositionEncoding.negotiate(['utf-32']).protocolName() == 'utf-32'
        assert PositionEncoding.negotiate(['nope']).protocolName() == 'utf-16'
        assert Positions.lineText('a\nb', 0) == ''
        assert Positions.lineText('a\nb', 9) == ''
        assert Positions.findIdentifier(null, 'x', PositionEncoding.UTF16) == null
        assert Uris.isGroovyDocument(URI.create('untitled:1'), null) == false
        assert Uris.fileName(URI.create('file:///tmp/Hello.groovy?raw=1')) == 'Hello.groovy'
        assert GroovyKeywords.isKeyword('def')
        assert !GroovyKeywords.isKeyword(null)
        assert !GroovyKeywords.isKeyword('notAKeyword')
    }

    @Test
    void privateUtilityConstructors() {
        [Identifiers, PackageGuess, ClientFeatures, GroovyKeywords, Uris,
         AstQuery, Positions].each { Class type ->
            def ctor = type.getDeclaredConstructor()
            ctor.accessible = true
            ctor.newInstance()
        }
        def keyType = Class.forName('org.apache.groovy.lsp.internal.compile.GroovyCompiler$LoaderKey')
        def ctor = keyType.getDeclaredConstructor(List, List, Map, boolean, boolean, ClassLoader)
        ctor.accessible = true
        def a = ctor.newInstance([], [], [:], false, false, HelperCoverageTest.classLoader)
        def b = ctor.newInstance([], [], [:], false, false, HelperCoverageTest.classLoader)
        assert a.hashCode() == b.hashCode()
        assert a == b
        def extra = ctor.newInstance([], ['x.jar'], [gdsl: 'p'], false, false, HelperCoverageTest.classLoader)
        assert a != extra
    }
}

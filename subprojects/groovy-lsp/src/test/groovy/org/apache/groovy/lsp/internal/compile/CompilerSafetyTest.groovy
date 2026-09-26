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

import com.sun.source.tree.CompilationUnitTree
import com.sun.source.util.JavacTask
import com.sun.source.util.Trees
import groovy.lang.GroovyClassLoader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.tools.JavaFileObject
import javax.tools.ToolProvider
import org.codehaus.groovy.control.CompilationUnit
import javax.tools.Diagnostic
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.io.TempDir

import org.apache.groovy.lsp.internal.diagnostic.DiagnosticConverter
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.apache.groovy.lsp.internal.LanguageServerContext
import org.apache.groovy.lsp.internal.util.Uris
import org.junit.jupiter.api.Test

import java.nio.file.Files

final class CompilerSafetyTest {
    private static final Object HOME = new Object()

    @TempDir
    Path folder


    @Test
    void astTestClosureDoesNotRunWhenDisabled() {
        System.clearProperty('groovy.asttest.enable')
        def compiler = new GroovyCompiler()
        def settings = CompilerSettings.defaults()
        def uri = URI.create('file:///tmp/AstTestSafety.groovy')
        def src = '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.control.CompilePhase
            @ASTTest(phase = CompilePhase.SEMANTIC_ANALYSIS, value = { throw new RuntimeException('asttest-executed') })
            class Victim {}
            '''
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, src)],
                [],
                settings,
                CompilerSafetyTest.classLoader)
        def compiled = snapshot.get(uri)
        assert compiled != null
        assert System.getProperty('groovy.asttest.enable') == null
        assert compiled.errors.every { !it.toString().contains('asttest-executed') }
    }

    @Test
    void diagnosticsAreScopedToOwningFile() {
        def config = new CompilerConfiguration()
        def a = new SourceUnit('file:///tmp/A.groovy', 'class A {', config, null, null)
        def b = new SourceUnit('file:///tmp/B.groovy', 'class B {}', config, null, null)
        def collector = new ErrorCollector(config)
        collector.addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException('bad A', 1, 1), a))
        def docA = new CompiledDocument(URI.create('file:///tmp/A.groovy'), 1, 'class A {', null, a, collector)
        def docB = new CompiledDocument(URI.create('file:///tmp/B.groovy'), 1, 'class B {}', null, b, collector)
        def converter = new DiagnosticConverter()
        assert converter.convert(docA, PositionEncoding.UTF16).size() == 1
        assert converter.convert(docB, PositionEncoding.UTF16).isEmpty()
    }

    @Test
    void compilerReusesThenClosesClassLoader() {
        def compiler = new GroovyCompiler()
        def settings = CompilerSettings.defaults()
        def uri = URI.create('file:///tmp/Loader.groovy')
        def src = 'class Loader {}\n'
        compiler.compile([new TextDocument(uri, 'groovy', 1, src)], [], settings, CompilerSafetyTest.classLoader)
        compiler.compile([new TextDocument(uri, 'groovy', 2, src)], [], settings, CompilerSafetyTest.classLoader)
        compiler.close()
        def again = compiler.compile([new TextDocument(uri, 'groovy', 3, src)], [], settings, CompilerSafetyTest.classLoader)
        assert again.get(uri).module != null
        compiler.close()
    }

    @Test
    void compilerReadsExtraFilesAndRestoresProcessFlags() {
        def dir = Files.createTempDirectory('groovy-lsp-extra')
        def extra = dir.resolve('Extra.groovy')
        Files.writeString(extra, 'class Extra {}\n')
        def note = dir.resolve('readme.txt')
        Files.writeString(note, 'not groovy')
        def nested = dir.resolve('nested')
        Files.createDirectory(nested)
        System.setProperty('groovy.asttest.enable', 'true')
        System.setProperty('groovy.grape.enable', 'true')
        def compiler = new GroovyCompiler()
        def snapshot
        try {
            snapshot = compiler.compile(
                    [new TextDocument(URI.create('file:///tmp/Open.groovy'), 'groovy', 1, 'class Open {}\n')],
                    [null, nested, note, extra, extra],
                    CompilerSettings.defaults(),
                    CompilerSafetyTest.classLoader)
        } finally {
            compiler.close()
        }
        assert System.getProperty('groovy.asttest.enable') == 'true'
        assert System.getProperty('groovy.grape.enable') == 'true'
        System.clearProperty('groovy.asttest.enable')
        System.clearProperty('groovy.grape.enable')
        assert snapshot.get(Uris.normalize(extra.toUri())).module != null
        assert snapshot.get(Uris.normalize(extra.toUri())).module.classes.any { it.nameWithoutPackage == 'Extra' }
        def missing = Files.createTempDirectory('groovy-lsp-fp').resolve('gone.groovy')
        def live = Files.createTempFile('groovy-lsp-fp', '.groovy')
        Files.writeString(live, 'class Live {}')
        def fingerprint = LanguageServerContext.fingerprint([], [live, missing], CompilerSettings.defaults())
        assert fingerprint.contains(live.fileName.toString())
        assert fingerprint.contains(missing.fileName.toString())
    }
    @Test
    void compilerNullInputsUnreadableFilesAndBadSourceNames() {
        def compiler = new GroovyCompiler()
        try {
            def empty = compiler.compile(null, null, CompilerSettings.defaults(), CompilerSafetyTest.classLoader)
            assert empty.documents().isEmpty()

            if (posix()) {
                def groovy = folder.resolve('Hidden.groovy')
                def java = folder.resolve('Hidden.java')
                Files.writeString(groovy, 'class Hidden {}\n')
                Files.writeString(java, 'public class HiddenJava {}\n')
                groovy.toFile().setReadable(false, false)
                java.toFile().setReadable(false, false)
                try {
                    def snapshot = compiler.compile([], [groovy, java], CompilerSettings.defaults(), CompilerSafetyTest.classLoader)
                    assert snapshot.get(groovy.toUri()) == null
                    assert snapshot.get(java.toUri()) == null
                } finally {
                    groovy.toFile().setReadable(true, false)
                    java.toFile().setReadable(true, false)
                }
            }
        } finally {
            compiler.close()
        }

        def addCompiled = GroovyCompiler.getDeclaredMethod('addCompiled', Map, Map, Map, SourceUnit)
        addCompiled.accessible = true
        def config = new CompilerConfiguration()
        def unit = new SourceUnit('not a uri', 'class A {}', config, null, new ErrorCollector(config))
        def compiled = new LinkedHashMap()
        addCompiled.invoke(null, compiled, [:], [:], unit)
        assert compiled.isEmpty()
    }

    @Test
    void compilerJointOutputFailureCloseAndLoaderKey() {
        def homeFile = folder.resolve('not-a-directory')
        Files.writeString(homeFile, 'x')
        def java = folder.resolve('Plain.java')
        Files.writeString(java, 'public class Plain {}\n')
        withHome(homeFile) {
            def compiler = new GroovyCompiler()
            try {
                def snapshot = compiler.compile(
                        [new TextDocument(folder.resolve('G.groovy').toUri(), 'groovy', 1, 'class G {}')],
                        [java], CompilerSettings.defaults(), CompilerSafetyTest.classLoader)
                assert snapshot.get(folder.resolve('G.groovy').toUri()) != null
            } finally {
                compiler.close()
            }
        }

        def throwing = new GroovyCompiler()
        try {
            def groovyField = GroovyCompiler.getDeclaredField('groovyLoader')
            def classpathField = GroovyCompiler.getDeclaredField('classpathLoader')
            groovyField.accessible = true
            classpathField.accessible = true
            groovyField.set(throwing, new GroovyClassLoader(CompilerSafetyTest.classLoader) {
                @Override
                void close() throws IOException {
                    throw new IOException('groovy loader')
                }
            })
            classpathField.set(throwing, new URLClassLoader(new URL[0], CompilerSafetyTest.classLoader) {
                @Override
                void close() throws IOException {
                    throw new IOException('url loader')
                }
            })
        } finally {
            throwing.close()
        }

        def keyType = Class.forName('org.apache.groovy.lsp.internal.compile.GroovyCompiler$LoaderKey')
        def ctor = keyType.getDeclaredConstructor(List, List, Map, boolean, boolean, ClassLoader)
        ctor.accessible = true
        def key = ctor.newInstance([], [], [:], false, false, CompilerSafetyTest.classLoader)
        assert key.equals(key)
        assert !key.equals('nope')
        assert !key.equals(null)
    }

    @Test
    void symlinkJavaDiagnosticStillPublishes() {
        assumeJavac()
        def real = folder.resolve('real')
        Files.createDirectories(real)
        def target = real.resolve('Widget.java')
        Files.writeString(target, 'public class Widget { int x = ; }\n')
        def link = folder.resolve('Widget.java')
        Files.createSymbolicLink(link, target)
        def compiler = new GroovyCompiler()
        try {
            def snapshot = compiler.compile(
                    [new TextDocument(folder.resolve('Use.groovy').toUri(), 'groovy', 1, 'class Use {}')],
                    [link], CompilerSettings.defaults(), CompilerSafetyTest.classLoader)
            assert snapshot.documents().any { it.javaMessages }
        } finally {
            compiler.close()
        }
    }

    @Test
    void javaSymbolIndexPackageAnonymousAndStubUnits() {
        assumeJavac()
        def java = folder.resolve('Host.java')
        Files.writeString(java, '''\
            package demo;
            public class Host {
                public void run() {
                    Runnable r = new Runnable() {
                        public void run() {}
                    };
                }
            }
            '''.stripIndent())
        def compiler = new GroovyCompiler()
        try {
            def snapshot = compiler.compile(
                    [new TextDocument(folder.resolve('Call.groovy').toUri(), 'groovy', 1, 'class Call {}')],
                    [java], CompilerSettings.defaults(), CompilerSafetyTest.classLoader)
            assert snapshot.javaSymbols().type('demo.Host') != null
        } finally {
            compiler.close()
        }

        def javac = ToolProvider.systemJavaCompiler
        javac.getStandardFileManager(null, null, null).withCloseable { manager ->
            def task = javac.getTask(null, manager, null, null, null, List.of()) as JavacTask
            def trees = Trees.instance(task)
            def missing = Proxy.newProxyInstance(CompilationUnitTree.classLoader, [CompilationUnitTree] as Class[],
                    { proxy, method, args -> method.name == 'getSourceFile' ? null : null } as InvocationHandler)
            assert JavaSymbolIndex.of(trees, [missing as CompilationUnitTree]).types().isEmpty()
            def file = Proxy.newProxyInstance(JavaFileObject.classLoader, [JavaFileObject] as Class[],
                    { proxy, method, args -> method.name == 'toUri' ? null : null } as InvocationHandler) as JavaFileObject
            def noUri = Proxy.newProxyInstance(CompilationUnitTree.classLoader, [CompilationUnitTree] as Class[],
                    { proxy, method, args -> method.name == 'getSourceFile' ? file : null } as InvocationHandler)
            assert JavaSymbolIndex.of(trees, [noUri as CompilationUnitTree]).types().isEmpty()
        }

        def anon = folder.resolve('Anon.java')
        Files.writeString(anon, '''\
            package demo;
            public class Anon {
                public void run() {
                    new Runnable() { public void run() {} };
                }
            }
            '''.stripIndent())
        ToolProvider.systemJavaCompiler.getStandardFileManager(null, null, null).withCloseable { manager ->
            def files = manager.getJavaFileObjects(anon.toFile())
            def task = ToolProvider.systemJavaCompiler.getTask(null, manager, null, ['--release', '17'], null, files) as JavacTask
            def units = []
            task.parse().each { units << it }
            def indexed = JavaSymbolIndex.of(Trees.instance(task), units)
            assert indexed.type('demo.Anon') != null
            def other = ToolProvider.systemJavaCompiler.getTask(null, manager, null, ['--release', '17'], null, List.of()) as JavacTask
            def shifted = JavaSymbolIndex.of(Trees.instance(other), units)
            assert shifted.types()
        }
    }

    @Test
    void lspJavaCompilerNullFilesNotesAndUnboundedPositions() {
        assumeJavac()
        def compiler = new LspJavaCompiler(Map.of())
        compiler.compile(null, new CompilationUnit())
        assert compiler.messages().isEmpty()

        def diagnosticUri = LspJavaCompiler.getDeclaredMethod('diagnosticUri', Diagnostic)
        diagnosticUri.accessible = true
        assert diagnosticUri.invoke(null, javacDiagnostic(Diagnostic.Kind.NOTE, null)) == null
        assert diagnosticUri.invoke(null, javacDiagnostic(Diagnostic.Kind.ERROR, null)) == null

        def bounded = LspJavaCompiler.getDeclaredMethod('bounded', long)
        bounded.accessible = true
        assert bounded.invoke(null, -1L) == 1
        assert bounded.invoke(null, 0L) == 1
        assert bounded.invoke(null, Integer.MAX_VALUE + 1L) == 1
        assert bounded.invoke(null, 4L) == 4
    }

    private CompiledDocument compile(String text) {
        def compiler = new GroovyCompiler()
        try {
            def uri = folder.resolve("Cov${System.nanoTime()}.groovy").toUri()
            return compiler.compile([new TextDocument(uri, 'groovy', 1, text)], [],
                    CompilerSettings.defaults(), CompilerSafetyTest.classLoader).get(uri)
        } finally {
            compiler.close()
        }
    }

    private static void withHome(Path home, Closure work) {
        synchronized (HOME) {
            def previous = System.getProperty('user.home')
            System.setProperty('user.home', home.toString())
            try {
                work.call()
            } finally {
                System.setProperty('user.home', previous)
            }
        }
    }

    private static boolean posix() {
        FileSystems.default.supportedFileAttributeViews().contains('posix')
    }

    private static void assumeJavac() {
        Assumptions.assumeTrue(ToolProvider.systemJavaCompiler != null, 'jdk.compiler is required')
    }

    private static Diagnostic javacDiagnostic(Diagnostic.Kind kind, JavaFileObject source) {
        return (Diagnostic) Proxy.newProxyInstance(Diagnostic.classLoader,
                [Diagnostic] as Class[], { proxy, method, args ->
            switch (method.name) {
                case 'getKind': return kind
                case 'getSource': return source
                case 'getLineNumber': return -1L
                case 'getColumnNumber': return -1L
                case 'getPosition': return -1L
                case 'getStartPosition': return -1L
                case 'getEndPosition': return -1L
                case 'getCode': return null
                case 'getMessage': return 'note'
                default: return null
            }
        } as InvocationHandler)
    }

}

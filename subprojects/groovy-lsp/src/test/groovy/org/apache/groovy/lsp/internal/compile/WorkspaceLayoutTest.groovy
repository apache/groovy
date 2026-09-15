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

import java.nio.file.FileSystems
import java.nio.file.Path
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.io.TempDir

import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.junit.jupiter.api.Test

import java.nio.file.Files

final class WorkspaceLayoutTest {

    @TempDir
    Path folder

    private static boolean posix() {
        FileSystems.default.supportedFileAttributeViews().contains('posix')
    }


    @Test
    void conventionalSourceRootsWinOverWorkspaceRoot() {
        def root = Files.createTempDirectory('groovy-lsp-layout')
        Files.createDirectories(root.resolve('src/main/groovy'))
        Files.writeString(root.resolve('src/main/groovy/A.groovy'), 'class A {}')
        Files.createDirectories(root.resolve('other'))
        Files.writeString(root.resolve('other/B.groovy'), 'class B {}')
        def roots = WorkspaceLayout.searchRoots(root, [])
        assert roots.size() == 1
        assert roots[0].endsWith('src/main/groovy')
    }

    @Test
    void emptyProjectFallsBackToFolder() {
        def root = Files.createTempDirectory('groovy-lsp-empty')
        def roots = WorkspaceLayout.searchRoots(root, [])
        assert roots == [root]
    }

    @Test
    void clientSourcePathsAreHonoured() {
        def root = Files.createTempDirectory('groovy-lsp-src')
        Files.createDirectories(root.resolve('custom'))
        def roots = WorkspaceLayout.searchRoots(root, ['custom'])
        assert roots[0].endsWith('custom')
    }

    @Test
    void classpathJarsFromLib() {
        def root = Files.createTempDirectory('groovy-lsp-lib')
        Files.createDirectories(root.resolve('lib'))
        Files.write(root.resolve('lib/dep.jar'), new byte[0])
        Files.write(root.resolve('lib/readme.txt'), new byte[0])
        def jars = WorkspaceLayout.classpathJars(root)
        assert jars.size() == 1
        assert jars[0].endsWith('dep.jar')
    }

    @Test
    void withInferredFillsEmptySettings() {
        def root = Files.createTempDirectory('groovy-lsp-infer')
        Files.createDirectories(root.resolve('src/main/groovy'))
        Files.createDirectories(root.resolve('lib'))
        Files.write(root.resolve('lib/x.jar'), new byte[0])
        def inferred = WorkspaceLayout.withInferred(CompilerSettings.defaults(), [root.toUri()])
        assert inferred.sourcePaths.contains('src/main/groovy')
        assert inferred.sourcePaths.every { !it.contains('\\') }
        assert inferred.classpath.any { it.endsWith('x.jar') }
    }

    @Test
    void importSupportTreatsGroovyDefaultsAsPresent() {
        assert !ImportSupport.needsImport(null, 'java.lang.String')
        assert !ImportSupport.needsImport(null, 'java.util.List')
        assert !ImportSupport.needsImport(null, 'groovy.lang.Closure')
        assert ImportSupport.needsImport(null, 'com.example.Widget')
        assert ImportSupport.packageOf('com.example.Widget') == 'com.example'
        assert ImportSupport.packageName(null) == ''
        assert ImportSupport.allImports(null).isEmpty()
        assert ImportSupport.addImport(null, 'com.example.Widget', null).isEmpty()
        assert ImportSupport.needsImport(null, null) == false
        assert ImportSupport.needsImport(null, 'Widget') == false
        assert ImportSupport.packageOf(null) == ''
    }

    @Test
    void searchRootsRejectsNullAndMissing() {
        assert WorkspaceLayout.searchRoots(null, []).isEmpty()
        assert WorkspaceLayout.classpathJars(null).isEmpty()
        def inferred = WorkspaceLayout.withInferred(null, null)
        assert inferred.classpath.isEmpty()
    }

    @Test
    void typeIndexIndexesCompiledClasses() {
        def compiler = new GroovyCompiler()
        def settings = CompilerSettings.defaults()
        def a = new TextDocument(
                URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'package demo\nclass Alpha {}\n')
        def b = new TextDocument(
                URI.create('file:///tmp/B.groovy'), 'groovy', 1, 'package other\nclass Alpha {}\n')
        def snapshot = compiler.compile([a, b], [], settings, WorkspaceLayoutTest.classLoader)
        def index = snapshot.types()
        assert index.byName('demo.Alpha') != null
        assert index.uniqueBySimpleName('Alpha') == null
        assert index.bySimpleName('Alpha').size() == 2
        assert index.matchingPrefix('Al', 10).size() >= 2
        assert TypeIndex.of(null) === TypeIndex.EMPTY
        assert snapshot.types().is(index)
        compiler.close()
    }

    @Test
    void starImportSatisfiesNeedsImport() {
        def compiler = new GroovyCompiler()
        def uri = URI.create('file:///tmp/Star.groovy')
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1,
                        'package demo\nimport com.example.*\nclass Holder {}\n')],
                [], CompilerSettings.defaults(), WorkspaceLayoutTest.classLoader)
        def module = snapshot.get(uri).module
        assert !ImportSupport.needsImport(module, 'com.example.Foo')
        assert ImportSupport.needsImport(module, 'org.other.Foo')
        assert !ImportSupport.needsImport(module, 'java.time.Instant')
        assert !ImportSupport.needsImport(module, 'java.math.BigDecimal')
        assert ImportSupport.packageName(module) == 'demo'
        def edits = ImportSupport.addImport(snapshot.get(uri), 'org.other.Foo',
                PositionEncoding.UTF16)
        assert edits.size() == 1
        assert edits[0].newText.contains('import org.other.Foo')
        compiler.close()
    }
    @Test
    void snapshotTypeIndexPackageGuessAndWorkspaceEdges() {
        def oneArg = new CompilationSnapshot(Map.of())
        assert oneArg.javaSymbols() == JavaSymbolIndex.EMPTY
        assert oneArg.types() != null
        assert oneArg.types().is(oneArg.types())

        def script = new ClassNode('Scripty', 0, ClassHelper.OBJECT_TYPE)
        script.script = true
        script.lineNumber = 0
        def alpha = new ClassNode('Alpha', 0, ClassHelper.OBJECT_TYPE)
        alpha.lineNumber = 1
        def alpine = new ClassNode('Alpine', 0, ClassHelper.OBJECT_TYPE)
        alpine.lineNumber = 1
        def module = new ModuleNode((SourceUnit) null)
        module.addClass(script)
        module.addClass(alpha)
        module.addClass(alpine)
        def uri = URI.create('file:///tmp/coverage-types.groovy')
        def typed = new CompilationSnapshot([(uri): new CompiledDocument(uri, 1, 'class Alpha {}', module, null, null)])
        def types = TypeIndex.of(typed)
        assert types.byName('Scripty') == null
        assert types.matchingPrefix('Al', 1).size() == 1

        assert PackageGuess.fromUri(URI.create('file:///'), [URI.create('file:///')], []) == ''
        assert PackageGuess.fromUri(URI.create('file:///tmp/%00bad.groovy'), [URI.create('file:///tmp')], []) == ''
        def loose = folder.resolve('Loose.groovy')
        Files.writeString(loose, 'class Loose {}')
        assert PackageGuess.fromUri(loose.toUri(), [URI.create('https://example.test/')], []) == ''
        assert PackageGuess.fromUri(loose.toUri(), [folder.toUri()], []) == ''

        if (posix()) {
            def lib = folder.resolve('lib')
            Files.createDirectories(lib)
            assert lib.toFile().setReadable(false, false)
            assert lib.toFile().setExecutable(false, false)
            try {
                assert WorkspaceLayout.classpathJars(folder).isEmpty()
            } finally {
                lib.toFile().setReadable(true, false)
                lib.toFile().setExecutable(true, false)
            }
        }
        def inferred = WorkspaceLayout.withInferred(CompilerSettings.defaults(),
                [null, URI.create('https://example.test/root')])
        assert inferred.classpath.isEmpty()

        if (posix()) {
            def blocked = folder.resolve('blocked')
            Files.createDirectories(blocked)
            assert blocked.toFile().setReadable(false, false)
            assert blocked.toFile().setExecutable(false, false)
            try {
                assert new WorkspaceScanner().scan([blocked.toUri()], []).isEmpty()
            } finally {
                blocked.toFile().setReadable(true, false)
                blocked.toFile().setExecutable(true, false)
            }
        }

        def first = folder.resolve('cap-a')
        def second = folder.resolve('cap-b')
        Files.createDirectories(first)
        Files.createDirectories(second)
        for (int i = 0; i < 4000; i++) {
            Files.createFile(first.resolve("F${i}.groovy"))
        }
        Files.writeString(second.resolve('Z.groovy'), 'class Z {}')
        def found = new WorkspaceScanner().scan([first.toUri(), second.toUri()], [])
        assert found.size() == 4000
        assert found.every { it.fileName.toString() != 'Z.groovy' }
    }

}

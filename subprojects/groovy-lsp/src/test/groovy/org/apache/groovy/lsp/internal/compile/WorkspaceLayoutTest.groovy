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
import java.util.LinkedHashMap
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
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
    void documentsKeepBuilderOrder() {
        def first = URI.create('file:///tmp/A.groovy')
        def second = URI.create('file:///tmp/B.groovy')
        def third = URI.create('file:///tmp/C.groovy')
        def map = new LinkedHashMap<URI, CompiledDocument>()
        map.put(first, new CompiledDocument(first, 1, 'class A {}', null, null, null))
        map.put(second, new CompiledDocument(second, 1, 'class B {}', null, null, null))
        map.put(third, new CompiledDocument(third, 1, 'class C {}', null, null, null))
        def snapshot = new CompilationSnapshot(map)
        assert snapshot.documents()*.uri == [first, second, third]
        def extra = URI.create('file:///tmp/D.groovy')
        map.put(extra, new CompiledDocument(extra, 1, 'class D {}', null, null, null))
        assert snapshot.documents()*.uri == [first, second, third]
    }

    @Test
    void prefixMatchesFollowDocumentOrder() {
        def map = new LinkedHashMap<URI, CompiledDocument>()
        def names = ['Alpha', 'Beta', 'Alpine']
        names.each { name ->
            def uri = URI.create("file:///tmp/${name}.groovy")
            def type = new ClassNode(name, 0, ClassHelper.OBJECT_TYPE)
            type.lineNumber = 1
            def module = new ModuleNode((SourceUnit) null)
            module.addClass(type)
            map.put(uri, new CompiledDocument(uri, 1, "class ${name} {}", module, null, null))
        }
        def types = new CompilationSnapshot(map).types()
        assert types.matchingPrefix('Al', 1)*.simpleName == ['Alpha']
        assert types.matchingPrefix('', 2)*.simpleName == ['Alpha', 'Beta']
    }

    @Test
    void withInferredFillsEmptySettings() {
        def root = Files.createTempDirectory('groovy-lsp-infer')
        Files.createDirectories(root.resolve('src/main/groovy'))
        Files.createDirectories(root.resolve('lib'))
        Files.write(root.resolve('lib/x.jar'), new byte[0])
        def inferred = WorkspaceLayout.withInferred(CompilerSettings.defaults(), [root.toUri()])
        assert inferred.sourcePaths.any { unix(it).endsWith('/src/main/groovy') }
        assert inferred.sourcePaths.every { !it.contains('\\') }
        assert inferred.classpath.any { it.endsWith('x.jar') }
    }

    @Test
    void withInferredAggregatesEveryFolderWhenSettingsAreEmpty() {
        def one = folder.resolve('one')
        def two = folder.resolve('two')
        Files.createDirectories(one.resolve('src/main/groovy'))
        Files.createDirectories(one.resolve('lib'))
        Files.write(one.resolve('lib/a.jar'), new byte[0])
        Files.createDirectories(two.resolve('src'))
        Files.createDirectories(two.resolve('libs'))
        Files.write(two.resolve('libs/b.jar'), new byte[0])
        Files.writeString(one.resolve('src/main/groovy/A.groovy'), 'class A {}')
        Files.writeString(two.resolve('src/B.groovy'), 'class B {}')
        def folders = [one.toUri(), two.toUri(), one.toUri()]
        def inferred = WorkspaceLayout.withInferred(CompilerSettings.defaults(), folders)
        assert inferred.sourcePaths.any { unix(it).endsWith('/src/main/groovy') }
        assert inferred.sourcePaths.any { unix(it).endsWith('/src') && !unix(it).endsWith('/src/main/groovy') }
        assert inferred.classpath.count { it.endsWith("${File.separator}a.jar") || it.endsWith('/a.jar') } == 1
        assert inferred.classpath.any { it.endsWith('b.jar') }
        def found = new WorkspaceScanner().scan([one.toUri(), two.toUri()], inferred.sourcePaths)
                *.fileName*.toString() as Set
        assert found.contains('A.groovy')
        assert found.contains('B.groovy')
    }

    @Test
    void withInferredKeepsExplicitListsAcrossFolders() {
        def one = folder.resolve('keep-one')
        def two = folder.resolve('keep-two')
        Files.createDirectories(one.resolve('src/main/groovy'))
        Files.createDirectories(one.resolve('lib'))
        Files.write(one.resolve('lib/a.jar'), new byte[0])
        Files.createDirectories(two.resolve('libs'))
        Files.write(two.resolve('libs/b.jar'), new byte[0])
        def settings = new CompilerSettings(['/keep.jar'], ['custom'], 3, false, false)
        def inferred = WorkspaceLayout.withInferred(settings, [one.toUri(), two.toUri()])
        assert inferred.classpath == ['/keep.jar']
        assert inferred.sourcePaths == ['custom']
    }

    @Test
    void withInferredDedupesSharedSourceRootsAndInfersJarsOnly() {
        def one = folder.resolve('dup-one')
        def two = folder.resolve('dup-two')
        Files.createDirectories(one.resolve('src/main/groovy'))
        Files.createDirectories(two.resolve('src/main/groovy'))
        Files.createDirectories(one.resolve('lib'))
        Files.createDirectories(two.resolve('libs'))
        Files.write(one.resolve('lib/a.jar'), new byte[0])
        Files.write(two.resolve('libs/b.jar'), new byte[0])
        def sourcesOnly = WorkspaceLayout.withInferred(CompilerSettings.defaults(), [one.toUri(), two.toUri()])
        assert sourcesOnly.sourcePaths.findAll { unix(it).endsWith('/src/main/groovy') }.size() == 2
        assert sourcesOnly.classpath.any { it.endsWith('a.jar') }
        assert sourcesOnly.classpath.any { it.endsWith('b.jar') }

        def jarsOnly = WorkspaceLayout.withInferred(
                new CompilerSettings([], ['custom'], 3, false, false), [one.toUri(), two.toUri()])
        assert jarsOnly.sourcePaths == ['custom']
        assert jarsOnly.classpath.any { it.endsWith('a.jar') }
        assert jarsOnly.classpath.any { it.endsWith('b.jar') }
    }

    @Test
    void withInferredMixesExplicitClasspathWithEverySourceFolder() {
        def one = folder.resolve('mix-one')
        def two = folder.resolve('mix-two')
        Files.createDirectories(one.resolve('src/main/groovy'))
        Files.createDirectories(two.resolve('src/test/groovy'))
        Files.createDirectories(one.resolve('lib'))
        Files.write(one.resolve('lib/skip.jar'), new byte[0])
        def settings = new CompilerSettings(['/keep.jar'], [], 3, false, false)
        def inferred = WorkspaceLayout.withInferred(settings, [one.toUri(), two.toUri()])
        assert inferred.classpath == ['/keep.jar']
        assert inferred.sourcePaths.any { unix(it).endsWith('/src/main/groovy') }
        assert inferred.sourcePaths.any { unix(it).endsWith('/src/test/groovy') }
    }

    @Test
    void inferredRootsStayInsideTheFolderThatDeclaredThem() {
        def gradle = folder.resolve('gradle')
        def flat = folder.resolve('flat')
        Files.createDirectories(gradle.resolve('src/main/groovy'))
        Files.createDirectories(gradle.resolve('src/scratch'))
        Files.writeString(gradle.resolve('src/main/groovy/A.groovy'), 'class A {}')
        Files.writeString(gradle.resolve('src/scratch/Skip.groovy'), 'class Skip {}')
        Files.createDirectories(flat.resolve('src'))
        Files.writeString(flat.resolve('src/B.groovy'), 'class B {}')
        def inferred = WorkspaceLayout.withInferred(CompilerSettings.defaults(), [gradle.toUri(), flat.toUri()])
        def found = new WorkspaceScanner().scan([gradle.toUri(), flat.toUri()], inferred.sourcePaths)
                *.fileName*.toString() as Set
        assert found.contains('A.groovy')
        assert found.contains('B.groovy')
        assert !found.contains('Skip.groovy')
    }

    private static String unix(String path) {
        path.replace('\\', '/')
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
        assert types.matchingPrefix('Al', 1)*.simpleName == ['Alpha']

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

    @Test
    void packageGuessUsesTheWorkspaceRootWhenNoSourceDirExists() {
        def root = folder.resolve('plain')
        Files.createDirectories(root.resolve('demo'))
        def top = root.resolve('Top.groovy')
        def nested = root.resolve('demo/Nested.groovy')
        Files.writeString(top, 'class Top {}')
        Files.writeString(nested, 'class Nested {}')
        assert PackageGuess.fromUri(nested.toUri(), [root.toUri()], []) == 'demo'
        assert PackageGuess.fromUri(top.toUri(), [root.toUri()], []) == ''
    }

    @Test
    void typeIndexIsSharedAcrossConcurrentReaders() {
        def alpha = new ClassNode('Alpha', 0, ClassHelper.OBJECT_TYPE)
        alpha.lineNumber = 1
        def module = new ModuleNode((SourceUnit) null)
        module.addClass(alpha)
        def uri = URI.create('file:///tmp/race-types.groovy')
        def snap = new CompilationSnapshot([(uri): new CompiledDocument(uri, 1, 'class Alpha {}', module, null, null)])
        int n = 8
        def barrier = new CyclicBarrier(n)
        def results = new TypeIndex[n]
        def errors = []
        def threads = (0..<n).collect { int i ->
            Thread.start {
                try {
                    barrier.await(5, TimeUnit.SECONDS)
                    results[i] = snap.types()
                } catch (Throwable thrown) {
                    synchronized (errors) {
                        errors << thrown
                    }
                }
            }
        }
        threads.each { thread ->
            thread.join(5000)
            assert !thread.alive
        }
        assert errors.isEmpty()
        assert results.every { it != null && it.is(results[0]) }
        assert results[0].byName('Alpha') != null
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
        def loader = missing.createClassLoader(WorkspaceLayoutTest.classLoader)
        assert loader != null
        loader.close()
        def on = new CompilerSettings(['.'], ['src'], 3, true, true)
        def replaced = on.withClasspath(['other.jar']).withExtra([keep: '2'])
        assert replaced.classpath == ['other.jar']
        assert replaced.sourcePaths == on.sourcePaths
        assert replaced.throughPhase == on.throughPhase
        assert replaced.grapeEnabled
        assert replaced.astTestEnabled
        assert replaced.extra.keep == '2'
        assert on.classpath == ['.']
        def merged = CompilerSettings.fromClientMap(null, on)
        assert merged.classpath == on.classpath
        assert CompilerSettings.fromClientMap([:], on).is(on)
        def fromNull = CompilerSettings.fromClientMap(null, null)
        assert fromNull.classpath.isEmpty()
        def applied = CompilerSettings.fromClientMap(
                [classpath: ['b.jar'], sourcePaths: ['lib'], grapeEnabled: true,
                 astTestEnabled: true, extraKey: 'v', listed: ['a', null, 'b'], blank: null, (null): 'x'],
                new CompilerSettings(['a.jar'], ['src'], 3, false, false, [keep: '1']))
        assert applied.classpath == ['b.jar']
        assert applied.sourcePaths == ['lib']
        assert applied.grapeEnabled
        assert applied.astTestEnabled
        assert applied.extra.keep == '1'
        assert applied.extra.extraKey == 'v'
        assert applied.extra.listed == '[a, b]'
        assert applied.extra.blank == ''
        def keepLists = CompilerSettings.fromClientMap([grapeEnabled: false], applied)
        assert keepLists.classpath == ['b.jar']
        def config = on.toConfiguration()
        assert !config.disabledGlobalASTTransformations.contains('groovy.grape.GrabAnnotationTransformation')
        on.createClassLoader(WorkspaceLayoutTest.classLoader).withCloseable { urls ->
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
                [], CompilerSettings.defaults(), WorkspaceLayoutTest.classLoader)
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
    void privateConstructorCanBeInvoked() {
        [PackageGuess].each { Class type ->
            def ctor = type.getDeclaredConstructor()
            ctor.accessible = true
            ctor.newInstance()
        }
    }

}

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
package org.codehaus.groovy.tools.javac

import groovy.lang.GroovyClassLoader
import org.codehaus.groovy.ast.decompiled.AsmDecompiler
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import javax.tools.ToolProvider
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

import static groovy.test.GroovyAssert.shouldFail

/**
 * Joint compilation with {@code memStub} and {@code memClassOutput}.
 * The fixture is small; the timeout is a ceiling, not a benchmark.
 */
@Timeout(value = 60, unit = TimeUnit.SECONDS)
final class MemClassOutputTest {

    private static final String PACKAGE = 'memclassoutput.cycle'
    private static final String GREET = '''\
        package memclassoutput.cycle

        class Greet {
            String hello(String name) { 'hello ' + name }

            static class Inner {
                static int value() { 7 }
            }
        }
        '''.stripIndent()

    private static final String JAVA_SIDE = '''\
        package memclassoutput.cycle;

        public class JavaSide {
            public static String call(Greet greet) {
                return greet.hello("java") + Greet.Inner.value();
            }
        }
        '''.stripIndent()

    private static final String BROKEN_JAVA = '''\
        package memclassoutput.cycle;

        public class JavaSide {
            public static String call(Greet greet) {
                int bad = "no";
                return greet.hello("java");
            }
        }
        '''.stripIndent()

    private static final String MAIN = '''\
        package memclassoutput.cycle

        import groovy.transform.CompileStatic

        @CompileStatic
        class Main {
            static void main(String[] args) {
                String result = JavaSide.call(new Greet())
                if (result != 'hello java7') {
                    throw new AssertionError(result)
                }
            }
        }
        '''.stripIndent()

    // GROOVY-12711
    @Test
    void testMemClassOutputRequiresBothOptions() {
        def onlyClassOutput = new CompilerConfiguration()
        onlyClassOutput.jointCompilationOptions = [memClassOutput: true]
        assert !onlyClassOutput.memClassOutput

        def both = new CompilerConfiguration()
        both.jointCompilationOptions = [memStub: true, memClassOutput: true]
        assert both.memClassOutput
    }

    // GROOVY-12711
    @Test
    void testMemStubAndMemClassOutputCompilesWithoutDirectories() {
        def sources = File.createTempDir('mem-class-src', '')
        try {
            def files = writeCycle(sources, JAVA_SIDE)
            def config = new CompilerConfiguration()
            config.setTargetDirectory((File) null)
            config.jointCompilationOptions = [memStub: true, memClassOutput: true]

            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(config, loader)
                unit.addSources(files.greet, files.javaSide, files.main)
                unit.compile()

                def javaBytes = loader.getResource("${PACKAGE.replace('.', '/')}/JavaSide.class").bytes
                assert javaBytes.length > 8
                assert javaBytes[0] == (byte) 0xCA
                assert javaBytes[1] == (byte) 0xFE
                assert javaBytes[2] == (byte) 0xBA
                assert javaBytes[3] == (byte) 0xBE
                def greetResource = loader.getResource("${PACKAGE.replace('.', '/')}/Greet.class")
                assert greetResource != null && greetResource.protocol != 'file'
                def greet = unit.AST.getClass("${PACKAGE}.Greet")
                assert greet != null
                assert !(greet instanceof DecompiledClassNode)

                runMain(loader)
                assertNoClassFiles(sources)
                assert !new File('memclassoutput').exists()
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            new File('memclassoutput').deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testTypeErrorIsReportedWithoutDirectories() {
        def sources = File.createTempDir('mem-class-err', '')
        try {
            def files = writeCycle(sources, BROKEN_JAVA)
            def config = new CompilerConfiguration()
            config.setTargetDirectory((File) null)
            config.jointCompilationOptions = [memStub: true, memClassOutput: true]

            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(config, loader)
                unit.addSources(files.greet, files.javaSide, files.main)
                def thrown = shouldFail(CompilationFailedException) {
                    unit.compile()
                }
                assert thrown.message.contains('incompatible types')
                assert thrown.message.contains('JavaSide.java')
                assertNoClassFiles(sources)
                assert !new File('memclassoutput').exists()
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            new File('memclassoutput').deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testDiskStubDirectoryStillCompilesWhenOptionOff() {
        def sources = File.createTempDir('mem-class-disk-src', '')
        def stubs = File.createTempDir('mem-class-stubs', '')
        def classes = File.createTempDir('mem-class-out', '')
        try {
            def files = writeCycle(sources, JAVA_SIDE)
            def config = new CompilerConfiguration()
            config.targetDirectory = classes
            config.jointCompilationOptions = [stubDir: stubs, keepStubs: true]

            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(config, loader)
                unit.addSources(files.greet, files.javaSide, files.main)
                unit.compile()

                assert loader.getResource('memclassoutput/cycle/JavaSide.class').protocol == 'file'
                assert new File(classes, 'memclassoutput/cycle/JavaSide.class').isFile()
                assert new File(classes, 'memclassoutput/cycle/Greet.class').isFile()
                assert new File(classes, 'memclassoutput/cycle/Main.class').isFile()
                assert stubJavaFiles(stubs).size() > 0
                runMain(loader)
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            stubs.deleteDir()
            classes.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testMemStubWithoutMemClassOutputWritesTargetDirectory() {
        def sources = File.createTempDir('mem-stub-src', '')
        def classes = File.createTempDir('mem-stub-out', '')
        try {
            def files = writeCycle(sources, JAVA_SIDE)
            def config = new CompilerConfiguration()
            config.targetDirectory = classes
            config.jointCompilationOptions = [memStub: true]

            assert !config.memClassOutput

            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(config, loader)
                unit.addSources(files.greet, files.javaSide, files.main)
                unit.compile()

                assert loader.getResource('memclassoutput/cycle/JavaSide.class').protocol == 'file'
                assert new File(classes, 'memclassoutput/cycle/JavaSide.class').isFile()
                assert new File(classes, 'memclassoutput/cycle/Main.class').isFile()
                assertNoClassFiles(sources)
                runMain(loader)
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            classes.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testMemClassOutputWithoutMemStubStillRequiresStubDir() {
        def sources = File.createTempDir('mem-class-nostub', '')
        def classes = File.createTempDir('mem-class-nostub-out', '')
        try {
            def files = writeCycle(sources, JAVA_SIDE)
            def config = new CompilerConfiguration()
            config.targetDirectory = classes
            config.jointCompilationOptions = [memClassOutput: true]

            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(config, loader)
                unit.addSources(files.greet, files.javaSide, files.main)
                def thrown = shouldFail(CompilationFailedException) {
                    unit.compile()
                }
                assert thrown.message.contains('memClassOutput requires memStub')
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            classes.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testSecondCompileOnTheSameLoaderSeesTheNewJavaApi() {
        def sources = File.createTempDir('mem-class-reload', '')
        try {
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                compileReload(sources, loader, STRING_KIND_JAVA, STRING_KIND_GROOVY)
                compileReload(sources, loader, INT_KIND_JAVA, INT_KIND_GROOVY)

                def type = loader.loadClass('memclassoutput.reload.Main')
                assert type.getDeclaredMethod('kind').invoke(null) == 2
                def resource = loader.getResource('memclassoutput/reload/JavaSide.class')
                assert resource != null && resource.protocol != 'file'
                assertNoClassFiles(sources)
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testReplacedBytesAreReadAgain() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        try {
            def stage = MemClassOutputClasspath.install(loader).begin(this)
            def resourceName = 'mem/cache/ex\u00e4mple/Foo$Bar.class'
            stage.put(resourceName, classBytes(CacheOne))
            def url = loader.getResource(resourceName)
            assert url != null
            assert url.protocol == 'x-groovy-mem'
            assert !MemClassOutputClasspath.isInMemory(null)
            assert !MemClassOutputClasspath.isInMemory(new File('.').toURI().toURL())
            assert url.openConnection().lastModified == 0L
            assert url.openConnection().contentLengthLong == classBytes(CacheOne).length
            assert AsmDecompiler.parseClass(url).className == CacheOne.name
            assert url.hashCode() == url.hashCode()
            assert url.equals(url)

            stage.put(resourceName, classBytes(CacheTwo))
            assert AsmDecompiler.parseClass(url).className == CacheTwo.name
        } finally {
            loader.close()
        }
    }

    // GROOVY-12711
    @Test
    void testPlusInResourceNameIsRetrievable() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        try {
            def store = MemClassOutputClasspath.install(loader)
            def stage = store.begin(this)
            store.commit(new Object())
            store.abort(new Object())
            stage.put('p/a+b.class', [1, 2, 3] as byte[])
            stage.put('p/a$b.class', [4] as byte[])
            assert loader.getResource('p/a+b.class').bytes == [1, 2, 3] as byte[]
            assert loader.getResource('p/a$b.class').bytes == [4] as byte[]
        } finally {
            loader.close()
        }
    }

    // GROOVY-12711
    @Test
    void testEscapingResourcePathsAreRejected() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        try {
            def stage = MemClassOutputClasspath.install(loader).begin(this)
            stage.put('p/A.class', [1] as byte[])
            def url = loader.getResource('p/A.class')
            assert rejected(new URL(url, '/%2e%2e/secret.class'))
            assert rejected(new URL(url, '/%5csecret.class'))
            assert rejected(new URL(url, '/%zz'))
            assert rejected(new URL(url, '//x'))
            assert rejected(new URL(url, '/other-id/p/A.class'))
            assert new URL(url, '/%zz').openConnection().contentLengthLong == -1L
        } finally {
            loader.close()
        }
    }

    // GROOVY-12711
    @Test
    void testMemClassOutputRejectsACompilerThatWritesToDisk() {
        def sources = File.createTempDir('mem-class-factory', '')
        try {
            def files = writeCycle(sources, JAVA_SIDE)
            def config = new CompilerConfiguration()
            config.setTargetDirectory((File) null)
            config.jointCompilationOptions = [memStub: true, memClassOutput: true]

            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(config, loader)
                unit.compilerFactory = new JavaCompilerFactory() {
                    @Override
                    JavaCompiler createCompiler(CompilerConfiguration ignored) {
                        new JavaCompiler() {
                            @Override
                            void compile(List<String> javaFiles, CompilationUnit cu) {
                            }
                        }
                    }
                }
                unit.addSources(files.greet, files.javaSide, files.main)
                def thrown = shouldFail(CompilationFailedException) {
                    unit.compile()
                }
                assert thrown.message.contains('memClassOutput')
                assertNoClassFiles(sources)
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            new File('memclassoutput').deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testMemClassOutputRequiresTheJointCompilationUnit() {
        def config = new CompilerConfiguration()
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        def compiler = new JavacJavaCompiler(config)
        def unit = new CompilationUnit(config)
        def thrown = shouldFail(CompilationFailedException) {
            compiler.compile(List.of('JavaSide.java'), unit)
        }
        assert thrown.message.contains('JavaAwareCompilationUnit')
    }

    // GROOVY-12711
    @Test
    void testGroovyPackageDirectoryIsCreatedWhenJavacDidNot() {
        def sources = File.createTempDir('mem-mkdir-src', '')
        def classes = File.createTempDir('mem-mkdir-out', '')
        try {
            def javaFile = write(sources, 'onlyjava/JavaSide.java', '''\
                package onlyjava;

                public class JavaSide {
                    public static int n() { return 1; }
                }
                '''.stripIndent())
            def groovyFile = write(sources, 'onlygroovy/Main.groovy', '''\
                package onlygroovy

                class Main {
                    static int n() { onlyjava.JavaSide.n() }
                }
                '''.stripIndent())
            def config = new CompilerConfiguration()
            config.targetDirectory = classes
            config.jointCompilationOptions = [memStub: true]

            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(config, loader)
                unit.addSources(javaFile, groovyFile)
                unit.compile()
                assert new File(classes, 'onlyjava/JavaSide.class').isFile()
                assert new File(classes, 'onlygroovy/Main.class').isFile()
                assert loader.loadClass('onlygroovy.Main').getDeclaredMethod('n').invoke(null) == 1
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            classes.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testFileManagerKeepsClassBytesOffDisk() {
        def sourceDir = File.createTempDir('mem-fm-src', '')
        def outputDir = File.createTempDir('mem-fm-out', '')
        try {
            def javaFile = new File(sourceDir, 'Hello.java')
            javaFile.text = '''\
                public class Hello {
                    public static String hi() { return "hi"; }
                    public static class Nested { public static int n() { return 2; } }
                }
                '''.stripIndent()

            def compiler = ToolProvider.systemJavaCompiler
            assert compiler != null
            def standard = compiler.getStandardFileManager(null, Locale.ENGLISH, Charset.forName('UTF-8'))
            def manager = new InMemoryJavaFileManager(standard)
            def log = new StringWriter()
            def task = compiler.getTask(
                    log,
                    manager,
                    null,
                    List.of('-d', outputDir.absolutePath, '--release', '17'),
                    null,
                    standard.getJavaFileObjectsFromFiles(List.of(javaFile))
            )
            assert task.call() : log.toString()
            manager.close()

            def hello = manager.getClassBytes('Hello')
            def nested = manager.getClassBytes('Hello$Nested')
            assert hello != null && hello.length > 8
            assert hello[0] == (byte) 0xCA
            assert nested != null && nested.length > 8
            assert outputDir.listFiles()?.length == 0
            assert !new File(sourceDir, 'Hello.class').exists()
            assert !new File(sourceDir, 'Hello$Nested.class').exists()
        } finally {
            sourceDir.deleteDir()
            outputDir.deleteDir()
        }
    }

    private static Map<String, File> writeCycle(File root, String javaSource) {
        [
            greet   : write(root, 'memclassoutput/cycle/Greet.groovy', GREET),
            javaSide: write(root, 'memclassoutput/cycle/JavaSide.java', javaSource),
            main    : write(root, 'memclassoutput/cycle/Main.groovy', MAIN),
        ]
    }

    private static File write(File root, String relative, String text) {
        def file = new File(root, relative)
        file.parentFile.mkdirs()
        file.text = text
        file
    }

    private static void runMain(GroovyClassLoader loader) {
        def type = loader.loadClass("${PACKAGE}.Main")
        type.getDeclaredMethod('main', String[]).invoke(null, new Object[]{new String[0]})
    }

    private static void assertNoClassFiles(File root) {
        def classFiles = []
        root.eachFileRecurse { File file ->
            if (file.name.endsWith('.class')) {
                classFiles << file
            }
        }
        assert classFiles.empty
    }

    // GROOVY-12711
    @Test
    void testPhasedCompileKeepsJavacOutput() {
        def sources = File.createTempDir('mem-phased', '')
        try {
            def javaFile = write(sources, 'probe/JavaSide.java', STRING_KIND_JAVA.replace('memclassoutput.reload', 'probe'))
            def groovyFile = write(sources, 'probe/Main.groovy', STRING_KIND_GROOVY.replace('memclassoutput.reload', 'probe'))
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = memUnit(loader)
                unit.addSources(javaFile, groovyFile)
                unit.compile(Phases.SEMANTIC_ANALYSIS)
                assert loader.getResource('probe/JavaSide.class') != null

                unit.compile()
                assert loader.getResource('probe/JavaSide.class') != null
                assert loader.loadClass('probe.Main').getDeclaredMethod('kind').invoke(null) == 'text'
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testSecondUnitDoesNotRemoveFirstUnitsClasses() {
        def sources = File.createTempDir('mem-shared', '')
        try {
            def javaFile = write(sources, 'probe/JavaSide.java', STRING_KIND_JAVA.replace('memclassoutput.reload', 'probe'))
            def mainFile = write(sources, 'probe/Main.groovy', STRING_KIND_GROOVY.replace('memclassoutput.reload', 'probe'))
            def userFile = write(sources, 'other/User.groovy', '''\
                package other

                import probe.JavaSide

                class User {
                    static String go() { JavaSide.kind() }
                }
                '''.stripIndent())
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def first = memUnit(loader)
                first.addSources(javaFile, mainFile)
                first.compile()

                def second = memUnit(loader)
                second.addSources(userFile)
                second.compile()
                assert loader.getResource('probe/JavaSide.class') != null
                assert loader.loadClass('other.User').getDeclaredMethod('go').invoke(null) == 'text'
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testFailedRecompileKeepsLastGoodOutput() {
        def sources = File.createTempDir('mem-fail', '')
        try {
            def javaFile = write(sources, 'probe/JavaSide.java', STRING_KIND_JAVA.replace('memclassoutput.reload', 'probe'))
            def mainFile = write(sources, 'probe/Main.groovy', STRING_KIND_GROOVY.replace('memclassoutput.reload', 'probe'))
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def first = memUnit(loader)
                first.addSources(javaFile, mainFile)
                first.compile()
                def javaBefore = loader.getResource('probe/JavaSide.class').bytes
                def mainBefore = loader.getResource('probe/Main.class').bytes

                javaFile.text = 'package probe; public class JavaSide { public static int kind() { return "no"; } }'
                def second = memUnit(loader)
                second.addSources(javaFile, mainFile)
                def thrown = shouldFail(CompilationFailedException) {
                    second.compile()
                }
                assert thrown.message.contains('incompatible types')
                assert Arrays.equals(javaBefore, loader.getResource('probe/JavaSide.class').bytes)
                assert Arrays.equals(mainBefore, loader.getResource('probe/Main.class').bytes)
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testStubBytecodeIsNeverPublished() {
        def sources = File.createTempDir('mem-stub', '')
        try {
            def javaFile = write(sources, 'probe/JavaSide.java', STRING_KIND_JAVA.replace('memclassoutput.reload', 'probe'))
            def mainFile = write(sources, 'probe/Main.groovy', STRING_KIND_GROOVY.replace('memclassoutput.reload', 'probe'))
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = memUnit(loader)
                unit.addPhaseOperation({ source, context, classNode ->
                    if (classNode.name == 'probe.Main') {
                        assert loader.getResource('probe/Main.class') == null
                        assert loader.getResource('probe/JavaSide.class') != null
                    }
                } as CompilationUnit.IPrimaryClassNodeOperation, Phases.CANONICALIZATION)
                unit.addSources(javaFile, mainFile)
                unit.compile()
                assert loader.getResource('probe/Main.class') != null
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testModuleImportWithMemClassOutput() {
        def sources = File.createTempDir('mem-module', '')
        try {
            def file = write(sources, 'mod/Use.groovy', '''\
                import module java.base

                class Use {
                    static int n() { List.of(1).size() }
                }
                '''.stripIndent())
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = memUnit(loader)
                unit.addSources(file)
                unit.compile()
                assert loader.loadClass('Use').getDeclaredMethod('n').invoke(null) == 1
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testLoadedClassIsNotReplaced() {
        def sources = File.createTempDir('mem-pinned', '')
        try {
            def javaFile = write(sources, 'probe/JavaSide.java', STRING_KIND_JAVA.replace('memclassoutput.reload', 'probe'))
            def mainFile = write(sources, 'probe/Main.groovy', STRING_KIND_GROOVY.replace('memclassoutput.reload', 'probe'))
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def first = memUnit(loader)
                first.addSources(javaFile, mainFile)
                first.compile()
                assert loader.loadClass('probe.Main').getDeclaredMethod('kind').invoke(null) == 'text'

                javaFile.text = INT_KIND_JAVA.replace('memclassoutput.reload', 'probe')
                mainFile.text = INT_KIND_GROOVY.replace('memclassoutput.reload', 'probe')
                def second = memUnit(loader)
                second.addSources(javaFile, mainFile)
                second.compile()
                def again = loader.loadClass('probe.Main').getDeclaredMethod('kind').invoke(null)
                assert again == 'text'
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testShadowedOutputIsReported() {
        def sources = File.createTempDir('mem-shadow-src', '')
        def stale = File.createTempDir('mem-shadow-stale', '')
        try {
            def javaFile = write(sources, 'probe/JavaSide.java', STRING_KIND_JAVA.replace('memclassoutput.reload', 'probe'))
            def mainFile = write(sources, 'probe/Main.groovy', STRING_KIND_GROOVY.replace('memclassoutput.reload', 'probe'))
            def disk = new CompilerConfiguration()
            disk.targetDirectory = stale
            disk.jointCompilationOptions = [memStub: true]
            def diskLoader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(disk, diskLoader)
                unit.addSources(javaFile, mainFile)
                unit.compile()
            } finally {
                diskLoader.close()
            }

            javaFile.text = INT_KIND_JAVA.replace('memclassoutput.reload', 'probe')
            mainFile.text = INT_KIND_GROOVY.replace('memclassoutput.reload', 'probe')
            def loader = new GroovyClassLoader(this.class.classLoader)
            loader.addClasspath(stale.absolutePath)
            try {
                def unit = memUnit(loader)
                unit.addSources(javaFile, mainFile)
                def thrown = shouldFail(CompilationFailedException) {
                    unit.compile()
                }
                assert thrown.message.contains('String')
                assert unit.errorCollector.warnings.any { it.message.contains('hidden by') }
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
            stale.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testAnotherUnitCannotPublishWhileAStageIsOpen() {
        def sources = File.createTempDir('mem-busy', '')
        try {
            def javaFile = write(sources, 'probe/JavaSide.java', STRING_KIND_JAVA.replace('memclassoutput.reload', 'probe'))
            def mainFile = write(sources, 'probe/Main.groovy', STRING_KIND_GROOVY.replace('memclassoutput.reload', 'probe'))
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def held = memUnit(loader)
                held.addSources(javaFile, mainFile)
                held.compile(Phases.SEMANTIC_ANALYSIS)

                def other = memUnit(loader)
                other.addSources(mainFile)
                shouldFail(IllegalStateException) {
                    other.compile()
                }
                held.compile()
            } finally {
                loader.close()
            }
        } finally {
            sources.deleteDir()
        }
    }

    // GROOVY-12711
    @Test
    void testDecompilerCacheDoesNotRetainInMemoryKeys() {
        def field = AsmDecompiler.getDeclaredField('stubCache')
        field.accessible = true
        Map cache = field.get(null) as Map
        int before = cache.keySet().count { it.scheme == 'x-groovy-mem' }
        8.times {
            def loader = new GroovyClassLoader(this.class.classLoader)
            try {
                def store = MemClassOutputClasspath.install(loader)
                def stage = store.begin(loader)
                stage.put('p/T.class', classBytes(CacheOne))
                AsmDecompiler.parseClass(loader.getResource('p/T.class'))
                store.commit(loader)
            } finally {
                loader.close()
            }
        }
        int after = cache.keySet().count { it.scheme == 'x-groovy-mem' }
        assert after == before
    }

    private static JavaAwareCompilationUnit memUnit(GroovyClassLoader loader) {
        def config = new CompilerConfiguration()
        config.setTargetDirectory((File) null)
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        new JavaAwareCompilationUnit(config, loader)
    }

    private static final String STRING_KIND_JAVA = '''\
        package memclassoutput.reload;

        public class JavaSide {
            public static String kind() { return "text"; }
        }
        '''.stripIndent()

    private static final String STRING_KIND_GROOVY = '''\
        package memclassoutput.reload

        import groovy.transform.CompileStatic

        @CompileStatic
        class Main {
            static String kind() { JavaSide.kind() }
        }
        '''.stripIndent()

    private static final String INT_KIND_JAVA = '''\
        package memclassoutput.reload;

        public class JavaSide {
            public static int kind() { return 2; }
        }
        '''.stripIndent()

    private static final String INT_KIND_GROOVY = '''\
        package memclassoutput.reload

        import groovy.transform.CompileStatic

        @CompileStatic
        class Main {
            static int kind() { JavaSide.kind() }
        }
        '''.stripIndent()

    private static void compileReload(File root, GroovyClassLoader loader, String javaSource, String groovySource) {
        def javaFile = write(root, 'memclassoutput/reload/JavaSide.java', javaSource)
        def groovyFile = write(root, 'memclassoutput/reload/Main.groovy', groovySource)
        def config = new CompilerConfiguration()
        config.setTargetDirectory((File) null)
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        def unit = new JavaAwareCompilationUnit(config, loader)
        unit.addSources(javaFile, groovyFile)
        unit.compile()
    }

    private static boolean rejected(URL url) {
        try {
            url.openStream().close()
            return false
        } catch (FileNotFoundException e) {
            return true
        }
    }

    private static byte[] classBytes(Class<?> type) {
        def name = type.name.replace('.', '/') + '.class'
        MemClassOutputTest.classLoader.getResource(name).bytes
    }

    private static final class CacheOne {
    }

    private static final class CacheTwo {
    }

    private static List<File> stubJavaFiles(File root) {
        def stubs = []
        root.eachFileRecurse { File file ->
            if (file.name.endsWith('.java')) {
                stubs << file
            }
        }
        stubs
    }
}

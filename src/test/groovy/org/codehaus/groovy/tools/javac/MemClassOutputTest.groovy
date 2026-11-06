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
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
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

                def javaBytes = unit.inMemoryJavaFileManager.getClassBytes("${PACKAGE}.JavaSide")
                assert javaBytes != null && javaBytes.length > 8
                assert javaBytes[0] == (byte) 0xCA
                assert javaBytes[1] == (byte) 0xFE
                assert javaBytes[2] == (byte) 0xBA
                assert javaBytes[3] == (byte) 0xBE

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
                assert thrown.message =~ /(?i)(javac|incompatible|error)/
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

                assert unit.inMemoryJavaFileManager == null
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

                assert unit.inMemoryJavaFileManager == null
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
                shouldFail(GroovyBugError) {
                    unit.compile()
                }
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

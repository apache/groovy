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
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.util.concurrent.TimeUnit

import static groovy.test.GroovyAssert.shouldFail

/**
 * In-memory class output published from one joint compilation to a later one.
 */
@Timeout(value = 180, unit = TimeUnit.SECONDS)
final class MemClassOutputPublicationTest {

    private static final String LIB_TEXT = '''\
        package mempub;

        public class Lib {
            public static String kind() { return "text"; }
        }
        '''.stripIndent()

    private static final String LIB_INT = '''\
        package mempub;

        public class Lib {
            public static int kind() { return 2; }
        }
        '''.stripIndent()

    private static final String LIB_BROKEN = '''\
        package mempub;

        public class Lib {
            public static String kind() {
                int bad = "no";
                return "text";
            }
        }
        '''.stripIndent()

    private static final String OTHER = '''\
        package mempub

        class Other {
            static String id() { 'other' }
        }
        '''.stripIndent()

    private static final String USER = '''\
        package mempub

        import groovy.transform.CompileStatic

        @CompileStatic
        class User {
            static int n() { Lib.kind() }

            static String id() { Other.id() }
        }
        '''.stripIndent()

    private static final String PEEK_TEXT = '''\
        package mempub

        import groovy.transform.CompileStatic

        @CompileStatic
        class Peek {
            static String kind() { Lib.kind() }
        }
        '''.stripIndent()

    private static final String PEEK_INT = '''\
        package mempub

        import groovy.transform.CompileStatic

        @CompileStatic
        class Peek {
            static int kind() { Lib.kind() }
        }
        '''.stripIndent()

    private static final String TOOL = '''\
        package mempub

        class Tool {
            static int n() { 3 }
        }
        '''.stripIndent()

    private static final String USE_TOOL = '''\
        package mempub

        import groovy.transform.CompileStatic

        @CompileStatic
        class UseTool {
            static int n() { Tool.n() }
        }
        '''.stripIndent()

    // GROOVY-12751
    @Test
    void testPublishedOutputIsVisibleAndOverlapUsesTheNewBytes() {
        def sources = File.createTempDir('mem-pub-src', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def config = memConfig()
            def publication = new MemClassOutputPublication(parent, config)
            def first = publication.newCompilationLoader()
            try {
                compile(sources, first, [
                        'mempub/Lib.java'  : LIB_TEXT,
                        'mempub/Other.groovy': OTHER,
                ])
                def defined = first.loadClass('mempub.Lib')
                assert defined.getDeclaredMethod('kind').invoke(null) == 'text'
                assert memUrls(parent).empty
                shouldFail(ClassNotFoundException) { parent.loadClass('mempub.Lib') }
                assertNoClassFiles(sources)

                publication.publish(first)

                def second = publication.newCompilationLoader()
                try {
                    compile(sources, second, [
                            'mempub/Lib.java' : LIB_INT,
                            'mempub/User.groovy': USER,
                    ])
                    assert second.loadClass('mempub.User').getDeclaredMethod('n').invoke(null) == 2
                    assert second.loadClass('mempub.User').getDeclaredMethod('id').invoke(null) == 'other'
                    assert !Arrays.equals(first.getResource('mempub/Lib.class').bytes, second.getResource('mempub/Lib.class').bytes)
                    assert second.getResource('mempub/Other.class') != null
                    assert second.getResource('mempub/Other.class').protocol == 'x-groovy-mem'
                    assert second.getResource('mempub/Missing.class') == null
                    assert memUrls(second).size() == 1
                    assert MemClassOutputClasspath.isInMemory(second.URLs[-1])
                    assert first.loadClass('mempub.Lib').is(defined)
                    assert defined.getDeclaredMethod('kind').invoke(null) == 'text'
                    assert memUrls(parent).empty
                    shouldFail(ClassNotFoundException) { parent.loadClass('mempub.Lib') }
                    shouldFail(ClassNotFoundException) { parent.loadClass('mempub.Other') }

                    def before = publication.newCompilationLoader()
                    try {
                        compile(sources, before, ['mempub/Peek.groovy': PEEK_TEXT])
                        assert before.loadClass('mempub.Peek').getDeclaredMethod('kind').invoke(null) == 'text'
                    } finally {
                        before.close()
                    }

                    publication.publish(second)
                    def after = publication.newCompilationLoader()
                    try {
                        compile(sources, after, ['mempub/Peek.groovy': PEEK_INT])
                        assert after.loadClass('mempub.Peek').getDeclaredMethod('kind').invoke(null) == 2
                        assert defined.getDeclaredMethod('kind').invoke(null) == 'text'
                    } finally {
                        after.close()
                    }
                } finally {
                    second.close()
                }
            } finally {
                first.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testUnpublishedCompilationIsInvisible() {
        def sources = File.createTempDir('mem-pub-none', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def config = memConfig()
            def publication = new MemClassOutputPublication(parent, config)
            def first = publication.newCompilationLoader()
            try {
                compile(sources, first, ['mempub/Lib.java': LIB_TEXT])
                def second = publication.newCompilationLoader()
                try {
                    def thrown = shouldFail(CompilationFailedException) {
                        compile(sources, second, ['mempub/Peek.groovy': PEEK_TEXT])
                    }
                    assert thrown.message.contains('Lib')
                } finally {
                    second.close()
                }
            } finally {
                first.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testAnotherPublicationDoesNotSeePublishedBytes() {
        def sources = File.createTempDir('mem-pub-other', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def config = memConfig()
            def firstPublication = new MemClassOutputPublication(parent, config)
            def first = firstPublication.newCompilationLoader()
            try {
                compile(sources, first, ['mempub/Lib.java': LIB_TEXT])
                firstPublication.publish(first)

                def secondPublication = new MemClassOutputPublication(parent, config)
                def second = secondPublication.newCompilationLoader()
                try {
                    def thrown = shouldFail(CompilationFailedException) {
                        compile(sources, second, ['mempub/Peek.groovy': PEEK_TEXT])
                    }
                    assert thrown.message.contains('Lib')
                    def foreign = shouldFail(IllegalArgumentException) {
                        secondPublication.publish(first)
                    }
                    assert foreign.message.contains('not created by this publication')
                } finally {
                    second.close()
                }
            } finally {
                first.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testFailedCompileDoesNotReplacePublishedBytes() {
        def sources = File.createTempDir('mem-pub-fail', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def config = memConfig()
            def publication = new MemClassOutputPublication(parent, config)
            def first = publication.newCompilationLoader()
            try {
                compile(sources, first, ['mempub/Lib.java': LIB_TEXT])
                def previous = first.getResource('mempub/Lib.class').bytes
                publication.publish(first)

                def second = publication.newCompilationLoader()
                try {
                    def thrown = shouldFail(CompilationFailedException) {
                        compile(sources, second, ['mempub/Lib.java': LIB_BROKEN])
                    }
                    assert thrown.message.contains('incompatible types')
                    publication.publish(second)
                    assert Arrays.equals(previous, second.getResource('mempub/Lib.class').bytes)
                    assert second.loadClass('mempub.Lib').getDeclaredMethod('kind').invoke(null) == 'text'
                } finally {
                    second.close()
                }
            } finally {
                first.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testUncommittedStageIsNotPublished() {
        def sources = File.createTempDir('mem-pub-stage', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def config = memConfig()
            def publication = new MemClassOutputPublication(parent, config)
            def first = publication.newCompilationLoader()
            try {
                compile(sources, first, ['mempub/Lib.java': LIB_TEXT, 'mempub/Other.groovy': OTHER])
                def previous = first.getResource('mempub/Lib.class').bytes
                publication.publish(first)

                def second = publication.newCompilationLoader()
                try {
                    def unit = unit(config, second)
                    unit.addSources(
                            write(sources, 'mempub/Lib.java', LIB_INT),
                            write(sources, 'mempub/Marker.groovy', 'package mempub\nclass Marker { static int n() { 1 } }\n')
                    )
                    unit.compile(Phases.SEMANTIC_ANALYSIS)
                    assert !Arrays.equals(previous, second.getResource('mempub/Lib.class').bytes)
                    publication.publish(second)
                } finally {
                    second.close()
                }

                def peek = publication.newCompilationLoader()
                try {
                    compile(sources, peek, ['mempub/Peek.groovy': PEEK_TEXT])
                    assert peek.loadClass('mempub.Peek').getDeclaredMethod('kind').invoke(null) == 'text'
                } finally {
                    peek.close()
                }
            } finally {
                first.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testParentClasspathHidesPublishedOutput() {
        def sources = File.createTempDir('mem-pub-shadow-src', '')
        def stale = File.createTempDir('mem-pub-shadow-out', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def disk = new CompilerConfiguration()
            disk.targetDirectory = stale
            disk.jointCompilationOptions = [memStub: true]
            def diskLoader = new GroovyClassLoader(this.class.classLoader)
            try {
                def unit = new JavaAwareCompilationUnit(disk, diskLoader)
                unit.addSources(
                        write(sources, 'mempub/Lib.java', LIB_TEXT),
                        write(sources, 'mempub/Main.groovy', PEEK_TEXT.replace('class Peek', 'class Main'))
                )
                unit.compile()
            } finally {
                diskLoader.close()
            }

            parent.addClasspath(stale.absolutePath)
            write(sources, 'mempub/Lib.java', LIB_INT)
            write(sources, 'mempub/Main.groovy', PEEK_INT.replace('class Peek', 'class Main'))
            def config = memConfig()
            def publication = new MemClassOutputPublication(parent, config)
            def loader = publication.newCompilationLoader()
            try {
                def unit = unit(config, loader)
                unit.addSources(new File(sources, 'mempub/Lib.java'), new File(sources, 'mempub/Main.groovy'))
                def thrown = shouldFail(CompilationFailedException) {
                    unit.compile()
                }
                assert thrown.message.contains('String')
                assert unit.errorCollector.warnings.any { it.message.contains('hidden by') }
                assert memUrls(parent).empty
            } finally {
                loader.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
            stale.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testSecondUnitOnTheSameCompilationLoaderIsRejected() {
        def sources = File.createTempDir('mem-pub-twice', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def config = memConfig()
            def publication = new MemClassOutputPublication(parent, config)
            def loader = publication.newCompilationLoader()
            try {
                compile(sources, loader, ['mempub/Tool.groovy': TOOL])
                def unit = unit(config, loader)
                unit.addSources(new File(sources, 'mempub/Tool.groovy'))
                def thrown = shouldFail(IllegalStateException) {
                    unit.compile()
                }
                assert thrown.message.contains('already in use')
                assert loader.loadClass('mempub.Tool').getDeclaredMethod('n').invoke(null) == 3
            } finally {
                loader.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testSameCompilationUnitCanCompileAgain() {
        def sources = File.createTempDir('mem-pub-same', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def config = memConfig()
            def publication = new MemClassOutputPublication(parent, config)
            def loader = publication.newCompilationLoader()
            try {
                def unit = unit(config, loader)
                unit.addSources(write(sources, 'mempub/Lib.java', LIB_TEXT))
                unit.compile()
                unit.addSources(write(sources, 'mempub/Peek.groovy', PEEK_TEXT))
                unit.compile()
                assert loader.loadClass('mempub.Peek').getDeclaredMethod('kind').invoke(null) == 'text'
                assert loader.loadClass('mempub.Lib').getDeclaredMethod('kind').invoke(null) == 'text'
            } finally {
                loader.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testPublishRejectsLoadersFromOutsideTheCompilation() {
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def publication = new MemClassOutputPublication(parent, memConfig())
            def thrown = shouldFail(NullPointerException) {
                publication.publish(null)
            }
            assert thrown.message == 'loader'

            def plain = new GroovyClassLoader(parent)
            try {
                def foreign = shouldFail(IllegalArgumentException) {
                    publication.publish(plain)
                }
                assert foreign.message.contains('not created by this publication')
            } finally {
                plain.close()
            }

            def loader = publication.newCompilationLoader()
            try {
                def early = shouldFail(IllegalStateException) {
                    publication.publish(loader)
                }
                assert early.message.contains('no in-memory class output')
            } finally {
                loader.close()
            }
        } finally {
            parent.close()
        }
    }

    // GROOVY-12751
    @Test
    void testPublicationRequiresAParent() {
        def thrown = shouldFail(NullPointerException) {
            new MemClassOutputPublication(null)
        }
        assert thrown.message == 'parent'
        def withConfig = shouldFail(NullPointerException) {
            new MemClassOutputPublication(null, memConfig())
        }
        assert withConfig.message == 'parent'
    }

    // GROOVY-12751
    @Test
    void testDefaultConfigurationPublishesGroovyClasses() {
        def sources = File.createTempDir('mem-pub-groovy', '')
        def parent = new GroovyClassLoader(this.class.classLoader)
        try {
            def publication = new MemClassOutputPublication(parent)
            def first = publication.newCompilationLoader()
            try {
                compile(sources, first, ['mempub/Tool.groovy': TOOL])
                publication.publish(first)
                def second = publication.newCompilationLoader()
                try {
                    compile(sources, second, ['mempub/UseTool.groovy': USE_TOOL])
                    assert second.loadClass('mempub.UseTool').getDeclaredMethod('n').invoke(null) == 3
                    assert second.getResource('mempub/Tool.class').protocol == 'x-groovy-mem'
                    assertNoClassFiles(sources)
                } finally {
                    second.close()
                }
            } finally {
                first.close()
            }
        } finally {
            parent.close()
            sources.deleteDir()
        }
    }

    // GROOVY-12751
    @Test
    void testOrdinaryLoaderStillAppendsAUrlPerOpen() {
        def loader = new GroovyClassLoader(this.class.classLoader)
        try {
            MemClassOutputClasspath.open(loader)
            MemClassOutputClasspath.open(loader)
            assert memUrls(loader).size() == 2
        } finally {
            loader.close()
        }
    }

    private static CompilerConfiguration memConfig() {
        def config = new CompilerConfiguration()
        config.setTargetDirectory((File) null)
        config.jointCompilationOptions = [memStub: true, memClassOutput: true]
        config
    }

    private static JavaAwareCompilationUnit unit(CompilerConfiguration config, GroovyClassLoader loader) {
        new JavaAwareCompilationUnit(config, loader)
    }

    private static void compile(File root, GroovyClassLoader loader, Map<String, String> sources) {
        def config = memConfig()
        def unit = unit(config, loader)
        def files = []
        sources.each { String relative, String text ->
            files << write(root, relative, text)
        }
        unit.addSources(files as File[])
        unit.compile()
    }

    private static File write(File root, String relative, String text) {
        def file = new File(root, relative)
        file.parentFile.mkdirs()
        file.text = text
        file
    }

    private static List<URL> memUrls(GroovyClassLoader loader) {
        loader.URLs.findAll { MemClassOutputClasspath.isInMemory(it) }
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
}

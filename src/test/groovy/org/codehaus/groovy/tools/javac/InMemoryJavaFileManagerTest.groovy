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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import javax.tools.JavaFileObject
import javax.tools.StandardLocation
import javax.tools.ToolProvider
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

import static groovy.test.GroovyAssert.shouldFail

/**
 * Direct coverage of in-memory javac class output.
 * Joint compilation is covered by {@link MemClassOutputTest}.
 */
@Timeout(value = 60, unit = TimeUnit.SECONDS)
final class InMemoryJavaFileManagerTest {

    private static final byte[] MARKER = [0xCA, 0xFE, 0xBA, 0xBE] as byte[]

    // GROOVY-12711
    @Test
    void testResourceNameRejectsPathsThatEscapeTheMap() {
        assert InMemoryJavaFileManager.resourceName('pkg.Outer$Inner') == 'pkg/Outer$Inner.class'
        shouldFail(IllegalArgumentException) { InMemoryJavaFileManager.resourceName(null) }
        shouldFail(IllegalArgumentException) { InMemoryJavaFileManager.resourceName('') }
        shouldFail(IllegalArgumentException) { InMemoryJavaFileManager.resourceName('..') }
        shouldFail(IllegalArgumentException) { InMemoryJavaFileManager.resourceName('a\\b') }
    }

    // GROOVY-12711
    @Test
    void testClassOutputStaysInTheManager() {
        def compiler = ToolProvider.systemJavaCompiler
        assert compiler != null
        def standard = compiler.getStandardFileManager(null, Locale.ENGLISH, Charset.forName('UTF-8'))
        def sink = new LinkedHashMap<String, byte[]>()
        def manager = new InMemoryJavaFileManager(standard, sink)
        try {
            assert manager.hasLocation(StandardLocation.CLASS_OUTPUT)
            assert manager.getClassLoader(StandardLocation.CLASS_OUTPUT) == null
            shouldFail(NullPointerException) { manager.getClassBytes((String) null) }
            assert manager.getClassBytes('missing.Type') == null

            def options = ['ignored-dir', '-g'].iterator()
            assert manager.handleOption('-d', options)
            assert options.next() == '-g'
            assert manager.handleOption('-d', [].iterator())

            assert manager.list(StandardLocation.CLASS_OUTPUT, 'pkg', null, false).toList().empty
            assert manager.list(StandardLocation.CLASS_OUTPUT, 'pkg', EnumSet.of(JavaFileObject.Kind.SOURCE), true).toList().empty

            def unwritten = manager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, 'pkg.Empty', JavaFileObject.Kind.CLASS, null)
            assert manager.getJavaFileForInput(StandardLocation.CLASS_OUTPUT, 'pkg.Empty', JavaFileObject.Kind.CLASS) == null
            assert manager.contains(StandardLocation.CLASS_OUTPUT, unwritten)
            assert !manager.contains(StandardLocation.SOURCE_PATH, unwritten)
            shouldFail(IOException) { unwritten.openInputStream() }

            write(manager, 'pkg.A')
            write(manager, 'pkg.A$Inner')
            write(manager, 'pkg.sub.B')
            write(manager, 'Top')
            assert manager.getClassBytes('pkg.A') == MARKER
            assert sink['pkg/A.class'] == MARKER
            assert sink['pkg/A$Inner.class'] == MARKER

            assert listed(manager, 'pkg', false) as Set == ['pkg.A', 'pkg.A$Inner'] as Set
            assert listed(manager, 'pkg', true) as Set == ['pkg.A', 'pkg.A$Inner', 'pkg.sub.B'] as Set
            assert listed(manager, '', false) as Set == ['Top'] as Set
            assert listed(manager, '', true) as Set == ['pkg.A', 'pkg.A$Inner', 'pkg.sub.B', 'Top'] as Set

            def snapshot = manager.getClassBytes()
            write(manager, 'pkg.Later')
            assert snapshot.keySet() == ['pkg.A', 'pkg.A$Inner', 'pkg.sub.B', 'Top'] as Set
            shouldFail(UnsupportedOperationException) { snapshot.put('x', MARKER) }
            assert 'pkg.Later' in listed(manager, 'pkg', false)

            def again = manager.getJavaFileForInput(StandardLocation.CLASS_OUTPUT, 'pkg.A', JavaFileObject.Kind.CLASS)
            assert manager.isSameFile(unwritten, again) == false
            assert manager.isSameFile(again, manager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, 'pkg.A', JavaFileObject.Kind.CLASS, null))
            assert manager.inferBinaryName(StandardLocation.CLASS_OUTPUT, again) == 'pkg.A'

            def fromRelative = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, 'pkg', 'C.class', null)
            fromRelative.openOutputStream().withCloseable { it.write(MARKER) }
            assert manager.inferBinaryName(StandardLocation.CLASS_OUTPUT, fromRelative) == 'pkg.C'
            def top = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, '', 'D.class', null)
            assert manager.inferBinaryName(StandardLocation.CLASS_OUTPUT, top) == 'D'
            def nested = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, 'pkg', 'sub/C.class', null)
            nested.openOutputStream().withCloseable { it.write(MARKER) }
            assert manager.inferBinaryName(StandardLocation.CLASS_OUTPUT, nested) == 'pkg.sub.C'
            def notes = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, 'pkg', 'notes.txt', null)
            notes.openOutputStream().withCloseable { it.write(MARKER) }
            assert sink['pkg/notes.txt'] == MARKER
            assert manager.getFileForInput(StandardLocation.CLASS_OUTPUT, 'pkg', 'notes.txt').openInputStream().bytes == MARKER

            def generated = manager.getJavaFileForOutput(StandardLocation.SOURCE_OUTPUT, 'gen.Generated', JavaFileObject.Kind.SOURCE, null)
            generated.openWriter().withCloseable { it.write('class Generated {}') }
            assert !new File('gen/Generated.java').exists()
            assert manager.getJavaFileForInput(StandardLocation.SOURCE_OUTPUT, 'gen.Generated', JavaFileObject.Kind.SOURCE)
                    .getCharContent(true).toString().contains('Generated')

            def stub = new MemClassFileObject('stub.Stub', null)
            def discarding = new InMemoryJavaFileManager(standard, sink, [stub] as Set)
            def hidden = discarding.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, 'stub.Stub', JavaFileObject.Kind.CLASS, stub)
            hidden.openOutputStream().withCloseable { it.write(MARKER) }
            assert !sink.containsKey('stub/Stub.class')

            assert again.openInputStream().bytes == MARKER
            assert again.delete()
            assert manager.getClassBytes('pkg.A') == null
            assert !sink.containsKey('pkg/A.class')

            manager.flush()
            manager.close()
            assert manager.getClassBytes('Top') == MARKER
        } finally {
            standard.close()
        }
    }

    // GROOVY-12711
    @Test
    void testNonClassOutputIsForwarded() {
        def compiler = ToolProvider.systemJavaCompiler
        assert compiler != null
        def standard = compiler.getStandardFileManager(null, Locale.ENGLISH, Charset.forName('UTF-8'))
        def manager = new InMemoryJavaFileManager(standard)
        def source = File.createTempFile('mem-fwd', '.java')
        try {
            source.text = 'public class MemFwd {}\n'
            def javaFile = standard.getJavaFileObjects(source).iterator().next()
            assert manager.isSameFile(javaFile, javaFile)
            assert !manager.contains(StandardLocation.CLASS_OUTPUT, javaFile)
            assert manager.getJavaFileForInput(StandardLocation.CLASS_PATH, 'no.such.Type', JavaFileObject.Kind.CLASS) == null
            assert manager.hasLocation(StandardLocation.CLASS_PATH) == standard.hasLocation(StandardLocation.CLASS_PATH)
            def remaining = ['left'].iterator()
            assert !manager.handleOption('-not-a-javac-option', remaining)
            assert remaining.next() == 'left'

            def output = File.createTempDir('mem-fwd-out', '')
            try {
                standard.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(output))
                def sourceOut = manager.getJavaFileForOutput(
                        StandardLocation.SOURCE_OUTPUT, 'pkg.Note', JavaFileObject.Kind.SOURCE, null)
                sourceOut.openOutputStream().withCloseable { it.write('// note'.bytes) }
                assert !new File(output, 'pkg/Note.java').exists()
                assert sourceOut.getCharContent(true).toString().contains('note')
                assert manager.getClassBytes().isEmpty()

                def text = manager.getFileForOutput(StandardLocation.SOURCE_OUTPUT, 'pkg', 'readme.txt', null)
                text.openOutputStream().withCloseable { it.write('hi'.bytes) }
                assert new File(output, 'pkg/readme.txt').isFile()
            } finally {
                output.deleteDir()
            }

            def unnamed = manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, null, 'E.class', null)
            assert manager.inferBinaryName(StandardLocation.CLASS_OUTPUT, unnamed) == 'E'
            shouldFail(IOException) {
                manager.getFileForOutput(StandardLocation.CLASS_OUTPUT, 'pkg', null, null)
            }
            assert !manager.isSameFile(javaFile, unnamed)
            assert !manager.contains(StandardLocation.CLASS_PATH, javaFile)
        } finally {
            source.delete()
            standard.close()
        }
    }

    // GROOVY-12711
    @Test
    void testFileObjectIdentityAndEmptyPublish() {
        def sink = new LinkedHashMap<String, byte[]>()
        def first = new MemClassFileObject('pkg.A', sink)
        def second = new MemClassFileObject('pkg.A', sink)
        def other = new MemClassFileObject('pkg.B', null)
        assert first == second
        assert first.hashCode() == second.hashCode()
        assert first != other
        assert first != 'pkg.A'
        assert first.toString().contains('pkg.A')
        assert first.binaryName == 'pkg.A'

        sink['pkg/A.class'] = MARKER
        first.openOutputStream().close()
        assert !sink.containsKey('pkg/A.class')
        assert other.delete()
    }

    // GROOVY-12711
    @Test
    void testManagerRequiresADelegate() {
        def thrown = shouldFail(NullPointerException) {
            new InMemoryJavaFileManager(null)
        }
        assert thrown.message.contains('fileManager')
    }

    private static JavaFileObject write(InMemoryJavaFileManager manager, String binaryName) {
        JavaFileObject file = manager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, binaryName, JavaFileObject.Kind.CLASS, null)
        file.openOutputStream().withCloseable { it.write(MARKER) }
        file
    }

    private static List<String> listed(InMemoryJavaFileManager manager, String packageName, boolean recurse) {
        manager.list(StandardLocation.CLASS_OUTPUT, packageName, EnumSet.of(JavaFileObject.Kind.CLASS), recurse)
                .collect { manager.inferBinaryName(StandardLocation.CLASS_OUTPUT, it) }
    }
}

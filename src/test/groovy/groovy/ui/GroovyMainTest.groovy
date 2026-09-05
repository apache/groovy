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
package groovy.ui

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assumptions.assumeTrue

final class GroovyMainTest {

    private baos = new ByteArrayOutputStream()
    private ps = new PrintStream(baos)

    @Test
    void testHelp() {
        String[] args = ['-h']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('Usage: groovy')
        ['-a', '-c', '-d', '-e', '-h', '-i', '-l', '-n', '-p', '-v'].each{
            assert out.contains(it)
        }
    }

    @Test
    void testVersion() {
        String[] args = ['-v']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('Groovy Version:')
        assert out.contains('JVM:')
    }

    @Test
    void testNoArgs() {
        String[] args = []
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: neither -e or filename provided')
    }

    @Test
    void testAttemptToRunJavaFile() {
        String[] args = ['abc.java']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: cannot compile file with .java extension: abc.java')
    }

    /**
     * GROOVY-1512: Add support for begin() and end() methods when processing files by line with -a -ne
     */
    @Test
    void testAandNEparametersWithBeginEndFunctions() {
        def originalErr = System.err
        System.setErr(ps)
        def tempFile = null
        try {
            tempFile = File.createTempFile("groovy-ui-GroovyMainTest-testAandNEparametersWithBeginEndFunctions", "txt")
            tempFile.text = "dummy text\n" * 10
            String[] args = ['-a', '-ne', 'def begin() { nb = 0 }; def end() { System.err.println nb }; nb++', tempFile.absolutePath]
            GroovyMain.main(args)
            def out = baos.toString()
            assert out.contains('10')
        } finally {
            System.setErr(originalErr)
            tempFile?.delete()
        }
    }

    /**
     * GROOVY-6561 : Correct handling of scripts from a URI.
     * GROOVY-1642 : Enable a script to get its URI by annotating a field.
     */
    @Test
    void testURISource() {
        def tempFile = File.createTempFile("groovy-ui-GroovyMainTest-testURISource", ".groovy")
        tempFile.text = """
@groovy.transform.SourceURI def myURI

assert myURI instanceof java.net.URI

print myURI
"""
        tempFile.deleteOnExit()

        def oldOut = System.out
        System.setOut(ps)
        try {
            String[] args = [tempFile.toURI().toString()]
            GroovyMain.main(args)
            def out = baos.toString().trim()
            assert out == tempFile.toURI().toString()
        } finally {
            System.setOut(oldOut)
        }
    }

    // Gotta use configscript for this because : separated paths can't have : in them
    // and GroovyMain ignores -cp.
    @Test
    void testURIClasspath() {
        def tempDir1 = new File("build/tmp/GroovyMainTest1")
        tempDir1.mkdirs()
        def interfaceFile = File.createTempFile("GroovyMainTestInterface", ".groovy", tempDir1)
        interfaceFile.deleteOnExit()
        def tempDir2 = new File("build/tmp/GroovyMainTest2")
        tempDir2.mkdirs()
        def concreteFile = File.createTempFile("GroovyMainTestConcrete", ".groovy", tempDir2)
        concreteFile.deleteOnExit()

        try {
            // Create the interface
            def interfaceName = interfaceFile.name - ".groovy"
            interfaceFile.write "interface $interfaceName { }\n"

            // Create a concrete class which implements the interface
            def concreteName = concreteFile.name - ".groovy"
            concreteFile.write """class MyConcreteClass implements $interfaceName { }
assert new MyConcreteClass() != null"""

            def tempDir = new File("build/tmp/GroovyMainTest3")
            tempDir.mkdirs()
            def configScriptFile = File.createTempFile("config", ".groovy", tempDir)
            configScriptFile.deleteOnExit()
            configScriptFile.text = "configuration.classpath << '${interfaceFile.parentFile.toURI()}'"

            String[] args = ["--configscript", configScriptFile.path, concreteFile.toURI()]
            GroovyMain.main(args)
        } finally {
            interfaceFile.delete()
            concreteFile.delete()
        }
    }

    // GROOVY-10483
    @Test
    void testSourceEncoding() {
        def configScript = File.createTempFile('config', '.groovy')
        def sourceCoding = System.setProperty('groovy.source.encoding', 'US-ASCII')
        try {
            configScript.text = 'assert configuration.sourceEncoding == "US-ASCII"'
            GroovyMain.main('--configscript', configScript.path, '-e', '42')
        } finally {
            if (sourceCoding) {
                System.setProperty('groovy.source.encoding', sourceCoding)
            } else {
                System.clearProperty('groovy.source.encoding')
            }
            configScript.delete()
        }
    }

    @Test
    void testInPlaceEditWithoutBackupExtensionStaysInSourceDirectory() {
        def dir = File.createTempDir('groovy-main-inplace-')
        def file = new File(dir, 'input.txt')
        file.text = 'hello\nworld\n'
        try {
            GroovyMain.main('-i', '-p', '-e', 'line.toUpperCase()', file.absolutePath)
            assert file.text.normalize() == 'HELLO\nWORLD\n'
            def leftovers = dir.listFiles()?.findAll { it.name.startsWith('groovy_') && it.name.endsWith('.tmp') } ?: []
            assert leftovers.isEmpty() : leftovers
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    @Test
    void testProcessFileFailureLeavesUnnamedSibling() {
        def dir = File.createTempDir('groovy-main-inplace-fail-')
        def file = new File(dir, 'input.txt')
        file.text = 'hello\n'
        def main = new GroovyMain()
        main.@editFiles = true
        main.@backupExtension = ''
        def script = new GroovyShell().parse('throw new RuntimeException("boom")')
        def method = GroovyMain.getDeclaredMethod('processFile', groovy.lang.Script, File)
        method.accessible = true
        try {
            def thrown = shouldFail(java.lang.reflect.InvocationTargetException) {
                method.invoke(main, script, file)
            }
            assert thrown.cause instanceof RuntimeException
            def backups = dir.listFiles()?.findAll { it.name.startsWith('groovy_') && it.name.endsWith('.tmp') } ?: []
            assert backups.size() == 1
            assert backups[0].text == 'hello\n'
            assert backups[0].parentFile.canonicalFile == dir.canonicalFile
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    private static boolean posix() {
        FileSystems.default.supportedFileAttributeViews().contains('posix')
    }

    private static String modeOf(File f) {
        PosixFilePermissions.toString(
            Files.getPosixFilePermissions(f.toPath()))
    }

    private static void setMode(File f, String mode) {
        Files.setPosixFilePermissions(f.toPath(),
            PosixFilePermissions.fromString(mode))
    }

    // GROOVY-12337
    @Test
    void testInPlaceEditPreservesRestrictivePermissions() {
        assumeTrue(posix())
        def dir = File.createTempDir('groovy-main-inplace-perm-')
        def file = new File(dir, 'secret.txt')
        file.text = 'hello\n'
        setMode(file, 'rw-------')
        try {
            GroovyMain.main('-i', '-p', '-e', 'line.toUpperCase()', file.absolutePath)

            assert file.text.normalize() == 'HELLO\n'
            // a rewrite must not widen access to content that was deliberately restricted
            assert modeOf(file) == 'rw-------'
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    // GROOVY-12337
    @Test
    void testInPlaceEditWithBackupExtensionPreservesPermissions() {
        assumeTrue(posix())
        def dir = File.createTempDir('groovy-main-inplace-bakperm-')
        def file = new File(dir, 'secret.txt')
        file.text = 'hello\n'
        setMode(file, 'rw-------')
        try {
            GroovyMain.main('-i', '.bak', '-p', '-e', 'line.toUpperCase()', file.absolutePath)

            assert modeOf(file) == 'rw-------'
            // the moved-aside original keeps its own mode too
            assert modeOf(new File(dir, 'secret.txt.bak')) == 'rw-------'
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    // GROOVY-12337
    @Test
    void testInPlaceEditPreservesAnExecutableMode() {
        assumeTrue(posix())
        def dir = File.createTempDir('groovy-main-inplace-exec-')
        def file = new File(dir, 'script.sh')
        file.text = 'echo hi\n'
        setMode(file, 'rwxr-x---')
        try {
            GroovyMain.main('-i', '-p', '-e', 'line.toUpperCase()', file.absolutePath)

            // preserving means preserving, not just restricting
            assert modeOf(file) == 'rwxr-x---'
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    @Test
    void testInPlaceEditWithBackupExtension() {
        def dir = File.createTempDir('groovy-main-inplace-bak-')
        def file = new File(dir, 'input.txt')
        file.text = 'hello\n'
        try {
            GroovyMain.main('-i', '.bak', '-p', '-e', 'line.toUpperCase()', file.absolutePath)
            assert file.text.normalize() == 'HELLO\n'
            def backup = new File(dir, 'input.txt.bak')
            assert backup.exists()
            assert backup.text == 'hello\n'
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    @Test
    void testMoveAsideForInPlaceEditCreatesSiblingNotSystemTemp() {
        def dir = File.createTempDir('groovy-main-move-aside-')
        def file = new File(dir, 'source.txt')
        file.text = 'payload'
        def tmpDir = new File(System.getProperty('java.io.tmpdir')).canonicalFile
        try {
            def method = GroovyMain.getDeclaredMethod('moveAsideForInPlaceEdit', File)
            method.accessible = true
            def backup = (File) method.invoke(null, file)
            assert !file.exists()
            assert backup.exists()
            assert backup.text == 'payload'
            assert backup.parentFile.canonicalFile == dir.canonicalFile
            assert backup.name.startsWith('groovy_')
            assert backup.name.endsWith('.tmp')
            if (dir.canonicalFile != tmpDir) {
                assert backup.canonicalFile.parentFile != tmpDir
            }
        } finally {
            dir.listFiles()?.each { it.delete() }
            dir.delete()
        }
    }

    @Test @Disabled('current xstream causes illegal access errors on JDK9+ - skip on those JDK versions, get coverage on older versions')
    void testGroovyASTDump() {
        def temporaryDirectory = new File("build/tmp/testGroovyXMLAstGeneration/")
        temporaryDirectory.mkdirs()

        def scriptFile = new File(temporaryDirectory, "Script1.groovy")
        scriptFile.deleteOnExit()

        scriptFile << "assert 1 + 1 == 2"

        try {
            System.setProperty('groovy.ast', 'xml')

            GroovyMain.main([scriptFile.absolutePath] as String[])

            assert new File(temporaryDirectory, scriptFile.name + '.xml').exists()
        } finally {
            temporaryDirectory.deleteDir()

            System.clearProperty('groovy.ast')
        }
    }
}

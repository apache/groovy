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

import static groovy.test.GroovyAssert.isAtLeastJdk

class GroovyMainTest extends GroovyTestCase {
    private baos = new ByteArrayOutputStream()
    private ps = new PrintStream(baos)

    void testHelp() {
        String[] args = ['-h']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('Usage: groovy')
        ['-a', '-c', '-d', '-e', '-h', '-i', '-l', '-n', '-p', '-v'].each{
            assert out.contains(it)
        }
    }

    void testVersion() {
        String[] args = ['-v']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('Groovy Version:')
        assert out.contains('JVM:')
    }

    void testNoArgs() {
        String[] args = []
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: neither -e or filename provided')
    }

    void testAttemptToRunJavaFile() {
        String[] args = ['abc.java']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: cannot compile file with .java extension: abc.java')
    }

    /**
     * GROOVY-1512: Add support for begin() and end() methods when processing files by line with -a -ne
     */
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
     * GROOVY-1642 : Enable a script to get it's URI by annotating a field.
     */
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
    void testURIClasspath() {
        def tempDir1 = new File("target/tmp/GroovyMainTest1")
        tempDir1.mkdirs()
        def interfaceFile = File.createTempFile("GroovyMainTestInterface", ".groovy", tempDir1)
        interfaceFile.deleteOnExit()
        def tempDir2 = new File("target/tmp/GroovyMainTest2")
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

            def tempDir = new File("target/tmp/GroovyMainTest3")
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

    void testGroovyASTDump() {
        // current xstream causes illegal access errors on JDK9+ - skip on those JDK versions, get coverage on older versions
        if (isAtLeastJdk('9.0')) return

        def temporaryDirectory = new File("target/tmp/testGroovyXMLAstGeneration/")
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

    // This works for a URL in the classpath, but there isn't a way to do this from the command line.
//    public void testConfigURIClasspath() {
//        URI baseURI = new URI("https://raw.github.com/jimwhite/groovy-snippets/master/GROOVY-6451/")
//        GroovyCodeSource codeSource = new GroovyCodeSource(baseURI.resolve("run_from_uri_test.groovy"))
//        def shell = new GroovyShell()
//        shell.classLoader.addURL(baseURI.toURL())
//        // We're testing whether this fails:
//        def script = shell.parse(codeSource)
//        script.run()
//    }


}

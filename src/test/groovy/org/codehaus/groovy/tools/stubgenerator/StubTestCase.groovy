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
package org.codehaus.groovy.tools.stubgenerator

import com.thoughtworks.qdox.JavaProjectBuilder
import com.thoughtworks.qdox.model.JavaClass
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

import groovy.test.GroovyTestCase

import static groovy.io.FileType.*

/**
 * Base class for all the stub generator test samples.
 * <br><br>
 *
 * If you want to create a new test, you have to create a class extending <code>StubTestCase</code>.
 * Your subclass has to implement <code>void verifyStubs()</code>.
 * <p>
 * All the sample Java and Groovy sources to be joint-compiled must be either:
 * <ul>
 * <li>put in <code>src/test-resources/stubgenerator</code>,
 * under a directory whose name is the name of the subclass you created, with the first letter lowercase,
 * and the suffix Test removed.
 * Example: for the test <code>CircularLanguageReferenceTest</code>,
 * you should put your resources in <code>src/test-resources/stubgenerator/circularLanguageReference</code>.</li>
 * <li>provided via the <code>Map<String, String> provideSources()</code> method. Example: see one of the
 * existing tests which use this approach, e.g. <code>DuplicateMethodAdditionInStubsTest</code>.</li>
 * </ul>
 * From within the <code>verifyStubs()</code> method, you can make various assertions on the stubs.
 * QDox is used for parsing the Java sources (both the generated stub Java sources, as well as the original Java source,
 * but not the Groovy sources).
 * The execution of the <code>verifyStubs()</code> method is done with the <code>QDoxCategory</code> applied,
 * which provides various useful shortcuts for quickly checking the structure of your stubs.
 * <p>
 * Please have a look at the existing samples to see what kind of asserts can be done.
 */
abstract class StubTestCase extends GroovyTestCase {

    protected final File targetDir = createTempDirectory()
    protected final File stubDir   = createTempDirectory()

    protected File sourceRootPath

    protected JavaProjectBuilder qdox = new JavaProjectBuilder()

    protected GroovyClassLoader loader
    protected CompilerConfiguration config = new CompilerConfiguration()

    protected boolean debug = false;
    protected boolean delete = true;

    /**
     * Prepares the target and stub directories.
     */
    @Override
    protected void setUp() {
        super.setUp()
        if (debug) {
            println """\
                Stub generator test [${this.class.name}]
                  target directory: ${targetDir.absolutePath}
                    stub directory: ${stubDir.absolutePath}
            """.stripIndent()
        }
        assert targetDir.exists()
        assert stubDir.exists()
    }

    /**
     * Deletes the temporary directories.
     */
    @Override
    protected void tearDown() {
        if (delete) {
            if (debug) println "Deleting temporary folders"
            targetDir.deleteDir()
            stubDir.deleteDir()
        }
        loader.close()
        loader = null
        config = null
    }

    /**
     * Called before a test run, for initialization purpose.
     * For instance, a subclass could override <code>init()</code>
     * to set the <code>debug</code> flag to true,
     * to see the content of the sources and stubs.
     * <br><br>
     * Add the following method to your test to enable printing of debug information
     * and output of sources and stubs:
     * <pre><code>
     *     protected void init() {
     *         debug = true
     *     }
     * </code></pre>
     */
    protected void init() {
    }

    /**
     * @return the folder containing the sample Groovy and Java sources for the test
     */
    protected File sourcesRootPath() {
        def nameWithoutTest = this.class.simpleName - 'Test'
        def folder = nameWithoutTest[0].toLowerCase() + nameWithoutTest[1..-1]

        def testDirectory = new File(StubTestCase.class.classLoader.getResource('.').toURI())
        // for Ant build
        def result = new File(testDirectory, "../../src/test-resources/stubgenerator/${folder}")
        if (!result.exists()) {
            // For Gradle build
            result = new File("build/resources/test/stubgenerator/${folder}")
        }

        result
    }

    /**
     * Sole JUnit test method which will delegate to the <code>verifyStubs()</code> method
     * in the subclasses of <code>StubTestCase</code>.
     */
    void testRun() {
        init()
        configure()

        sourceRootPath = sourcesRootPath()
        if (!sourceRootPath && !sourceRootPath.exists()) {
            fail "Path doesn't exist: ${sourceRootPath}"
        }

        def sources = collectSources(sourceRootPath)

        if (debug) {
            println ">>> Sources to be compiled:"
            sources.each { File sourceFile ->
                println " -> " + sourceFile.name
                println sourceFile.text
            }
        }

        Throwable compileError = null
        try {
            compile(sources)

            // use QDox for parsing the Java stubs and Java sources
            qdox.addSourceTree(sourceRootPath)
            qdox.addSourceTree(stubDir)

            if (debug) {
                println ">>> Stubs generated"
                stubDir.eachFileRecurse(FILES) { File stubFile ->
                    if (stubFile.name ==~ /.*(\.groovy|\.java)/) {
                        println " -> " + stubFile.name
                        println stubFile.text
                    }
                }

                println ">>> QDox canonical sources"
                qdox.classes.each { JavaClass jc ->
                    println " -> " + jc.fullyQualifiedName
                    println jc.source
                }

                println "Verifying the stubs"
            }
        } catch (Throwable t) {
            compileError = t
        } finally {
            try {
                use (QDoxCategory) {
                    verifyStubs()
                }
            } catch (AssertionError | RuntimeException e) {
                if (compileError) {
                    println "Unable to verify stubs: $e.message\nPerhaps due to earlier error?"
                    throw compileError
                }
                throw e
            }
            if (sourceRootPath.absolutePath =~ 'stubgentests') {
                sourceRootPath.deleteDir()
            }
        }
        if (compileError) throw compileError
    }

    /**
     * Collect all the Groovy and Java sources to be joint compiled.
     *
     * @param path the root path where to find the sources
     * @return a list of files
     */
    protected List<File> collectSources(File path) {
        def sources = []
        path.eachFileRecurse(FILES) { sourceFile ->
            if (sourceFile.name ==~ /.*(\.groovy|\.java)/) {
                // add all the source files for the compiler to compile
                sources << sourceFile
            }
        }
        return sources
    }

    /**
     * Launch the actual compilation -- hence launching the stub generator as well.
     *
     * @param sources the sources to be compiled
     */
    protected void compile(List<File> sources) {
        loader = new GroovyClassLoader(this.class.classLoader).tap {
            addURL(this.class.location)
            addURL(GroovyTestCase.location)
            addURL(junit.framework.TestCase.location)
        }
        def cu = new JavaAwareCompilationUnit(config, loader)
        cu.addSources(sources as File[])
        try {
            cu.compile()
            if (debug) println "Sources compiled successfully"
        } catch (CompilationFailedException any) {
            handleCompilationFailure(any)
        }
    }

    /**
     * Handle any compilation error that may have happened.
     *
     * @param any the compilation exception
     */
    protected void handleCompilationFailure(CompilationFailedException any) {
        def stringWriter = new StringWriter()
        any.printStackTrace(new PrintWriter(stringWriter))
        fail "Compilation failed for stub generator test:\n${stringWriter.toString()}"
    }

    /**
     * Create a compiler configuration to define a place to store the stubs and compiled classes
     */
    protected void configure() {
        config = new CompilerConfiguration()
        config.with {
            targetDirectory = targetDir
            jointCompilationOptions = [stubDir: stubDir, keepStubs: true]
        }
    }

    /**
     * All tests must implement this method, for doing
     */
    protected abstract void verifyStubs()

    /**
     * Method providing a useful shortcut for the subclasses, so that you can use <code>classes</code>
     * from within the <code>verifyStubs()</code> method, to access all the stubs.
     *
     * @return an array of QDox' <code>JavaClass</code>es.
     */
    protected JavaClass[] getClasses() {
        qdox.classes as JavaClass[]
    }

    /**
     * Retrieves the source code of the Java stub of the fully qualified name class in argument.
     * Example:
     * <pre><code>
     * assert stubJavaSourceFor('com.foo.Bar').contains(...)
     * </code></pre>
     *
     * @param fqn the fully qualified name of the class
     * @return the source code of the class
     */
    protected String stubJavaSourceFor(String fqn) {
        new File(stubDir, fqn.replace('.' as char, File.separatorChar) + '.java').text
    }

    /**
     * Create a temporary directory.
     *
     * @return the created temporary directory
     * @throws IOException if a temporary directory could not be created
     */
    protected static File createTempDirectory() throws IOException {
        File.createTempDir("stubgentests", Long.toString(System.currentTimeMillis()))
    }

    /**
     * Create sub-folders used in relativeFilePath under the specified directory
     *
     * @throws IOException if a sub-directory could not be created
     */
    protected static void createNecessaryPackageDirs(File path, String relativeFilePath) {
        def index = relativeFilePath.lastIndexOf('/')

        if (index < 0) return

        def relativeDirectories = relativeFilePath.substring(0, index)

        def tmpPath = path.absolutePath

        relativeDirectories.split('/').each {
            if (!tmpPath.endsWith(File.separator)) {
                tmpPath = tmpPath + File.separator
            }
            File newDir = new File(tmpPath + it)
            if (!newDir.exists()) {
                if (!(newDir.mkdir())) {
                    throw new IOException("Impossible to create package directory: ${newDir.absolutePath}")
                }
            }
            tmpPath = newDir.absolutePath
        }
    }
}


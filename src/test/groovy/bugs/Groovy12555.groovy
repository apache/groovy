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
package bugs

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT_PACKAGE_FIRST

final class Groovy12555 {

    private static final Map<String, String> SOURCES = [
        'Factory.groovy': 'class Factory { static String hello() { "mine" } }',
        'Caller.groovy': 'class Caller { def run() { Factory.hello() } }'
    ]

    private File dir

    @BeforeEach
    void setUp() {
        dir = File.createTempDir()
    }

    @AfterEach
    void tearDown() {
        dir.deleteDir()
    }

    /**
     * Compiles the sources together, then compiles {@code Caller} alone against {@code Factory}, either compiled,
     * as an incremental build has it, or as a source on the class path, and returns the text of {@code Caller}
     * from both compilations.
     */
    private List<String> compileCallerBothWays(Map<String, Boolean> options, boolean factoryCompiled) {
        File sourceDir = new File(dir, 'src')
        Map<String, File> files = SOURCES.collectEntries { name, text -> [name, write(new File(sourceDir, name), text)] }
        CompilationUnit together = compile(new File(dir, 'together'), files.values() as List, null, options)
        File first = new File(dir, 'first')
        if (factoryCompiled) {
            compile(first, [files['Factory.groovy']], null, options)
        } else {
            write(new File(first, 'Factory.groovy'), SOURCES['Factory.groovy'])
        }
        CompilationUnit separate = compile(new File(dir, 'separate'), [files['Caller.groovy']], first, options)
        [text(together, 'Caller'), text(separate, 'Caller')]
    }

    private static File write(File file, String text) {
        file.parentFile.mkdirs()
        file.text = text
        file
    }

    private static CompilationUnit compile(File targetDir, List<File> sources, File classpath, Map<String, Boolean> options) {
        def config = new CompilerConfiguration(targetDirectory: targetDir)
        config.optimizationOptions.putAll(options)
        def loader = new GroovyClassLoader(Groovy12555.classLoader, config)
        if (classpath) loader.addClasspath(classpath.absolutePath)
        def unit = new CompilationUnit(config, null, loader)
        unit.addSources(sources as File[])
        unit.compile()
        unit
    }

    private static String text(CompilationUnit unit, String className) {
        def writer = new StringWriter()
        new ClassReader(unit.classes.find { it.name == className }.bytes)
                .accept(new TraceClassVisitor(new PrintWriter(writer)), 0)
        writer.toString()
    }

    // a class of the default package refers to a compiled class of the default package rather
    // than to a default import of the same name, as it does when both are compiled together
    @Test
    void testCompiledDefaultPackageClassBeforeDefaultImports() {
        def (together, separate) = compileCallerBothWays([(DEFAULT_PACKAGE_FIRST): true], true)
        assert together == separate
        assert !separate.contains('groovy/util/Factory')
    }

    // the option is on unless it is set to false
    @Test
    void testDefaultPackageFirstByDefault() {
        def (together, separate) = compileCallerBothWays([:], true)
        assert together == separate
        assert !separate.contains('groovy/util/Factory')
    }

    // with the option off, the default import comes before a compiled class of the default package, as it used to
    @Test
    void testCompiledDefaultPackageClassAfterDefaultImportsWhenOff() {
        def (together, separate) = compileCallerBothWays([(DEFAULT_PACKAGE_FIRST): false], true)
        assert !together.contains('groovy/util/Factory')
        assert separate.contains('groovy/util/Factory')
    }

    // a source of the default package on the class path is compiled and referred to rather than a default import
    @Test
    void testDefaultPackageSourceBeforeDefaultImports() {
        def (together, separate) = compileCallerBothWays([(DEFAULT_PACKAGE_FIRST): true], false)
        assert together == separate
        assert !separate.contains('groovy/util/Factory')
    }

    @Test
    void testDefaultPackageSourceAfterDefaultImportsWhenOff() {
        def (together, separate) = compileCallerBothWays([(DEFAULT_PACKAGE_FIRST): false], false)
        assert !together.contains('groovy/util/Factory')
        assert separate.contains('groovy/util/Factory')
    }
}

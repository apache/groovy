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

final class Groovy12552 {

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
     * Compiles all sources together, then compiles {@code recompiled} against the other sources compiled,
     * as an incremental build does, and returns the text of each recompiled class from both compilations.
     */
    private Map<String, List<String>> compileBothWays(Map<String, String> sources, List<String> recompiled) {
        File sourceDir = new File(dir, 'src')
        Map<String, File> files = sources.collectEntries { name, text ->
            File file = new File(sourceDir, name)
            file.parentFile.mkdirs()
            file.text = text
            [name, file]
        }
        CompilationUnit together = compile(new File(dir, 'together'), files.values() as List, null)
        File first = new File(dir, 'first')
        compile(first, files.findAll { !recompiled.contains(it.key) }.values() as List, null)
        CompilationUnit separate = compile(new File(dir, 'separate'), recompiled.collect { files[it] }, first)
        separate.classes*.name.collectEntries { name ->
            [name, [text(together, name), text(separate, name)]]
        }
    }

    private static CompilationUnit compile(File targetDir, List<File> sources, File classpath) {
        def loader = new GroovyClassLoader(Groovy12552.classLoader)
        if (classpath) loader.addClasspath(classpath.absolutePath)
        def unit = new CompilationUnit(new CompilerConfiguration(targetDirectory: targetDir), null, loader)
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

    // a property's own accessors are called rather than the interface methods they implement,
    // whether or not the property's class is compiled together with the caller
    @Test
    void testPropertyAccessorImplementingInterfaceMethod() {
        def texts = compileBothWays([
            'p/Named.groovy': 'package p; interface Named { void setName(String name); String getName() }',
            'p/Base.groovy': 'package p; abstract class Base implements Named { String name }',
            'p/Impl.groovy': 'package p; class Impl extends Base {}',
            'p/Caller.groovy': '''package p
                @groovy.transform.CompileStatic
                class Caller {
                    def run() {
                        Base b = new Impl()
                        b.name = 'x'
                        b.setName('y')
                        b.getName()
                    }
                }
            '''
        ], ['p/Caller.groovy'])
        for (text in texts['p.Caller']) {
            assert !text.contains('INVOKEINTERFACE p/Named')
            assert text.count('INVOKEVIRTUAL p/Impl.setName') == 2
            assert text.contains('INVOKEVIRTUAL p/Impl.getName')
        }
    }
}

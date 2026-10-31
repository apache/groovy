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
package org.codehaus.groovy.transform.traitx

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor

final class Groovy10932 {

    private static List<File> write(Map<String, String> sources, File dir) {
        sources.collect { name, text ->
            new File(dir, name).tap { write(text) }
        }
    }

    private static CompilationUnit compile(File targetDir, List<File> sources, File classpath = null) {
        def loader = new GroovyClassLoader(Groovy10932.classLoader)
        if (classpath) loader.addClasspath(classpath.absolutePath)
        def unit = new CompilationUnit(new CompilerConfiguration(targetDirectory: targetDir), null, loader)
        unit.addSources(sources as File[])
        unit.compile()
        unit
    }

    private static String textify(CompilationUnit unit, String className) {
        def writer = new StringWriter()
        new ClassReader(unit.classes.find { it.name == className }.bytes)
                .accept(new TraceClassVisitor(new PrintWriter(writer)), 0)
        writer.toString()
    }

    // precompiled helpers are read from bytecode, so a helper that refers to
    // a class being recompiled does not need that class on the class path
    @Test
    void testPrecompiledTraitHelperRefersToRecompiledClass() {
        File sourceDir = File.createTempDir()
        File targetDir = File.createTempDir()
        try {
            def sources = write(sourceDir,
                'MyTest.groovy': '''
                    @groovy.transform.CompileStatic
                    class MyTest extends MyTestBase implements WithPluginValidation {
                        def run() { allPlugins.base }
                    }
                ''',
                'MyTestBase.groovy': '''
                    class MyTestBase {
                        def make() { new MyTest() }
                    }
                ''',
                'WithPluginValidation.groovy': '''
                    @groovy.transform.SelfType(MyTestBase)
                    trait WithPluginValidation {
                        final AllPluginsValidation allPlugins = new AllPluginsValidation(this)
                        static class AllPluginsValidation {
                            final MyTestBase base
                            AllPluginsValidation(MyTestBase base) { this.base = base }
                        }
                    }
                ''')
            compile(targetDir, sources)

            // what an incremental build does: remove the stale classes, then recompile their sources
            assert new File(targetDir, 'MyTest.class').delete()
            assert new File(targetDir, 'MyTestBase.class').delete()
            File recompileDir = new File(targetDir, 'recompiled')
            compile(recompileDir, sources.take(2), targetDir)

            assert new File(recompileDir, 'MyTest.class').exists()
        } finally {
            sourceDir.deleteDir()
            targetDir.deleteDir()
        }
    }

    // an implementer of a precompiled trait carries the annotations of the
    // trait's methods as written, not with their default members filled in
    @Test
    void testPrecompiledTraitMethodAnnotations() {
        File sourceDir = File.createTempDir()
        File targetDir = File.createTempDir()
        try {
            def sources = write(sourceDir,
                'Flag.groovy': '''
                    import java.lang.annotation.*
                    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
                    @interface Flag { boolean value() default true; String note() default '' }
                ''',
                'Named.groovy': '''
                    trait Named {
                        @Flag
                        String describe() { 'named' }
                        @Flag(note='x')
                        String explain() { 'explained' }
                    }
                ''',
                'Thing.groovy': '''
                    class Thing implements Named {
                    }
                ''')
            String together = textify(compile(new File(targetDir, 'together'), sources), 'Thing')

            File traitDir = new File(targetDir, 'trait')
            compile(traitDir, sources.take(2))
            String separate = textify(compile(new File(targetDir, 'separate'), sources.drop(2), traitDir), 'Thing')

            for (text in [together, separate]) {
                assert text.contains('@LFlag;()')
                assert text.contains('@LFlag;(note="x")')
                assert !text.contains('value=true')
            }
        } finally {
            sourceDir.deleteDir()
            targetDir.deleteDir()
        }
    }
}

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
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * GROOVY-12548: a closure passed to an extension method whose SAM
 * parameter is {@code Function<? super D, C>} must have {@code it}
 * inferred from the generic receiver, not as {@code Object}.
 */
final class Groovy12548 {

    @TempDir
    File tempDir

    @Test
    void testExtensionMethodClosureParamInferredFromReceiver() {
        File jar = buildExtensionJar()
        def loader = new GroovyClassLoader(Groovy12548.classLoader)
        loader.addURL(jar.toURI().toURL())
        try {
            def result = new GroovyShell(loader).evaluate('''
                @groovy.transform.CompileStatic
                class ExtSite {
                    static Cmd viaExtension(Holder<Set<String>> h) { h.exec { new Cmd(it) } }
                    static Cmd viaStatic(Holder<Set<String>> h) { ExtModule.exec(h) { new Cmd(it) } }
                }
                class Cmd {
                    Collection<String> c
                    Cmd(Collection<String> c) { this.c = c }
                }
                def holder = new Holder<Set<String>>(['a'] as Set)
                [ExtSite.viaExtension(holder).c, ExtSite.viaStatic(holder).c]
            ''')
            assert result == [['a'] as Set, ['a'] as Set]
        } finally {
            loader.close()
        }
    }

    private File buildExtensionJar() {
        File classesDir = new File(tempDir, 'classes')
        def config = new CompilerConfiguration(targetDirectory: classesDir)
        def unit = new CompilationUnit(config)
        unit.addSource('Holder.groovy', '''
            class Holder<D> {
                final D value
                Holder(D value) { this.value = value }
            }
        ''')
        unit.addSource('ExtModule.groovy', '''
            class ExtModule {
                static <D, C> C exec(Holder<D> self, java.util.function.Function<? super D, C> prep) {
                    prep.apply(self.value)
                }
            }
        ''')
        unit.compile()

        File jar = new File(tempDir, 'groovy12548-ext.jar')
        new JarOutputStream(jar.newOutputStream()).withCloseable { stream ->
            classesDir.eachFileRecurse { f ->
                if (f.file) {
                    String path = classesDir.toPath().relativize(f.toPath()).toString().replace(File.separatorChar, '/' as char)
                    stream.putNextEntry(new JarEntry(path))
                    stream << f.bytes
                    stream.closeEntry()
                }
            }
            stream.putNextEntry(new JarEntry(ExtensionModuleScanner.MODULE_META_INF_FILE))
            stream.write('''\
                moduleName=groovy12548-ext
                moduleVersion=1.0
                extensionClasses=ExtModule
            '''.stripIndent().getBytes('UTF-8'))
            stream.closeEntry()
        }
        jar
    }
}

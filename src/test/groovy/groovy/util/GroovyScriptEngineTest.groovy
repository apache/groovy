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
package groovy.util

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

final class GroovyScriptEngineTest {

    @TempDir
    File temporaryFolder

    @Test @Disabled('current xstream causes illegal access errors on JDK9+ - skip on those JDK versions, get coverage on older versions')
    void createASTDumpWhenScriptIsLoadedByName() {
        def scriptFile = new File(temporaryFolder, 'Script1.groovy').tap { createNewFile() }
        scriptFile << "assert 1 + 1 == 2" // the script just has to have _some_ content
        try {
            System.setProperty('groovy.ast', 'xml')

            def clazz = new GroovyScriptEngine([temporaryFolder.toURI().toURL()] as URL[]).loadScriptByName('Script1.groovy')

            assert new File(temporaryFolder, scriptFile.name + '.xml').exists()
            assert clazz != null
        } finally {
            System.clearProperty('groovy.ast')
        }
    }

    @Test
    void whenSystemPropertyIsMissingDontCreateASTDump() {

        def scriptFile = new File(temporaryFolder, 'Script1.groovy').tap { createNewFile() }

        scriptFile << "assert 1 + 1 == 2" // the script just has to have _some_ content

        System.clearProperty('groovy.ast')

        def clazz = new GroovyScriptEngine([temporaryFolder.toURI().toURL()] as URL[]).loadScriptByName('Script1.groovy')
        assert clazz != null

        assert !new File(temporaryFolder, scriptFile.name + '.xml').exists()
    }

    @Test
    void customizersAppliedOncePerClassNode_GROOVY_8402() {
        def scriptFile = new File(temporaryFolder, 'Script1.groovy').tap { createNewFile() }
        scriptFile << '''
            class Foo {}
            assert 1 + 1 == 2
        '''
        def counts = [:].withDefault { 0 }

        def config = new CompilerConfiguration().addCompilationCustomizers(new CompilationCustomizer(CompilePhase.SEMANTIC_ANALYSIS) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                counts[classNode.name]++
            }
        })

        GroovyScriptEngine scriptEngine = new GroovyScriptEngine([temporaryFolder.toURI().toURL()] as URL[])
        scriptEngine.setConfig(config)
        scriptEngine.loadScriptByName('Script1.groovy')
        assert counts['Script1'] == 1
        assert counts['Foo'] == 1
    }
}

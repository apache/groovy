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

    @Test
    void resourceLoaderClosesConnectorConnection() {
        File scriptFile = new File(temporaryFolder, 'Foo.groovy')
        scriptFile.text = 'class Foo {}'

        TrackingURLConnection tracking = null
        ResourceConnector rc = { String name ->
            File f = new File(temporaryFolder, name)
            if (!f.exists()) throw new ResourceException("missing $name")
            tracking = new TrackingURLConnection(f)
            tracking
        }

        GroovyScriptEngine engine = new GroovyScriptEngine(rc)
        engine.config.scriptExtensions = new LinkedHashSet(['groovy'])

        URL url = engine.groovyClassLoader.resourceLoader.loadGroovySource('Foo')
        assert url != null
        assert tracking != null
        assert tracking.inputStreamOpened : 'URLConnection obtained only for its URL must still be closed'
    }

    @Test
    void resourceLoaderUsesCustomConnectorWhenParentIsGroovyClassLoader() {
        new File(temporaryFolder, 'Helper.groovy').text = 'class Helper { def ping() { "pong" } }'

        ResourceConnector rc = { String name ->
            File f = new File(temporaryFolder, name)
            if (!f.exists()) throw new ResourceException("missing $name")
            f.toURI().toURL().openConnection()
        }

        GroovyScriptEngine engine = new GroovyScriptEngine(rc, new GroovyClassLoader())
        Class helper = engine.groovyClassLoader.loadClass('Helper')
        assert helper.newInstance().ping() == 'pong'
    }

    @Test
    void resourceLoaderTriesNextExtensionWhenFirstIsMissing() {
        new File(temporaryFolder, 'Helper.gy').text = 'class Helper { def ping() { "pong" } }'

        GroovyScriptEngine engine = new GroovyScriptEngine([temporaryFolder.toURI().toURL()] as URL[])
        engine.config.scriptExtensions = new LinkedHashSet(['groovy', 'gy'])

        Class helper = engine.groovyClassLoader.loadClass('Helper')
        assert helper.newInstance().ping() == 'pong'
    }

    @Test
    void resourceLoaderRequestsForwardSlashPathsForPackagedClasses() {
        File pkg = new File(temporaryFolder, 'com/example')
        pkg.mkdirs()
        new File(pkg, 'Helper.groovy').text = 'package com.example; class Helper { def ping() { "pong" } }'

        def requested = []
        ResourceConnector rc = { String name ->
            requested << name
            File f = new File(temporaryFolder, name)
            if (!f.exists()) throw new ResourceException("missing $name")
            f.toURI().toURL().openConnection()
        }

        GroovyScriptEngine engine = new GroovyScriptEngine(rc)
        engine.config.scriptExtensions = new LinkedHashSet(['groovy'])

        Class helper = engine.groovyClassLoader.loadClass('com.example.Helper')
        assert helper.newInstance().ping() == 'pong'
        assert requested.contains('com/example/Helper.groovy')
    }

    static final class TrackingURLConnection extends URLConnection {
        boolean inputStreamOpened
        private final File file

        TrackingURLConnection(File file) {
            super(file.toURI().toURL())
            this.file = file
        }

        @Override
        void connect() {
        }

        @Override
        InputStream getInputStream() {
            inputStreamOpened = true
            file.newInputStream()
        }
    }
}

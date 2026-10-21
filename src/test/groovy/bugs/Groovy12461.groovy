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

import groovy.grape.Grape
import groovy.grape.GrapeEngine
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

/**
 * The system properties of {@code @GrabConfig} are only set when grapes are
 * enabled, and are set before the grab runs, so they apply to the engine it creates.
 */
final class Groovy12461 {

    private static final String PROBE = 'groovy.test.grabconfig.probe'

    private static final String SCRIPT = """
        @GrabConfig(systemProperties = '$PROBE=set-by-grabconfig')
        @Grab('org.example:not-fetched:1.0')
        import java.util.List
        System.getProperty('$PROBE')
    """

    private GrapeEngine savedInstance
    private boolean savedEnableGrapes
    private String savedImpl

    @BeforeEach
    void setUp() {
        savedInstance = Grape.instance
        savedEnableGrapes = Grape.enableGrapes
        savedImpl = System.getProperty('groovy.grape.impl')
        System.clearProperty(PROBE)
    }

    @AfterEach
    void tearDown() {
        Grape.instance = savedInstance
        Grape.enableGrapes = savedEnableGrapes
        if (savedImpl == null) System.clearProperty('groovy.grape.impl')
        else System.setProperty('groovy.grape.impl', savedImpl)
        System.clearProperty(PROBE)
    }

    @Test
    void testPropertiesSetWhenGrabRuns() {
        Grape.instance = new NoOpGrapeEngine()
        assert new GroovyShell().evaluate(SCRIPT) == 'set-by-grabconfig'
    }

    @Test
    void testPropertiesNotSetWhenGrapesDisabled() {
        Grape.instance = new NoOpGrapeEngine()
        Grape.enableGrapes = false
        assert new GroovyShell().evaluate(SCRIPT) == null
    }

    @Test
    void testPropertiesSetBeforeEngineIsCreated(@TempDir Path services) {
        // compiled while an engine exists, then run where the grab creates the engine
        Grape.instance = new NoOpGrapeEngine()
        Class<? extends Script> scriptClass = new GroovyClassLoader().parseClass(SCRIPT)
        Grape.instance = null
        Path config = services.resolve('META-INF/services/' + GrapeEngine.name)
        Files.createDirectories(config.parent)
        Files.writeString(config, RecordingGrapeEngine.name)
        System.setProperty('groovy.grape.impl', RecordingGrapeEngine.name)
        Thread thread = Thread.currentThread()
        ClassLoader savedLoader = thread.contextClassLoader
        thread.contextClassLoader = new URLClassLoader([services.toUri().toURL()] as URL[], getClass().classLoader)
        try {
            assert scriptClass.getDeclaredConstructor().newInstance().run() == 'set-by-grabconfig'
            assert Grape.instance instanceof RecordingGrapeEngine
            assert RecordingGrapeEngine.seenAtCreation == 'set-by-grabconfig'
        } finally {
            thread.contextClassLoader = savedLoader
        }
    }

    static class RecordingGrapeEngine extends NoOpGrapeEngine {
        static String seenAtCreation

        RecordingGrapeEngine() {
            seenAtCreation = System.getProperty(PROBE)
        }
    }

    private static class NoOpGrapeEngine implements GrapeEngine {
        Object grab(String endorsedModule) { null }
        Object grab(Map args) { null }
        Object grab(Map args, Map... dependencies) { null }
        Map<String, Map<String, List<String>>> enumerateGrapes() { [:] }
        URI[] resolve(Map args, Map... dependencies) { new URI[0] }
        URI[] resolve(Map args, List depsInfo, Map... dependencies) { new URI[0] }
        Map[] listDependencies(ClassLoader classLoader) { new Map[0] }
        void addResolver(Map<String, Object> args) { }
    }
}

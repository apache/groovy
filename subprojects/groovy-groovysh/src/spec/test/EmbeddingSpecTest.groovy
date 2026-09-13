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
package org.apache.groovy.groovysh.spec

import org.apache.groovy.groovysh.GroovyshOptions
import org.apache.groovy.groovysh.Main
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.jupiter.api.Test

/**
 * Executable examples for embedding groovysh. Included from
 * {@code groovysh.adoc}.
 */
class EmbeddingSpecTest {

    @Test
    void compilerConfigurationOnTheEngine() {
        // tag::embed_engine_config[]
        def imports = new ImportCustomizer()
        imports.addStarImports('java.util.concurrent.atomic')
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(imports)

        def engine = new GroovyEngine(config)
        assert engine.execute('new AtomicInteger(7).get()') == 7
        // end::embed_engine_config[]
    }

    @Test
    void optionsBuilderHelpPath() {
        // tag::embed_options_help[]
        int rc = Main.start(GroovyshOptions.builder().showBanner(false).build(), '--help')
        assert rc == 0
        // end::embed_options_help[]
    }

    @Test
    void classLoaderAccessor() {
        // tag::embed_classloader[]
        def engine = new GroovyEngine()
        assert engine.classLoader instanceof GroovyEngine.EngineClassLoader
        // end::embed_classloader[]
    }
}

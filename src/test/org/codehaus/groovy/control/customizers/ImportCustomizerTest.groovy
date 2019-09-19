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
package org.codehaus.groovy.control.customizers

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Tests the import customizer.
 */
class ImportCustomizerTest extends GroovyTestCase {
    CompilerConfiguration configuration
    ImportCustomizer importCustomizer

    void setUp() {
        configuration = new CompilerConfiguration()
        importCustomizer = new ImportCustomizer()
        configuration.addCompilationCustomizers(importCustomizer)
    }

    void testAddImport() {
        importCustomizer.addImports("java.util.concurrent.atomic.AtomicInteger")
        def shell = new GroovyShell(configuration)
        shell.evaluate("new AtomicInteger(0)")
        // no exception means success
    }

    void testAddImportWithAlias() {
        importCustomizer.addImport("AI","java.util.concurrent.atomic.AtomicInteger")
        def shell = new GroovyShell(configuration)
        shell.evaluate("new AI(0)")
        // no exception means success
    }

    void testAddInnerClassImport() {
        importCustomizer.addImports("org.codehaus.groovy.control.customizers.ImportCustomizerTest.Inner")
        def shell = new GroovyShell(configuration)
        shell.evaluate("new Inner()")
        // no exception means success
    }

    void testAddInnerClassImport2() {
        importCustomizer.addImports("org.codehaus.groovy.control.customizers.ImportCustomizerTest")
        def shell = new GroovyShell(configuration)
        shell.evaluate("new ImportCustomizerTest.Inner()")
        // no exception means success
    }

    void testAddStaticImport() {
        importCustomizer.addStaticImport("java.lang.Math", "PI")
        def shell = new GroovyShell(configuration)
        shell.evaluate("PI")
        // no exception means success
    }

    void testAddStaticImportWithAlias() {
        importCustomizer.addStaticImport("pi","java.lang.Math", "PI")
        def shell = new GroovyShell(configuration)
        shell.evaluate("pi")
        // no exception means success
    }

    void testAddStaticStarImport() {
        importCustomizer.addStaticStars("java.lang.Math")
        def shell = new GroovyShell(configuration)
        shell.evaluate("PI")
        // no exception means success
    }

    void testAddStarImport() {
        importCustomizer.addStarImports("java.util.concurrent.atomic")
        def shell = new GroovyShell(configuration)
        shell.evaluate("new AtomicInteger(0)")
        // no exception means success
    }

    void testAddImports() {
        importCustomizer.addImports("java.util.concurrent.atomic.AtomicInteger","java.util.concurrent.atomic.AtomicLong")
        def shell = new GroovyShell(configuration)
        shell.evaluate("""new AtomicInteger(0)
        new AtomicLong(0)""")
        // no exception means success
    }

    void testAddImportsOnScriptEngine() {
        File script = File.createTempFile('test', '.groovy')
            script.deleteOnExit()
            script.write """
            println new SimpleDateFormat()
        """

        importCustomizer.addImports 'java.text.SimpleDateFormat'

        // Run script with script engine: this will not work, import customizer is not used
        GroovyScriptEngine scriptEngine = new GroovyScriptEngine(script.parent)
        scriptEngine.setConfig configuration
        scriptEngine.run script.name, new Binding()
    }

    protected static class Inner {}
}

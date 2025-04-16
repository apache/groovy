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

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link ImportCustomizer}.
 */
final class ImportCustomizerTest {

    private final CompilerConfiguration configuration = new CompilerConfiguration()
    private final ImportCustomizer importCustomizer = new ImportCustomizer()

    @Before
    void setUp() {
        configuration.addCompilationCustomizers(importCustomizer)
    }

    @Test
    void testAddImport() {
        importCustomizer.addImports('java.util.concurrent.atomic.AtomicInteger')
        def shell = new GroovyShell(configuration)
        shell.evaluate('new AtomicInteger(0)')
        // no exception means success
    }

    @Test
    void testAddImportWithAlias() {
        importCustomizer.addImport('AI', 'java.util.concurrent.atomic.AtomicInteger')
        def shell = new GroovyShell(configuration)
        shell.evaluate('new AI(0)')
        // no exception means success
    }

    @Test
    void testAddInnerClassImport() {
        importCustomizer.addImports('org.codehaus.groovy.control.customizers.ImportCustomizerTest.Inner')
        def shell = new GroovyShell(configuration)
        shell.evaluate('new Inner()')
        // no exception means success
    }

    @Test
    void testAddInnerClassImport2() {
        importCustomizer.addImports('org.codehaus.groovy.control.customizers.ImportCustomizerTest')
        def shell = new GroovyShell(configuration)
        shell.evaluate('new ImportCustomizerTest.Inner()')
        // no exception means success
    }

    @Test
    void testAddStaticImport() {
        importCustomizer.addStaticImport('java.lang.Math', 'PI')
        def shell = new GroovyShell(configuration)
        shell.evaluate('PI')
        // no exception means success
    }

    @Test
    void testAddStaticImportWithAlias() {
        importCustomizer.addStaticImport('pi', 'java.lang.Math', 'PI')
        def shell = new GroovyShell(configuration)
        shell.evaluate('pi')
        // no exception means success
    }

    @Test
    void testAddStaticStarImport() {
        importCustomizer.addStaticStars('java.lang.Math')
        def shell = new GroovyShell(configuration)
        shell.evaluate('PI')
        // no exception means success
    }

    @Test
    void testAddStarImport() {
        importCustomizer.addStarImports('java.util.concurrent.atomic')
        def shell = new GroovyShell(configuration)
        shell.evaluate('new AtomicInteger(0)')
        // no exception means success
    }

    @Test
    void testAddImports() {
        importCustomizer.addImports('java.util.concurrent.atomic.AtomicInteger', 'java.util.concurrent.atomic.AtomicLong')
        def shell = new GroovyShell(configuration)
        shell.evaluate('''
            new AtomicInteger(0)
            new AtomicLong(0)
        ''')
        // no exception means success
    }

    @Test
    void testAddImportsOnScriptEngine() {
        importCustomizer.addImports('java.text.SimpleDateFormat')

        def script = File.createTempFile('test', '.groovy')
        script.deleteOnExit()
        script.write '''
            println new SimpleDateFormat()
        '''
        // run script with script engine; this will not work if import customizer is not used
        def scriptEngine = new GroovyScriptEngine(script.parent)
        scriptEngine.config = configuration
        scriptEngine.run(script.name, new Binding())
    }

    @Test // GROOVY-8399
    void testAddImportsOnModuleWithMultipleClasses() {
        importCustomizer.addImports('java.text.SimpleDateFormat')
        def shell = new GroovyShell(configuration)
        shell.evaluate('''\
            @groovy.transform.ASTTest(phase=SEMANTIC_ANALYSIS, value={
                def imports = node.module.imports*.text
                assert imports == ['import java.text.SimpleDateFormat as SimpleDateFormat']
            })
            class A {
                static class AA {
                }
            }
            class B {
                static class BB {
                }
            }
            class C {
                static class CC {
                }
            }
            println new SimpleDateFormat()
        ''')
    }

    @Test // GROOVY-8399
    void testAddStarImportsOnModuleWithMultipleClasses() {
        importCustomizer.addStarImports('java.text', 'groovy.transform')
        def shell = new GroovyShell(configuration)
        shell.evaluate('''\
            @groovy.transform.ASTTest(phase=SEMANTIC_ANALYSIS, value={
                def imports = node.module.starImports*.text
                assert imports == ['import java.text.*', 'import groovy.transform.*']
            })
            class A {
                static class AA {
                }
            }
            class B {
                static class BB {
                }
            }
            class C {
                static class CC {
                }
            }
            println new SimpleDateFormat()
        ''')
    }

    protected static class Inner {}
}

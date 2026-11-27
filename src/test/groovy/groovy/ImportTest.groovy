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
package groovy

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class ImportTest {

    @Test
    void testImportDefaults() {
        assertScript '''
            Object file = new File('x')
            assert file instanceof File
        '''

        assertScript '''
            def map = [foo:'bar']
            assert map instanceof Map
        '''

        assertScript '''
            def list = [1, 2, 3]
            assert list instanceof List
        '''

        assertScript '''
            def decimal = new BigDecimal(0)
            def integer = new BigInteger(0)
        '''
    }

    // GROOVY-8254
    @Test
    void testImportConflicts() {
        assertScript '''
            import java.util.List
            import java.util.List
        '''

        assertScript '''
            import java.util.List
            import java.util.List as List
        '''

        assertScript '''
            import java.util.List
            import java.util.List as MyList
        '''

        assertScript '''
            import java.util.Map.Entry
            import static java.util.Map.Entry
            import static java.util.Map.Entry as Entry
        '''

        assertScript '''
            import java.util.Map.Entry
            class Main {
                static class Entry { }
                static main(array) { }
            }
        '''

        assertScript '''package p
            import p.Main // okay
            class Main {
                static main(array) { }
            }
        '''

        def err = shouldFail '''
            import java.net.Proxy
            import groovy.util.Proxy
        '''
        assert err.message =~ /The name Proxy is already declared/

        GroovyShell shell = GroovyShell.withConfig {
            imports { normal 'java.net.Proxy' }
        }
        err = shouldFail shell, '''
            import groovy.util.Proxy
        '''
        assert err.message =~ /The name Proxy is already declared/

        err = shouldFail '''
            import java.lang.Object as Foo
            import java.lang.Number as Foo
        '''
        assert err.message =~ /The name Foo is already declared/

        err = shouldFail '''
            import java.util.Map.Entry
            import java.lang.Object as Entry
        '''
        assert err.message =~ /The name Entry is already declared/

        err = shouldFail '''
            import java.lang.Object as Entry
            import static java.util.Map.Entry
        '''
        assert err.message =~ /The name Entry is already declared/

        err = shouldFail '''
            import java.util.Map.Entry
            class Entry { }
        '''
        assert err.message =~ /The name Entry is already declared/

        err = shouldFail '''
            import java.util.Map.Entry as Pair
            class Pair { }
        '''
        assert err.message =~ /The name Pair is already declared/

        shell = GroovyShell.withConfig {
            imports { normal 'java.util.Map.Entry' }
        }
        err = shouldFail shell, '''
            class Entry { }
        '''
        assert err.message =~ /The name Entry is already declared/
    }

    // GROOVY-5103
    @Test
    void testImportStaticInnerClass() {
        assertScript '''
            import java.util.Map.Entry
            Entry entry = [foo:'bar'].entrySet().first()
        '''

        assertScript '''
            import static java.util.Map.Entry
            Entry entry = [foo:'bar'].entrySet().first()
        '''

        assertScript '''
            import java.util.Map.*
            Entry entry = [foo:'bar'].entrySet().first()
        '''

        assertScript '''
            import static java.util.Map.*
            Entry entry = [foo:'bar'].entrySet().first()
        '''
    }

    // module imports

    @Test
    void testImportModuleJavaBase() {
        assertScript '''
            import module java.base

            // java.util
            List list = [1, 2, 3]
            Map map = [a: 1]

            // java.io
            File f = new File('.')

            // java.net
            URI uri = new URI('http://example.com')

            // java.time
            LocalDate date = LocalDate.now()

            // java.util.concurrent
            CountDownLatch latch = new CountDownLatch(1)

            assert list.size() == 3
        '''
    }

    @Test
    void testImportModuleJavaSql() {
        assertScript '''
            import module java.sql

            // java.sql package
            assert Connection.name == 'java.sql.Connection'
            assert DriverManager.name == 'java.sql.DriverManager'
        '''
    }

    @Test
    void testImportModuleFromClasspath() {
        // Find JUnit JAR from the test classloader and add it to the compiler classpath
        def junitUrl = Test.class.protectionDomain.codeSource.location
        def config = new CompilerConfiguration()
        config.classpathList = [new File(junitUrl.toURI()).path]
        def loader = new GroovyClassLoader(getClass().classLoader, config)
        def shell = new GroovyShell(loader)
        shell.evaluate '''
            import module org.junit.jupiter.api

            // org.junit.jupiter.api package
            assert Test.name == 'org.junit.jupiter.api.Test'
            assert Assertions.name == 'org.junit.jupiter.api.Assertions'
            assert DisplayName.name == 'org.junit.jupiter.api.DisplayName'
            assert DisabledIf.name == 'org.junit.jupiter.api.condition.DisabledIf'
        '''
    }

    // GROOVY-11896: JEP 476 specifies that `import module M` should also import
    // packages from modules that M requires transitively.
    @Test
    void testImportModuleTransitiveDependencies() {
        // java.sql requires transitive java.xml, java.logging, java.transaction.xa
        assertScript '''
            import module java.sql

            // Direct exports of java.sql
            assert Connection.name == 'java.sql.Connection'

            // From java.xml (requires transitive): javax.xml.transform
            assert Source.name == 'javax.xml.transform.Source'

            // From java.logging (requires transitive): java.util.logging
            assert Logger.name == 'java.util.logging.Logger'

            // From java.transaction.xa (requires transitive): javax.transaction.xa
            assert XAResource.name == 'javax.transaction.xa.XAResource'
        '''
    }

    @Test
    void testImportModuleUnknown() {
        shouldFail '''
            import module no.such.module
        '''
    }

    @Test
    void testImportModuleStarNotAllowed() {
        shouldFail '''
            import module java.base.*
        '''
    }

    @Test
    void testImportModuleAliasNotAllowed() {
        shouldFail '''
            import module java.base as jb
        '''
    }

    @Test
    void testModuleAsIdentifier() {
        // 'module' can still be used as a variable name
        assertScript '''
            def module = 'hello'
            assert module.toUpperCase() == 'HELLO'
        '''
    }

    // GROOVY-11896 / JEP 511: JLS §7.5.5 — when a module import exposes two
    // packages that both contain a public type with the same simple name,
    // referencing that simple name is a deterministic compile-time ambiguity error.
    // java.desktop exports both javax.swing.text.Element and javax.swing.text.html.parser.Element
    // (as well as org.w3c.dom.Element via requires transitive java.xml).
    @Test
    void testImportModuleAmbiguousSimpleName() {
        def err = shouldFail '''
            import module java.desktop
            Element e = null
        '''
        assert err.message.contains('ambiguous') : "expected ambiguity error, got: ${err.message}"
    }

    // JEP 511 / JLS §7.5.5: ambiguity across two module imports (including java.base packages
    // that are also Groovy default imports, such as java.util.List vs java.awt.List and
    // java.util.Date vs java.sql.Date).
    @Test
    void testImportModuleAmbiguousAcrossTwoModules() {
        def listErr = shouldFail '''
            import module java.base
            import module java.desktop
            List items = null
        '''
        assert listErr.message.contains('ambiguous') : "expected ambiguity error, got: ${listErr.message}"

        def dateErr = shouldFail '''
            import module java.base
            import module java.sql
            Date value = null
        '''
        assert dateErr.message.contains('ambiguous') : "expected ambiguity error, got: ${dateErr.message}"
    }

    // JEP 511 / JLS §6.4.1: explicit type-import-on-demand (star import) and
    // single-type imports shadow module imports regardless of source order.
    @Test
    void testImportModuleShadowedByStarImport() {
        assertScript '''
            import module java.desktop
            assert List.name == 'java.awt.List'
        '''
        assertScript '''
            import module java.desktop
            import java.util.*
            assert List.name == 'java.util.List'
        '''
        assertScript '''
            import java.util.*
            import module java.desktop
            assert List.name == 'java.util.List'
        '''
        assertScript '''
            import module java.base
            import module java.desktop
            import java.util.*
            assert List.name == 'java.util.List'
        '''
        assertScript '''
            import module java.base
            import module java.sql
            import java.sql.Date
            assert Date.name == 'java.sql.Date'
        '''
    }

    // JEP 511 / JLS §7.5.5: duplicate module imports are permitted and do not self-collide;
    // dotted module names may span lines and contain comments.
    @Test
    void testImportModuleDuplicateAndMultilineName() {
        assertScript '''
            import module java.sql
            import module java.sql
            assert Connection.name == 'java.sql.Connection'
            assert String.name == 'java.lang.String'
        '''
        assertScript '''
            import module java
                .sql
            import module java /* comment */ .logging
            assert Connection.name == 'java.sql.Connection'
            assert Logger.name == 'java.util.logging.Logger'
        '''
    }

    // JEP 261 / JEP 511: aggregator module java.se is not in the default unnamed-module
    // root set and requires --add-modules java.se (or ALL-SYSTEM) to be readable.
    @Test
    void testImportModuleJavaSeUnnamedRequiresAddModules() {
        def err = shouldFail '''
            import module java.se
            assert Connection.name == 'java.sql.Connection'
        '''
        assert err.message.contains('module java.se is not readable in the unnamed module')

        def seConfig = new CompilerConfiguration()
        seConfig.addModules = ['java.se']
        new GroovyShell(seConfig).evaluate '''
            import module java.se
            assert Connection.name == 'java.sql.Connection'
            assert Logger.name == 'java.util.logging.Logger'
        '''

        def allSysConfig = new CompilerConfiguration()
        allSysConfig.addModules = ['ALL-SYSTEM']
        new GroovyShell(allSysConfig).evaluate '''
            import module java.se
            assert Connection.name == 'java.sql.Connection'
        '''
    }

    // JEP 261 / JEP 511: --add-exports <module>/<pkg>=ALL-UNNAMED extends unnamed-module import module.
    @Test
    void testImportModuleUnnamedAddExports() {
        def config = new CompilerConfiguration()
        config.addExports = ['java.base/sun.security.util=ALL-UNNAMED']
        new GroovyClassLoader(this.class.classLoader, config).withCloseable { gcl ->
            def clazz = gcl.parseClass('''
                import module java.base
                class UseExported {
                    SecurityConstants sc
                }
            ''')
            assert clazz.getDeclaredField('sc').type.name == 'sun.security.util.SecurityConstants'
        }

        def badConfig = new CompilerConfiguration()
        badConfig.addExports = ['malformed-spec']
        def err = shouldFail {
            new GroovyShell(badConfig).evaluate '''
                import module java.base
                assert List.name == 'java.util.List'
            '''
        }
        assert err.message.contains('invalid --add-exports option:')
    }
}

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
        def config = new org.codehaus.groovy.control.CompilerConfiguration()
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

    // GROOVY-11896: JLS 7.5.5 Example 7.5.5-3 — when a module import exposes
    // two packages that both contain a public type with the same simple name,
    // using that simple name must be a compile-time error, not a silent pick.
    // Here, java.desktop exports both javax.swing.text (with Element interface)
    // and javax.swing.text.html.parser (with Element class).
    @Test
    void testImportModuleAmbiguousSimpleName() {
        def err = shouldFail '''
            import module java.desktop
            Element e = null
        '''
        assert err.message.contains('ambiguous'),
                "expected ambiguity error, got: ${err.message}"
    }

    // GROOVY-11896: JLS 6.4.1 — a type-import-on-demand declaration shadows
    // types imported by a single-module-import declaration. The `import module`
    // is written FIRST so that without proper tier separation, the module-
    // expanded java.awt.* would win by list ordering. With correct shadowing,
    // the user's `import java.util.*` wins regardless of source order, and
    // List resolves to java.util.List rather than java.awt.List.
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
    }
}

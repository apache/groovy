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
package org.apache.groovy.ginq.provider.sql

import org.apache.groovy.ginq.GinqGroovyMethods
import org.apache.groovy.ginq.provider.GinqProvider
import org.apache.groovy.ginq.provider.collection.GinqAstWalker
import org.apache.groovy.ginq.provider.sql.jooq.JooqSqlGinqProvider
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Providers report whether their dependencies can be loaded.
 * {@code provider: 'sql'} uses the first candidate that can;
 * an explicit name fails with that provider's message when it cannot.
 */
class SqlAutoProviderTest {

    private static final String NATIVE_WALKER = 'org.apache.groovy.ginq.provider.sql.GinqSqlWalker'
    private static final String JOOQ_WALKER = 'org.apache.groovy.ginq.provider.sql.jooq.JooqSqlWalker'

    @Test
    void testProvidersAreAvailableOnTheTestClasspath() {
        ClassLoader loader = GinqGroovyMethods.class.classLoader
        assert new NativeSqlGinqProvider().unavailableReason(loader) == null
        assert new JooqSqlGinqProvider().unavailableReason(loader) == null
    }

    @Test
    void testJooqProviderReportsMissingJooq() {
        String reason = new JooqSqlGinqProvider().unavailableReason(hidingLoader('org.jooq.'))
        assert reason.contains('org.jooq:jooq')
        assert reason.contains('compile classpath')
        assert new NativeSqlGinqProvider().unavailableReason(hidingLoader('org.jooq.')) == null
    }

    @Test
    void testSqlProvidersReportMissingGroovySql() {
        ClassLoader loader = hidingLoader('groovy.sql.')
        assert new NativeSqlGinqProvider().unavailableReason(loader).contains('groovy-sql')
        assert new JooqSqlGinqProvider().unavailableReason(loader).contains('groovy-sql')
    }

    @Test
    void testSqlProviderFallsBackWhenJooqClassesAreAbsent() {
        Selection selection = select(hidingLoader('org.jooq.'), 'sql')
        assert selection.error == null
        assert NATIVE_WALKER == selection.walker
    }

    @Test
    void testExplicitNativeProviderStaysSelectedWhenJooqClassesAreAbsent() {
        Selection selection = select(hidingLoader('org.jooq.'), 'native-sql')
        assert selection.error == null
        assert NATIVE_WALKER == selection.walker
    }

    @Test
    void testExplicitJooqProviderFailsWhenJooqClassesAreAbsent() {
        Selection selection = select(hidingLoader('org.jooq.'), 'jooq-sql')
        assert selection.walker == null
        assert selection.error != null
        assert selection.error.contains('org.jooq:jooq')
        assert selection.error.contains('compile classpath')
    }

    @Test
    void testExplicitNativeProviderFailsWhenGroovySqlIsAbsent() {
        Selection selection = select(hidingLoader('groovy.sql.'), 'native-sql')
        assert selection.walker == null
        assert selection.error != null
        assert selection.error.contains('groovy-sql')
        assert !selection.error.contains('org.jooq:jooq')
    }

    @Test
    void testSqlProviderFailsWhenNoBackendCanLoad() {
        Selection selection = select(hidingLoader('org.jooq.', 'groovy.sql.'), 'sql')
        assert selection.walker == null
        assert selection.error != null
        assert selection.error.contains('No usable GINQ SQL provider')
        assert selection.error.contains('org.jooq:jooq')
        assert selection.error.contains('groovy-sql')
    }

    @Test
    void testSqlProviderPrefersJooqWhenPresent() {
        Selection selection = select(GinqGroovyMethods.class.classLoader, 'sql')
        assert selection.error == null
        assert JOOQ_WALKER == selection.walker
    }

    private static Selection select(ClassLoader loader, String provider) {
        Class<?> methods = loader.loadClass(GinqGroovyMethods.name)
        Method resolve = methods.getDeclaredMethod('resolveAstWalkerClassName', SourceUnit, Map, MapExpression)
        resolve.accessible = true
        SourceUnit source = SourceUnit.create('auto-sql', '')
        Selection selection = new Selection()
        try {
            selection.walker = (String) resolve.invoke(null, source, Collections.singletonMap('provider', provider), new MapExpression())
        } catch (InvocationTargetException e) {
            // collectErrors reports a fatal error, which aborts compilation
            if (!source.errorCollector.hasErrors()) {
                throw e.cause ?: e
            }
        }
        if (source.errorCollector.hasErrors()) {
            selection.error = ((SyntaxErrorMessage) source.errorCollector.errors[0]).cause.originalMessage
        }
        return selection
    }

    private static ClassLoader hidingLoader(String... hiddenPrefixes) {
        List<URL> urls = [
                GinqGroovyMethods,
                GinqProvider,
                GinqAstWalker,
                NativeSqlGinqProvider,
                JooqSqlGinqProvider,
                GinqSqlWalker,
        ].collect { Class<?> type -> type.protectionDomain.codeSource.location }
        return new HidingLoader(urls.unique().toArray(new URL[0]), hiddenPrefixes)
    }

    private static class Selection {
        String walker
        String error
    }

    /**
     * Defines the GINQ classes itself and refuses the supplied prefixes,
     * so provider selection runs as it would on a classpath missing those dependencies.
     */
    private static class HidingLoader extends URLClassLoader {
        private final String[] hiddenPrefixes

        HidingLoader(URL[] urls, String[] hiddenPrefixes) {
            super(urls, GinqGroovyMethods.class.classLoader)
            this.hiddenPrefixes = hiddenPrefixes
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            for (String prefix : hiddenPrefixes) {
                if (name.startsWith(prefix)) {
                    throw new ClassNotFoundException(name)
                }
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name)
                if (loaded != null) {
                    if (resolve) {
                        resolveClass(loaded)
                    }
                    return loaded
                }
                if (name.startsWith('org.apache.groovy.ginq.') || name.startsWith('groovy.ginq.')) {
                    try {
                        Class<?> found = findClass(name)
                        if (resolve) {
                            resolveClass(found)
                        }
                        return found
                    } catch (ClassNotFoundException ignored) {
                        // fall through to the parent for classes outside these jars
                    }
                }
                return super.loadClass(name, resolve)
            }
        }
    }
}

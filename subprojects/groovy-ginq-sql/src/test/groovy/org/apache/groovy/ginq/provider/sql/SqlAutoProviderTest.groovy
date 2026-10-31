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
import org.junit.jupiter.api.Test

import java.lang.reflect.Method

/**
 * {@code provider: 'sql'} prefers {@code jooq-sql} only when jOOQ can be loaded.
 * The service descriptor still registers that provider when the optional classes
 * are absent, so explicit {@code provider: 'jooq-sql'} keeps its own error.
 */
class SqlAutoProviderTest {

    private static final String NATIVE_WALKER = 'org.apache.groovy.ginq.provider.sql.GinqSqlWalker'
    private static final String JOOQ_WALKER = 'org.apache.groovy.ginq.provider.sql.jooq.JooqSqlWalker'

    @Test
    void testSqlProviderFallsBackWhenJooqClassesAreAbsent() {
        String walker = resolve(jooqHidingLoader(), 'sql')
        assert NATIVE_WALKER == walker
    }

    @Test
    void testExplicitJooqProviderStaysSelectedWhenJooqClassesAreAbsent() {
        String walker = resolve(jooqHidingLoader(), 'jooq-sql')
        assert JOOQ_WALKER == walker
    }

    @Test
    void testSqlProviderPrefersJooqWhenPresent() {
        String walker = resolve(GinqGroovyMethods.class.classLoader, 'sql')
        assert JOOQ_WALKER == walker
    }

    private static String resolve(ClassLoader loader, String provider) {
        Class<?> methods = loader.loadClass(GinqGroovyMethods.name)
        Method resolve = methods.getDeclaredMethod('resolveAstWalkerClassName', SourceUnit, Map, MapExpression)
        resolve.accessible = true
        SourceUnit source = SourceUnit.create('auto-sql', '')
        String walker = (String) resolve.invoke(null, source, Collections.singletonMap('provider', provider), null)
        assert !source.errorCollector.hasErrors()
        return walker
    }

    private static ClassLoader jooqHidingLoader() {
        List<URL> urls = [
                GinqGroovyMethods,
                GinqProvider,
                GinqAstWalker,
                NativeSqlGinqProvider,
                JooqSqlGinqProvider,
                GinqSqlWalker,
        ].collect { Class<?> type -> type.protectionDomain.codeSource.location }
        return new JooqHidingLoader(urls.unique().toArray(new URL[0]))
    }

    /**
     * Defines the GINQ classes itself and refuses to load {@code org.jooq.*},
     * so provider selection runs as it would on a classpath without jOOQ.
     */
    private static class JooqHidingLoader extends URLClassLoader {
        JooqHidingLoader(URL[] urls) {
            super(urls, GinqGroovyMethods.class.classLoader)
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith('org.jooq.')) {
                throw new ClassNotFoundException(name)
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

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

import groovy.transform.CompileStatic
import org.apache.groovy.lang.annotation.Incubating

/**
 * Maps Groovy method and function names onto SQL function shapes.
 * <p>
 * {@link #standard()} is the sealed set the translator uses. A new uniform
 * function is one {@link #register} line here (or on a {@link #copy()} for a
 * caller that is not the built-in set). Unknown names are not registered, so
 * the translator keeps rejecting them. Aggregates ({@code count}, {@code sum},
 * {@code avg}, {@code min}, {@code max}, including {@code count(distinct)})
 * stay on the aggregate path: {@code min} and {@code max} there are aggregates,
 * while {@code Math.min} and {@code Math.max} are the entries in this registry.
 * <p>
 * Not registered, because the SQL function is not faithful to Groovy:
 * {@code padLeft} and {@code padRight} ({@code LPAD} and {@code RPAD} truncate
 * when the value is already longer; Groovy does not), and {@code Math.signum}
 * ({@code SIGN}'s numeric type differs by dialect).
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
final class SqlFunctionRegistry {
    private final Map<String, SqlFunctionMapping> mappings = new LinkedHashMap<String, SqlFunctionMapping>()
    private boolean sealed

    private SqlFunctionRegistry() {
    }

    /**
     * The built-in scalar mappings. The instance is sealed.
     *
     * @return the standard registry
     */
    static SqlFunctionRegistry standard() {
        return STANDARD
    }

    /**
     * A mutable copy. Registering on the copy does not change {@link #standard()}.
     *
     * @return an unsealed copy of these mappings
     */
    SqlFunctionRegistry copy() {
        SqlFunctionRegistry copy = new SqlFunctionRegistry()
        copy.mappings.putAll(mappings)
        return copy
    }

    /**
     * Adds a mapping. The built-in registry refuses further registration.
     * Registering the same Groovy name and {@link SqlFunctionMapping.Kind} twice
     * is an error, so a built-in is not silently replaced.
     *
     * @param mapping the mapping to add
     */
    void register(SqlFunctionMapping mapping) {
        if (sealed) {
            throw new IllegalStateException('the built-in SQL function registry is sealed; copy() it to add a function')
        }
        if (mapping == null) {
            throw new IllegalArgumentException('mapping is required')
        }
        String key = key(mapping.kind, mapping.groovyName)
        if (mappings.containsKey(key)) {
            throw new IllegalArgumentException("SQL function `${mapping.groovyName}` is already registered for ${mapping.kind}")
        }
        mappings.put(key, mapping)
    }

    /**
     * Looks up a mapping.
     *
     * @param groovyName the method or function name, without a receiver
     * @param kind where the call sits
     * @return the mapping, or {@code null} when nothing is registered
     */
    SqlFunctionMapping find(String groovyName, SqlFunctionMapping.Kind kind) {
        if (groovyName == null || kind == null) return null
        return mappings.get(key(kind, groovyName))
    }

    private void seal() {
        sealed = true
    }

    private static String key(SqlFunctionMapping.Kind kind, String groovyName) {
        return kind.name() + ':' + groovyName
    }

    private static final SqlFunctionRegistry STANDARD = builtin()

    private static SqlFunctionRegistry builtin() {
        SqlFunctionRegistry registry = new SqlFunctionRegistry()
        registry.register(SqlFunctionMapping.receiver('toUpperCase', 'UPPER'))
        registry.register(SqlFunctionMapping.receiver('toLowerCase', 'LOWER'))
        registry.register(SqlFunctionMapping.receiver('trim', 'TRIM'))
        registry.register(SqlFunctionMapping.receiver('length', 'CHAR_LENGTH'))
        registry.register(SqlFunctionMapping.receiver('size', 'CHAR_LENGTH'))
        registry.register(SqlFunctionMapping.receiver('abs', 'ABS'))
        registry.register(SqlFunctionMapping.receiver('floor', 'FLOOR'))
        registry.register(SqlFunctionMapping.receiver('ceil', 'CEILING'))
        registry.register(SqlFunctionMapping.receiver('round', 'ROUND', 0, 1,
                '`round` expects no arguments, or one scale argument'))
        registry.register(SqlFunctionMapping.receiver('replace', 'REPLACE', 2, 2,
                '`replace` expects two arguments, the search text and its replacement'))
        registry.register(SqlFunctionMapping.concat())
        registry.register(SqlFunctionMapping.isEmpty())
        registry.register(SqlFunctionMapping.substring())
        registry.register(SqlFunctionMapping.like('contains', true, true))
        registry.register(SqlFunctionMapping.like('startsWith', false, true))
        registry.register(SqlFunctionMapping.like('endsWith', true, false))
        registry.register(SqlFunctionMapping.freeFunction('coalesce', 'COALESCE', 2, Integer.MAX_VALUE,
                '`coalesce` expects at least two arguments'))
        registry.register(SqlFunctionMapping.math('max', 'GREATEST',
                '`Math.max` and `Math.min` expect two arguments'))
        registry.register(SqlFunctionMapping.math('min', 'LEAST',
                '`Math.max` and `Math.min` expect two arguments'))
        registry.register(SqlFunctionMapping.cast())
        registry.register(SqlFunctionMapping.indexOf())
        registry.seal()
        return registry
    }
}

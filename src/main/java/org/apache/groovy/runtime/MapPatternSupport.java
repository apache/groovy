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
package org.apache.groovy.runtime;

import org.apache.groovy.lang.annotation.Incubating;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime destructuring support for map patterns (GEP-19) &mdash; the emit
 * target of the pattern switch lowering in the parser.
 * <p>
 * Map patterns destructure {@code Map} values with open semantics: a pattern
 * matches if all named keys are present and their value patterns match; extra
 * entries are ignored unless captured by a rest binding. Other values (and
 * {@code null}) do not match.
 *
 * @since 6.0.0
 */
@Incubating
public final class MapPatternSupport {

    private MapPatternSupport() {
    }

    /**
     * Returns the given value as a map of its entries, or {@code null} when the
     * value is not destructurable by a map pattern.
     */
    @SuppressWarnings("unchecked")
    public static Map<Object, Object> entriesOrNull(final Object value) {
        return value instanceof Map ? (Map<Object, Object>) value : null;
    }

    /**
     * Returns a copy of the entries bound by a rest binding: those whose keys are
     * not named by the map pattern.
     */
    public static Map<Object, Object> rest(final Map<Object, Object> entries, final List<Object> namedKeys) {
        Map<Object, Object> result = new LinkedHashMap<>(entries);
        for (Object key : namedKeys) {
            result.remove(key);
        }
        return result;
    }
}

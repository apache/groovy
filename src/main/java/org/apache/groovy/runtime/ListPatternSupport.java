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

import org.apache.groovy.lang.annotation.GroovyABI;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runtime destructuring support for list patterns (GEP-19): the methods the
 * code derived from a list pattern calls, so compiled classes link to them.
 * <p>
 * List patterns destructure {@code List}, array and {@code Iterable} values
 * structurally; other values (and {@code null}) do not match. An
 * {@code Iterable} is materialised as a list for matching, so it must be
 * traversable non-destructively; a switch materialises its selector at most
 * once, for all of its list pattern arms. A {@code List} is matched in place,
 * so it must not be modified concurrently.
 *
 * @since 7.0.0
 */
@Incubating
@GroovyABI(since = "7.0.0")
public final class ListPatternSupport {

    private ListPatternSupport() {
    }

    /**
     * Returns the given value as a list of its elements, or {@code null} when the
     * value is not destructurable by a list pattern: a list is returned as is
     * (and so sees any concurrent modification of it), an array (including a
     * primitive array) or any other iterable as a copy, which traverses it.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> elementsOrNull(final Object value) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        if (value instanceof Object[]) {
            return Arrays.asList((Object[]) value);
        }
        if (value != null && value.getClass().isArray()) {
            return DefaultTypeTransformation.primitiveArrayToList(value);
        }
        if (value instanceof Iterable) {
            List<Object> result = new ArrayList<>();
            for (Object element : (Iterable<Object>) value) {
                result.add(element);
            }
            return result;
        }
        return null;
    }

    /**
     * Returns a copy of the elements bound by a rest binding: those from
     * {@code from} up to (but not including) the last {@code dropRight} elements.
     */
    public static List<Object> rest(final List<Object> elements, final int from, final int dropRight) {
        return new ArrayList<>(elements.subList(from, elements.size() - dropRight));
    }

    /**
     * Whether every element is an instance of the given type; used for the type
     * check of a typed rest binding such as {@code Integer... t}.
     */
    public static boolean allInstanceOf(final List<Object> elements, final Class<?> type) {
        for (Object element : elements) {
            if (!type.isInstance(element)) return false;
        }
        return true;
    }
}

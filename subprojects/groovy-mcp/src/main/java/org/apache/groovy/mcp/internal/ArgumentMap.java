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
package org.apache.groovy.mcp.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * One tool or prompt argument object. Unknown names and wrong JSON
 * types fail closed. Strings are not coerced from numbers.
 */
public final class ArgumentMap {

    private final Map<String, Object> values;

    private ArgumentMap(final Map<String, Object> values) {
        this.values = values;
    }

    /**
     * @param raw the JSON value, or {@code null} for an empty object
     * @param allowed names the caller may set
     * @return the argument map
     */
    public static ArgumentMap of(final Object raw, final Set<String> allowed) {
        if (raw == null) {
            return new ArgumentMap(Map.of());
        }
        if (!(raw instanceof Map<?, ?> map)) {
            throw new ProtocolException(-32602, "arguments must be an object");
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String name)) {
                throw new ProtocolException(-32602, "argument name must be a string");
            }
            if (allowed != null && !allowed.contains(name)) {
                throw new ToolFailure("unknown argument: " + JsonValues.brief(name));
            }
            copy.put(name, entry.getValue());
        }
        return new ArgumentMap(copy);
    }

    /**
     * @param name argument name
     * @return {@code true} when the name is present, including a null value
     */
    public boolean has(final String name) {
        return values.containsKey(name);
    }

    /**
     * @param name argument name
     * @return the string, or {@code null} when the name is absent
     */
    public String optionalString(final String name) {
        if (!has(name)) {
            return null;
        }
        return stringValue(name);
    }

    /**
     * @param name argument name
     * @return the string
     */
    public String requiredString(final String name) {
        if (!has(name) || values.get(name) == null) {
            throw new ToolFailure(name + " is required");
        }
        return stringValue(name);
    }

    /**
     * @param name argument name
     * @param defaultValue value when the name is absent
     * @param minimum inclusive minimum
     * @param maximum inclusive maximum
     * @return the int
     */
    public int optionalInt(final String name, final int defaultValue, final int minimum, final int maximum) {
        if (!has(name) || values.get(name) == null) {
            if (has(name)) {
                throw new ToolFailure(name + " must be an integer");
            }
            return defaultValue;
        }
        return ranged(name, minimum, maximum);
    }

    /**
     * @param name argument name
     * @param minimum inclusive minimum
     * @param maximum inclusive maximum
     * @return the int
     */
    public int requiredInt(final String name, final int minimum, final int maximum) {
        if (!has(name) || values.get(name) == null) {
            throw new ToolFailure(name + " is required");
        }
        return ranged(name, minimum, maximum);
    }

    /**
     * @param name argument name
     * @param defaultValue value when the name is absent
     * @return the boolean
     */
    public boolean optionalBoolean(final String name, final boolean defaultValue) {
        if (!has(name)) {
            return defaultValue;
        }
        Object value = values.get(name);
        if (!(value instanceof Boolean flag)) {
            throw new ToolFailure(name + " must be a boolean");
        }
        return flag;
    }

    private int ranged(final String name, final int minimum, final int maximum) {
        Integer value = JsonValues.asInt(values.get(name));
        if (value == null) {
            throw new ToolFailure(name + " must be an integer");
        }
        if (value < minimum || value > maximum) {
            throw new ToolFailure(name + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    private String stringValue(final String name) {
        Object value = values.get(name);
        if (!(value instanceof String text)) {
            throw new ToolFailure(name + " must be a string");
        }
        return text;
    }
}

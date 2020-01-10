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
package org.apache.groovy.util;

public class SystemUtil {
    /**
     * Sets a system property from a {@code name=value} String.
     * If no '=' is found, the property is assumed to be a Boolean flag with value {@code true}.
     *
     * @param nameValue the non-null name=value String
     * @return the found name
     * @throws IllegalArgumentException if nameValue is null
     */
    public static String setSystemPropertyFrom(final String nameValue) {
        if (nameValue == null) throw new IllegalArgumentException("argument should not be null");

        String name, value;
        int i = nameValue.indexOf("=");

        if (i == -1) {
            name = nameValue;
            value = Boolean.TRUE.toString();
        } else {
            name = nameValue.substring(0, i);
            value = nameValue.substring(i + 1);
        }
        name = name.trim();

        System.setProperty(name, value);
        return name;
    }

    /**
     * Sets a system property from a {@code name=value} String.
     * If no '=' is found, the property is assumed to be a Boolean flag with value {@code true}.
     * Does nothing if a Security manager is in place which doesn't allow the operation.
     *
     * @param nameValue the non-null name=value String
     * @return the found property name or null if the operation wasn't successful
     * @throws IllegalArgumentException if nameValue is null
     */
    public static String setSystemPropertyFromSafe(final String nameValue) {
        try {
            return setSystemPropertyFrom(nameValue);
        } catch (SecurityException ignore) {
            // suppress exception
        }
        return null;
    }

    /**
     * Retrieves a System property, or returns some default value if:
     * <ul>
     * <li>the property isn't found</li>
     * <li>the property name is null or empty</li>
     * <li>if a security manager exists and its checkPropertyAccess method doesn't allow access to the specified system property.</li>
     * </ul>
     *
     * @param name         the name of the system property.
     * @param defaultValue a default value.
     * @return value of the system property or the default value
     */
    public static String getSystemPropertySafe(String name, String defaultValue) {
        try {
            return System.getProperty(name, defaultValue);
        } catch (SecurityException | NullPointerException | IllegalArgumentException ignore) {
            // suppress exception
        }
        return defaultValue;
    }

    /**
     * Retrieves a System property, or null if:
     * <ul>
     * <li>the property isn't found</li>
     * <li>the property name is null or empty</li>
     * <li>if a security manager exists and its checkPropertyAccess method doesn't allow access to the specified system property.</li>
     * </ul>
     *
     * @param name the name of the system property.
     * @return value of the system property or null
     */
    public static String getSystemPropertySafe(String name) {
        return getSystemPropertySafe(name, null);
    }

    /**
     * Retrieves a Boolean System property, or returns false if:
     * <ul>
     * <li>the property isn't found</li>
     * <li>the property name is null or empty</li>
     * <li>if a security manager exists and its checkPropertyAccess method doesn't allow access to the specified system property.</li>
     * </ul>
     *
     * @param name the name of the system property.
     * @return value of the Boolean system property or false
     */
    public static boolean getBooleanSafe(String name) {
        try {
            return Boolean.getBoolean(name);
        } catch (SecurityException ignore) {
            // suppress exception
        }
        return false;
    }

    /**
     * Retrieves an Integer System property
     *
     * @param name the name of the system property.
     * @param def the default value
     * @return value of the Integer system property or the default value
     */
    public static Integer getIntegerSafe(String name, Integer def) {
        try {
            return Integer.getInteger(name, def);
        } catch (SecurityException ignore) {
            // suppress exception
        }

        return def;
    }

    /**
     * Retrieves an Long System property
     *
     * @param name the name of the system property.
     * @param def the default value
     * @return value of the Long system property or the default value
     */
    public static Long getLongSafe(String name, Long def) {
        try {
            return Long.getLong(name, def);
        } catch (SecurityException ignore) {
            // suppress exception
        }

        return def;
    }
}

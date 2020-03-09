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

import static java.lang.Character.isUpperCase;

public class BeanUtils {
    /**
     * Returns a new String which is the same as the original except the first letter
     * will be lowercase except for some special cases as per JavaBean handling.
     * In particular, if the first two letters are both uppercase, e.g. URL,
     * then no change of case occurs.
     *
     * Originally inspired by the method with the same name in java.lang.Introspector.
     * See also:
     * https://stackoverflow.com/questions/4052840/most-efficient-way-to-make-the-first-character-of-a-string-lower-case/4052914
     *
     * @param property a string representing the name of a JavaBean-like property
     * @return the decapitalized string
     */
    public static String decapitalize(final String property) {
        if (property == null || property.isEmpty()) return property;
        if (property.length() >= 2 && isUpperCase(property.charAt(1)) && isUpperCase(property.charAt(0))) return property;
        final char[] c = property.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    /**
     * This is the complement the behavior of the decapitalize(string) method.
     * We handle names that begin with an initial lowerCase followed by upperCase
     * with special JavaBean behavior (which is to make no change). See GROOVY-3211.
     *
     * @param property the property name to capitalize
     * @return the name capitalized, except when we don't
     */
    public static String capitalize(final String property) {
        final String rest = property.substring(1);

        // Funky rule so that names like 'pNAME' will still work.
        if (Character.isLowerCase(property.charAt(0)) && (rest.length() > 0) && isUpperCase(rest.charAt(0))) {
            return property;
        }

        return property.substring(0, 1).toUpperCase() + rest;
    }

    private BeanUtils() {
    }
}

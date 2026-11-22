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
package org.codehaus.groovy.dummy;

import java.lang.reflect.Array;

/**
 * Methods returning and accepting arrays of a bounded type variable, whose JVM descriptors use the
 * bound ({@code Number}) rather than {@code Object}.
 */
public class Groovy12793Support {

    @SuppressWarnings("unchecked")
    public static <T extends Number> T[] array(Class<T> type) {
        return (T[]) Array.newInstance(type, 2);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> T[][] grid(Class<T> type) {
        return (T[][]) Array.newInstance(type, 2, 3);
    }

    @SafeVarargs
    public static <T extends Number> int count(T... values) {
        return values.length;
    }

    public static <T extends Number> int cells(T[][] grid) {
        int cells = 0;
        for (T[] row : grid) {
            cells += row.length;
        }
        return cells;
    }
}

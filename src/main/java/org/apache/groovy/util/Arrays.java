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

import java.lang.reflect.Array;
import java.util.Objects;

/**
 * Array utilities.
 *
 * @since 3.0.0
 */
public class Arrays {

    /**
     * Concatenate arrays and ignore null array
     *
     * @param arrays arrays to merge
     * @param <T> array component type
     * @return the concatenated array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[]... arrays) {
        if (null == arrays || 0 == arrays.length) return null;

        int resultLength =
                java.util.Arrays.stream(arrays)
                        .filter(Objects::nonNull)
                        .map(e -> e.length)
                        .reduce(0, Integer::sum);
        T[] resultArray = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), resultLength);

        for (int i = 0, n = arrays.length, curr = 0; i < n; i++) {
            T[] array = arrays[i];

            if (null == array) continue;

            int length = array.length;
            System.arraycopy(array, 0, resultArray, curr, length);
            curr += length;
        }

        return resultArray;
    }

    private Arrays() {}
}

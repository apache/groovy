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
package org.codehaus.groovy.vmplugin.v8;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Defines new Groovy methods which appear on normal JDK 8
 * classes inside the Groovy environment.
 *
 * @since 2.5.0
 */
public class PluginDefaultGroovyMethods {

    // No instances, static methods only
    private PluginDefaultGroovyMethods() {
    }

    /**
     * Coerce an Optional instance to a boolean value.
     *
     * @param optional the Optional
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public static boolean asBoolean(Optional<?> optional) {
        return optional.isPresent();
    }

    /**
     * Accumulates the elements of stream into a new List.
     * @param stream the Stream
     * @param <T> the type of element
     * @return a new {@code java.util.List} instance
     */
    public static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.<T>toList());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     * @param stream the Stream
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     */
    public static <T> Set<T> toSet(Stream<T> stream) {
        return stream.collect(Collectors.<T>toSet());
    }

    /**
     * Accumulates the elements of stream into a new List.
     * @param stream the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.List} instance
     */
    public static <T> List<T> toList(BaseStream<T, ? extends BaseStream> stream) {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(stream.iterator(), Spliterator.ORDERED),
                false).collect(Collectors.<T>toList());
    }

    /**
     * Accumulates the elements of stream into a new Set.
     * @param stream the {@code java.util.stream.BaseStream}
     * @param <T> the type of element
     * @return a new {@code java.util.Set} instance
     */
    public static <T> Set<T> toSet(BaseStream<T, ? extends BaseStream> stream) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(stream.iterator(), Spliterator.ORDERED),
                false).collect(Collectors.<T>toSet());
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param <T> The type of the array elements
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static <T> Stream<T> stream(T[] self) {
        return Arrays.stream(self);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Integer> stream(int[] self) {
        Integer[] array = new Integer[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Long> stream(long[] self) {
        Long[] array = new Long[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Double> stream(double[] self) {
        Double[] array = new Double[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Character> stream(char[] self) {
        Character[] array = new Character[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Byte> stream(byte[] self) {
        Byte[] array = new Byte[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Short> stream(short[] self) {
        Short[] array = new Short[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Boolean> stream(boolean[] self) {
        Boolean[] array = new Boolean[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

    /**
     * Returns a sequential {@link Stream} with the specified array as its
     * source.
     *
     * @param self The array, assumed to be unmodified during use
     * @return a {@code Stream} for the array
     */
    public static Stream<Float> stream(float[] self) {
        Float[] array = new Float[self.length];

        for (int i = 0, n = self.length; i < n; i++) {
            array[i] = self[i];
        }

        return Arrays.stream(array);
    }

}

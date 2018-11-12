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
package groovy.lang;

import groovy.util.function.Consumer0;
import groovy.util.function.Consumer1;
import groovy.util.function.Consumer10;
import groovy.util.function.Consumer11;
import groovy.util.function.Consumer12;
import groovy.util.function.Consumer13;
import groovy.util.function.Consumer14;
import groovy.util.function.Consumer15;
import groovy.util.function.Consumer16;
import groovy.util.function.Consumer2;
import groovy.util.function.Consumer3;
import groovy.util.function.Consumer4;
import groovy.util.function.Consumer5;
import groovy.util.function.Consumer6;
import groovy.util.function.Consumer7;
import groovy.util.function.Consumer8;
import groovy.util.function.Consumer9;
import groovy.util.function.Function0;
import groovy.util.function.Function1;
import groovy.util.function.Function10;
import groovy.util.function.Function11;
import groovy.util.function.Function12;
import groovy.util.function.Function13;
import groovy.util.function.Function14;
import groovy.util.function.Function15;
import groovy.util.function.Function16;
import groovy.util.function.Function2;
import groovy.util.function.Function3;
import groovy.util.function.Function4;
import groovy.util.function.Function5;
import groovy.util.function.Function6;
import groovy.util.function.Function7;
import groovy.util.function.Function8;
import groovy.util.function.Function9;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;

/**
 * Represents a list of Objects.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class Tuple<E> extends AbstractList<E> implements Serializable, Cloneable, Comparable<Tuple<E>> {
    private static final long serialVersionUID = -6707770506387821031L;
    private final E[] contents;

    @SafeVarargs
    public Tuple(E... contents) {
        if (contents == null) throw new NullPointerException();
        this.contents = contents;
    }

    public Tuple(Tuple<E> tuple) {
        this.contents = tuple.contents;
    }

    @Override
    public E get(int index) {
        return contents[index];
    }

    @Override
    public int size() {
        return contents.length;
    }

    @Override
    public E[] toArray() {
        return contents;
    }

    /**
     * The degree of this tuple
     */
    public final int degree() {
        return this.size();
    }

    public final <K> Map<K, ?> toMap(Function1<? super Integer, ? extends K> keyMapper) {
        Object[] array = toArray();
        Map<K, Object> result = new LinkedHashMap<>();

        for (int i = 0, n = array.length; i < n; i++) {
            result.put(keyMapper.apply(i), array[i]);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        int size = toIndex - fromIndex;
        E[] newContent = (E[]) new Object[size];
        System.arraycopy(contents, fromIndex, newContent, 0, size);
        return new Tuple<>(newContent);
    }

    public Tuple<E> subTuple(int fromIndex, int toIndex) {
        return (Tuple<E>) subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;

        Tuple that = (Tuple) o;
        int size = size();
        if (size != that.size()) return false;
        for (int i = 0; i < size; i++) {
            if (!DefaultTypeTransformation.compareEqual(get(i), that.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }

    @Override
    public int compareTo(Tuple<E> other) {
        int thisSize = this.size();
        int otherSize = other.size();

        for (int i = 0, n = Math.min(thisSize, otherSize); i < n; i++) {
            int result = compare(this.get(i), other.get(i));
            if (result != 0) {
                return result;
            }
        }

        return Integer.compare(thisSize, otherSize);
    }

    @Override
    public Tuple<E> clone() {
        return new Tuple<>(this);
    }

    @SuppressWarnings("unchecked")
    private static <T> int compare(T t1, T t2) {
        return t1 == null && t2 == null
                ? 0
                : t1 == null
                ? 1
                : t2 == null
                ? -1
                : ((Comparable<T>) t1).compareTo(t2);
    }


    /**
     * Construct a tuple of degree 0.
     */
    public static Tuple0 tuple() {
        return new Tuple0();
    }

    /**
     * Construct a tuple of degree 1.
     */
    public static <T1> Tuple1<T1> tuple(T1 v1) {
        return new Tuple1<>(v1);
    }

    /**
     * Construct a tuple of degree 2.
     */
    public static <T1, T2> Tuple2<T1, T2> tuple(T1 v1, T2 v2) {
        return new Tuple2<>(v1, v2);
    }

    /**
     * Construct a tuple of degree 3.
     */
    public static <T1, T2, T3> Tuple3<T1, T2, T3> tuple(T1 v1, T2 v2, T3 v3) {
        return new Tuple3<>(v1, v2, v3);
    }

    /**
     * Construct a tuple of degree 4.
     */
    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> tuple(T1 v1, T2 v2, T3 v3, T4 v4) {
        return new Tuple4<>(v1, v2, v3, v4);
    }

    /**
     * Construct a tuple of degree 5.
     */
    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        return new Tuple5<>(v1, v2, v3, v4, v5);
    }

    /**
     * Construct a tuple of degree 6.
     */
    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        return new Tuple6<>(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Construct a tuple of degree 7.
     */
    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7) {
        return new Tuple7<>(v1, v2, v3, v4, v5, v6, v7);
    }

    /**
     * Construct a tuple of degree 8.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8) {
        return new Tuple8<>(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Construct a tuple of degree 9.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9) {
        return new Tuple9<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
    }

    /**
     * Construct a tuple of degree 10.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10) {
        return new Tuple10<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
    }

    /**
     * Construct a tuple of degree 11.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11) {
        return new Tuple11<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
    }

    /**
     * Construct a tuple of degree 12.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12) {
        return new Tuple12<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
    }

    /**
     * Construct a tuple of degree 13.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Construct a tuple of degree 14.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14) {
        return new Tuple14<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Construct a tuple of degree 15.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14, T15 v15) {
        return new Tuple15<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Construct a tuple of degree 16.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> tuple(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14, T15 v15, T16 v16) {
        return new Tuple16<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
    }


    /**
     * Construct a tuple function of degree 0.
     */
    public static <R> Function1<Tuple0, R> function(Function0<R> function) {
        return t -> function.get();
    }

    /**
     * Construct a tuple function of degree 1.
     */
    public static <T1, R> Function1<Tuple1<T1>, R> function(Function1<T1, R> function) {
        return t -> function.apply(t.v1());
    }

    /**
     * Construct a tuple function of degree 2.
     */
    public static <T1, T2, R> Function1<Tuple2<T1, T2>, R> function(Function2<T1, T2, R> function) {
        return t -> function.apply(t.v1(), t.v2());
    }

    /**
     * Construct a tuple function of degree 3.
     */
    public static <T1, T2, T3, R> Function1<Tuple3<T1, T2, T3>, R> function(Function3<T1, T2, T3, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3());
    }

    /**
     * Construct a tuple function of degree 4.
     */
    public static <T1, T2, T3, T4, R> Function1<Tuple4<T1, T2, T3, T4>, R> function(Function4<T1, T2, T3, T4, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4());
    }

    /**
     * Construct a tuple function of degree 5.
     */
    public static <T1, T2, T3, T4, T5, R> Function1<Tuple5<T1, T2, T3, T4, T5>, R> function(Function5<T1, T2, T3, T4, T5, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5());
    }

    /**
     * Construct a tuple function of degree 6.
     */
    public static <T1, T2, T3, T4, T5, T6, R> Function1<Tuple6<T1, T2, T3, T4, T5, T6>, R> function(Function6<T1, T2, T3, T4, T5, T6, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6());
    }

    /**
     * Construct a tuple function of degree 7.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, R> Function1<Tuple7<T1, T2, T3, T4, T5, T6, T7>, R> function(Function7<T1, T2, T3, T4, T5, T6, T7, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7());
    }

    /**
     * Construct a tuple function of degree 8.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function1<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>, R> function(Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8());
    }

    /**
     * Construct a tuple function of degree 9.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Function1<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>, R> function(Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9());
    }

    /**
     * Construct a tuple function of degree 10.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> Function1<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>, R> function(Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10());
    }

    /**
     * Construct a tuple function of degree 11.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> Function1<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>, R> function(Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11());
    }

    /**
     * Construct a tuple function of degree 12.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> Function1<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>, R> function(Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12());
    }

    /**
     * Construct a tuple function of degree 13.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> Function1<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>, R> function(Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13());
    }

    /**
     * Construct a tuple function of degree 14.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> Function1<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>, R> function(Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13(), t.v14());
    }

    /**
     * Construct a tuple function of degree 15.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> Function1<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>, R> function(Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13(), t.v14(), t.v15());
    }

    /**
     * Construct a tuple function of degree 16.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> Function1<Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>, R> function(Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> function) {
        return t -> function.apply(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13(), t.v14(), t.v15(), t.v16());
    }


    /**
     * Construct a tuple consumer of degree 0.
     */
    public static Consumer1<Tuple0> consumer(Consumer0 consumer) {
        return t -> consumer.run();
    }

    /**
     * Construct a tuple consumer of degree 1.
     */
    public static <T1> Consumer1<Tuple1<T1>> consumer(Consumer1<T1> consumer) {
        return t -> consumer.accept(t.v1());
    }

    /**
     * Construct a tuple consumer of degree 2.
     */
    public static <T1, T2> Consumer1<Tuple2<T1, T2>> consumer(Consumer2<T1, T2> consumer) {
        return t -> consumer.accept(t.v1(), t.v2());
    }

    /**
     * Construct a tuple consumer of degree 3.
     */
    public static <T1, T2, T3> Consumer1<Tuple3<T1, T2, T3>> consumer(Consumer3<T1, T2, T3> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3());
    }

    /**
     * Construct a tuple consumer of degree 4.
     */
    public static <T1, T2, T3, T4> Consumer1<Tuple4<T1, T2, T3, T4>> consumer(Consumer4<T1, T2, T3, T4> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4());
    }

    /**
     * Construct a tuple consumer of degree 5.
     */
    public static <T1, T2, T3, T4, T5> Consumer1<Tuple5<T1, T2, T3, T4, T5>> consumer(Consumer5<T1, T2, T3, T4, T5> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5());
    }

    /**
     * Construct a tuple consumer of degree 6.
     */
    public static <T1, T2, T3, T4, T5, T6> Consumer1<Tuple6<T1, T2, T3, T4, T5, T6>> consumer(Consumer6<T1, T2, T3, T4, T5, T6> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6());
    }

    /**
     * Construct a tuple consumer of degree 7.
     */
    public static <T1, T2, T3, T4, T5, T6, T7> Consumer1<Tuple7<T1, T2, T3, T4, T5, T6, T7>> consumer(Consumer7<T1, T2, T3, T4, T5, T6, T7> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7());
    }

    /**
     * Construct a tuple consumer of degree 8.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8> Consumer1<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> consumer(Consumer8<T1, T2, T3, T4, T5, T6, T7, T8> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8());
    }

    /**
     * Construct a tuple consumer of degree 9.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Consumer1<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> consumer(Consumer9<T1, T2, T3, T4, T5, T6, T7, T8, T9> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9());
    }

    /**
     * Construct a tuple consumer of degree 10.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Consumer1<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>> consumer(Consumer10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10());
    }

    /**
     * Construct a tuple consumer of degree 11.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Consumer1<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>> consumer(Consumer11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11());
    }

    /**
     * Construct a tuple consumer of degree 12.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Consumer1<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>> consumer(Consumer12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12());
    }

    /**
     * Construct a tuple consumer of degree 13.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Consumer1<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> consumer(Consumer13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13());
    }

    /**
     * Construct a tuple consumer of degree 14.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Consumer1<Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>> consumer(Consumer14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13(), t.v14());
    }

    /**
     * Construct a tuple consumer of degree 15.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Consumer1<Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>> consumer(Consumer15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13(), t.v14(), t.v15());
    }

    /**
     * Construct a tuple consumer of degree 16.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Consumer1<Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>> consumer(Consumer16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> consumer) {
        return t -> consumer.accept(t.v1(), t.v2(), t.v3(), t.v4(), t.v5(), t.v6(), t.v7(), t.v8(), t.v9(), t.v10(), t.v11(), t.v12(), t.v13(), t.v14(), t.v15(), t.v16());
    }

    /**
     * Construct a tuple collector of degree 1.
     */
    public static <T, A1, D1> Collector<T, Tuple1<A1>, Tuple1<D1>> collectors(
            Collector<? super T, A1, D1> collector1
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 2.
     */
    public static <T, A1, A2, D1, D2> Collector<T, Tuple2<A1, A2>, Tuple2<D1, D2>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 3.
     */
    public static <T, A1, A2, A3, D1, D2, D3> Collector<T, Tuple3<A1, A2, A3>, Tuple3<D1, D2, D3>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 4.
     */
    public static <T, A1, A2, A3, A4, D1, D2, D3, D4> Collector<T, Tuple4<A1, A2, A3, A4>, Tuple4<D1, D2, D3, D4>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 5.
     */
    public static <T, A1, A2, A3, A4, A5, D1, D2, D3, D4, D5> Collector<T, Tuple5<A1, A2, A3, A4, A5>, Tuple5<D1, D2, D3, D4, D5>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 6.
     */
    public static <T, A1, A2, A3, A4, A5, A6, D1, D2, D3, D4, D5, D6> Collector<T, Tuple6<A1, A2, A3, A4, A5, A6>, Tuple6<D1, D2, D3, D4, D5, D6>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 7.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, D1, D2, D3, D4, D5, D6, D7> Collector<T, Tuple7<A1, A2, A3, A4, A5, A6, A7>, Tuple7<D1, D2, D3, D4, D5, D6, D7>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 8.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, D1, D2, D3, D4, D5, D6, D7, D8> Collector<T, Tuple8<A1, A2, A3, A4, A5, A6, A7, A8>, Tuple8<D1, D2, D3, D4, D5, D6, D7, D8>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 9.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, D1, D2, D3, D4, D5, D6, D7, D8, D9> Collector<T, Tuple9<A1, A2, A3, A4, A5, A6, A7, A8, A9>, Tuple9<D1, D2, D3, D4, D5, D6, D7, D8, D9>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 10.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, D1, D2, D3, D4, D5, D6, D7, D8, D9, D10> Collector<T, Tuple10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10>, Tuple10<D1, D2, D3, D4, D5, D6, D7, D8, D9, D10>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
            , Collector<? super T, A10, D10> collector10
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                        , collector10.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                    collector10.accumulator().accept(a.v10(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                        , collector10.combiner().apply(a1.v10(), a2.v10())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                        , collector10.finisher().apply(a.v10())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 11.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11> Collector<T, Tuple11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11>, Tuple11<D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
            , Collector<? super T, A10, D10> collector10
            , Collector<? super T, A11, D11> collector11
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                        , collector10.supplier().get()
                        , collector11.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                    collector10.accumulator().accept(a.v10(), t);
                    collector11.accumulator().accept(a.v11(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                        , collector10.combiner().apply(a1.v10(), a2.v10())
                        , collector11.combiner().apply(a1.v11(), a2.v11())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                        , collector10.finisher().apply(a.v10())
                        , collector11.finisher().apply(a.v11())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 12.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12> Collector<T, Tuple12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>, Tuple12<D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
            , Collector<? super T, A10, D10> collector10
            , Collector<? super T, A11, D11> collector11
            , Collector<? super T, A12, D12> collector12
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                        , collector10.supplier().get()
                        , collector11.supplier().get()
                        , collector12.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                    collector10.accumulator().accept(a.v10(), t);
                    collector11.accumulator().accept(a.v11(), t);
                    collector12.accumulator().accept(a.v12(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                        , collector10.combiner().apply(a1.v10(), a2.v10())
                        , collector11.combiner().apply(a1.v11(), a2.v11())
                        , collector12.combiner().apply(a1.v12(), a2.v12())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                        , collector10.finisher().apply(a.v10())
                        , collector11.finisher().apply(a.v11())
                        , collector12.finisher().apply(a.v12())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 13.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13> Collector<T, Tuple13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13>, Tuple13<D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
            , Collector<? super T, A10, D10> collector10
            , Collector<? super T, A11, D11> collector11
            , Collector<? super T, A12, D12> collector12
            , Collector<? super T, A13, D13> collector13
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                        , collector10.supplier().get()
                        , collector11.supplier().get()
                        , collector12.supplier().get()
                        , collector13.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                    collector10.accumulator().accept(a.v10(), t);
                    collector11.accumulator().accept(a.v11(), t);
                    collector12.accumulator().accept(a.v12(), t);
                    collector13.accumulator().accept(a.v13(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                        , collector10.combiner().apply(a1.v10(), a2.v10())
                        , collector11.combiner().apply(a1.v11(), a2.v11())
                        , collector12.combiner().apply(a1.v12(), a2.v12())
                        , collector13.combiner().apply(a1.v13(), a2.v13())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                        , collector10.finisher().apply(a.v10())
                        , collector11.finisher().apply(a.v11())
                        , collector12.finisher().apply(a.v12())
                        , collector13.finisher().apply(a.v13())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 14.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13, D14> Collector<T, Tuple14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14>, Tuple14<D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13, D14>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
            , Collector<? super T, A10, D10> collector10
            , Collector<? super T, A11, D11> collector11
            , Collector<? super T, A12, D12> collector12
            , Collector<? super T, A13, D13> collector13
            , Collector<? super T, A14, D14> collector14
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                        , collector10.supplier().get()
                        , collector11.supplier().get()
                        , collector12.supplier().get()
                        , collector13.supplier().get()
                        , collector14.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                    collector10.accumulator().accept(a.v10(), t);
                    collector11.accumulator().accept(a.v11(), t);
                    collector12.accumulator().accept(a.v12(), t);
                    collector13.accumulator().accept(a.v13(), t);
                    collector14.accumulator().accept(a.v14(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                        , collector10.combiner().apply(a1.v10(), a2.v10())
                        , collector11.combiner().apply(a1.v11(), a2.v11())
                        , collector12.combiner().apply(a1.v12(), a2.v12())
                        , collector13.combiner().apply(a1.v13(), a2.v13())
                        , collector14.combiner().apply(a1.v14(), a2.v14())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                        , collector10.finisher().apply(a.v10())
                        , collector11.finisher().apply(a.v11())
                        , collector12.finisher().apply(a.v12())
                        , collector13.finisher().apply(a.v13())
                        , collector14.finisher().apply(a.v14())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 15.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13, D14, D15> Collector<T, Tuple15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15>, Tuple15<D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13, D14, D15>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
            , Collector<? super T, A10, D10> collector10
            , Collector<? super T, A11, D11> collector11
            , Collector<? super T, A12, D12> collector12
            , Collector<? super T, A13, D13> collector13
            , Collector<? super T, A14, D14> collector14
            , Collector<? super T, A15, D15> collector15
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                        , collector10.supplier().get()
                        , collector11.supplier().get()
                        , collector12.supplier().get()
                        , collector13.supplier().get()
                        , collector14.supplier().get()
                        , collector15.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                    collector10.accumulator().accept(a.v10(), t);
                    collector11.accumulator().accept(a.v11(), t);
                    collector12.accumulator().accept(a.v12(), t);
                    collector13.accumulator().accept(a.v13(), t);
                    collector14.accumulator().accept(a.v14(), t);
                    collector15.accumulator().accept(a.v15(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                        , collector10.combiner().apply(a1.v10(), a2.v10())
                        , collector11.combiner().apply(a1.v11(), a2.v11())
                        , collector12.combiner().apply(a1.v12(), a2.v12())
                        , collector13.combiner().apply(a1.v13(), a2.v13())
                        , collector14.combiner().apply(a1.v14(), a2.v14())
                        , collector15.combiner().apply(a1.v15(), a2.v15())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                        , collector10.finisher().apply(a.v10())
                        , collector11.finisher().apply(a.v11())
                        , collector12.finisher().apply(a.v12())
                        , collector13.finisher().apply(a.v13())
                        , collector14.finisher().apply(a.v14())
                        , collector15.finisher().apply(a.v15())
                )
        );
    }

    /**
     * Construct a tuple collector of degree 16.
     */
    public static <T, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13, D14, D15, D16> Collector<T, Tuple16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16>, Tuple16<D1, D2, D3, D4, D5, D6, D7, D8, D9, D10, D11, D12, D13, D14, D15, D16>> collectors(
            Collector<? super T, A1, D1> collector1
            , Collector<? super T, A2, D2> collector2
            , Collector<? super T, A3, D3> collector3
            , Collector<? super T, A4, D4> collector4
            , Collector<? super T, A5, D5> collector5
            , Collector<? super T, A6, D6> collector6
            , Collector<? super T, A7, D7> collector7
            , Collector<? super T, A8, D8> collector8
            , Collector<? super T, A9, D9> collector9
            , Collector<? super T, A10, D10> collector10
            , Collector<? super T, A11, D11> collector11
            , Collector<? super T, A12, D12> collector12
            , Collector<? super T, A13, D13> collector13
            , Collector<? super T, A14, D14> collector14
            , Collector<? super T, A15, D15> collector15
            , Collector<? super T, A16, D16> collector16
    ) {
        return Collector.of(
                () -> tuple(
                        collector1.supplier().get()
                        , collector2.supplier().get()
                        , collector3.supplier().get()
                        , collector4.supplier().get()
                        , collector5.supplier().get()
                        , collector6.supplier().get()
                        , collector7.supplier().get()
                        , collector8.supplier().get()
                        , collector9.supplier().get()
                        , collector10.supplier().get()
                        , collector11.supplier().get()
                        , collector12.supplier().get()
                        , collector13.supplier().get()
                        , collector14.supplier().get()
                        , collector15.supplier().get()
                        , collector16.supplier().get()
                ),
                (a, t) -> {
                    collector1.accumulator().accept(a.v1(), t);
                    collector2.accumulator().accept(a.v2(), t);
                    collector3.accumulator().accept(a.v3(), t);
                    collector4.accumulator().accept(a.v4(), t);
                    collector5.accumulator().accept(a.v5(), t);
                    collector6.accumulator().accept(a.v6(), t);
                    collector7.accumulator().accept(a.v7(), t);
                    collector8.accumulator().accept(a.v8(), t);
                    collector9.accumulator().accept(a.v9(), t);
                    collector10.accumulator().accept(a.v10(), t);
                    collector11.accumulator().accept(a.v11(), t);
                    collector12.accumulator().accept(a.v12(), t);
                    collector13.accumulator().accept(a.v13(), t);
                    collector14.accumulator().accept(a.v14(), t);
                    collector15.accumulator().accept(a.v15(), t);
                    collector16.accumulator().accept(a.v16(), t);
                },
                (a1, a2) -> tuple(
                        collector1.combiner().apply(a1.v1(), a2.v1())
                        , collector2.combiner().apply(a1.v2(), a2.v2())
                        , collector3.combiner().apply(a1.v3(), a2.v3())
                        , collector4.combiner().apply(a1.v4(), a2.v4())
                        , collector5.combiner().apply(a1.v5(), a2.v5())
                        , collector6.combiner().apply(a1.v6(), a2.v6())
                        , collector7.combiner().apply(a1.v7(), a2.v7())
                        , collector8.combiner().apply(a1.v8(), a2.v8())
                        , collector9.combiner().apply(a1.v9(), a2.v9())
                        , collector10.combiner().apply(a1.v10(), a2.v10())
                        , collector11.combiner().apply(a1.v11(), a2.v11())
                        , collector12.combiner().apply(a1.v12(), a2.v12())
                        , collector13.combiner().apply(a1.v13(), a2.v13())
                        , collector14.combiner().apply(a1.v14(), a2.v14())
                        , collector15.combiner().apply(a1.v15(), a2.v15())
                        , collector16.combiner().apply(a1.v16(), a2.v16())
                ),
                a -> tuple(
                        collector1.finisher().apply(a.v1())
                        , collector2.finisher().apply(a.v2())
                        , collector3.finisher().apply(a.v3())
                        , collector4.finisher().apply(a.v4())
                        , collector5.finisher().apply(a.v5())
                        , collector6.finisher().apply(a.v6())
                        , collector7.finisher().apply(a.v7())
                        , collector8.finisher().apply(a.v8())
                        , collector9.finisher().apply(a.v9())
                        , collector10.finisher().apply(a.v10())
                        , collector11.finisher().apply(a.v11())
                        , collector12.finisher().apply(a.v12())
                        , collector13.finisher().apply(a.v13())
                        , collector14.finisher().apply(a.v14())
                        , collector15.finisher().apply(a.v15())
                        , collector16.finisher().apply(a.v16())
                )
        );
    }

}

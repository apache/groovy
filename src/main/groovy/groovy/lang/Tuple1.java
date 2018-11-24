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

import groovy.util.function.Function1;

/**
 * Represents a list of 1 typed Object.
 *
 * @since 2.5.0
 */
public final class Tuple1<T1> extends Tuple {
    private static final long serialVersionUID = -4647790147461409603L;
    private final T1 v1;

    public Tuple1(T1 t1) {
        super(t1);

        this.v1 = t1;
    }

    public Tuple1(Tuple1<T1> tuple) {
        this(tuple.v1);
    }

    @Deprecated
    public T1 getFirst() {
        return v1;
    }

    public T1 getV1() {
        return v1;
    }

    /**
     * Concatenate a value to this tuple.
     */
    public final <T2> Tuple2<T1, T2> concat(T2 value) {
        return new Tuple2<>(v1, value);
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2> Tuple2<T1, T2> concat(Tuple1<T2> tuple) {
        return new Tuple2<>(v1, tuple.getV1());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3> Tuple3<T1, T2, T3> concat(Tuple2<T2, T3> tuple) {
        return new Tuple3<>(v1, tuple.getV1(), tuple.getV2());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4> Tuple4<T1, T2, T3, T4> concat(Tuple3<T2, T3, T4> tuple) {
        return new Tuple4<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> concat(Tuple4<T2, T3, T4, T5> tuple) {
        return new Tuple5<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple5<T2, T3, T4, T5, T6> tuple) {
        return new Tuple6<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple6<T2, T3, T4, T5, T6, T7> tuple) {
        return new Tuple7<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple7<T2, T3, T4, T5, T6, T7, T8> tuple) {
        return new Tuple8<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> concat(Tuple8<T2, T3, T4, T5, T6, T7, T8, T9> tuple) {
        return new Tuple9<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> concat(Tuple9<T2, T3, T4, T5, T6, T7, T8, T9, T10> tuple) {
        return new Tuple10<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> concat(Tuple10<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> tuple) {
        return new Tuple11<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> concat(Tuple11<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> tuple) {
        return new Tuple12<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10(), tuple.getV11());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> concat(Tuple12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> tuple) {
        return new Tuple13<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10(), tuple.getV11(), tuple.getV12());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> concat(Tuple13<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> tuple) {
        return new Tuple14<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10(), tuple.getV11(), tuple.getV12(), tuple.getV13());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> concat(Tuple14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> tuple) {
        return new Tuple15<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10(), tuple.getV11(), tuple.getV12(), tuple.getV13(), tuple.getV14());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> concat(Tuple15<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> tuple) {
        return new Tuple16<>(v1, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10(), tuple.getV11(), tuple.getV12(), tuple.getV13(), tuple.getV14(), tuple.getV15());
    }

    /**
     * Split this tuple into two tuples of degree 0 and 1.
     */
    public final Tuple2<Tuple0, Tuple1<T1>> split0() {
        return new Tuple2<>(limit0(), skip0());
    }

    /**
     * Split this tuple into two tuples of degree 1 and 0.
     */
    public final Tuple2<Tuple1<T1>, Tuple0> split1() {
        return new Tuple2<>(limit1(), skip1());
    }

    /**
     * Limit this tuple to degree 0.
     */
    public final Tuple0 limit0() {
        return Tuple0.INSTANCE;
    }

    /**
     * Limit this tuple to degree 1.
     */
    public final Tuple1<T1> limit1() {
        return this;
    }

    /**
     * Skip 0 degrees from this tuple.
     */
    public final Tuple1<T1> skip0() {
        return this;
    }

    /**
     * Skip 1 degrees from this tuple.
     */
    public final Tuple0 skip1() {
        return Tuple0.INSTANCE;
    }

    /**
     * Apply this tuple as arguments to a function.
     */
    public final <R> R map(Function1<? super T1, ? extends R> function) {
        return function.apply(v1);
    }

    /**
     * Apply attribute 1 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U1> Tuple1<U1> map1(Function1<? super T1, ? extends U1> function) {
        return new Tuple1<>(function.apply(v1));
    }

    @Override
    public Tuple1<T1> clone() {
        return new Tuple1<>(this);
    }
}

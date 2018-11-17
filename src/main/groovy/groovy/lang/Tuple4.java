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
import groovy.util.function.Function4;

/**
 * Represents a list of 4 typed Objects.
 *
 * @since 2.5.0
 */
public class Tuple4<T1, T2, T3, T4> extends Tuple {
    private static final long serialVersionUID = -7788878731471377207L;
    private final T1 v1;
    private final T2 v2;
    private final T3 v3;
    private final T4 v4;

    public Tuple4(T1 v1, T2 v2, T3 v3, T4 v4) {
        super(v1, v2, v3, v4);

        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
    }

    public Tuple4(Tuple4<T1, T2, T3, T4> tuple) {
        this(tuple.v1, tuple.v2, tuple.v3, tuple.v4);
    }

    @Deprecated
    public T1 getFirst() {
        return v1;
    }

    @Deprecated
    public T2 getSecond() {
        return v2;
    }

    @Deprecated
    public T3 getThird() {
        return v3;
    }

    @Deprecated
    public T4 getFourth() {
        return v4;
    }

    public T1 getV1() {
        return v1;
    }

    public T2 getV2() {
        return v2;
    }

    public T3 getV3() {
        return v3;
    }

    public T4 getV4() {
        return v4;
    }


    /**
     * Concatenate a value to this tuple.
     */
    public final <T5> Tuple5<T1, T2, T3, T4, T5> concat(T5 value) {
        return new Tuple5<>(v1, v2, v3, v4, value);
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5> Tuple5<T1, T2, T3, T4, T5> concat(Tuple1<T5> tuple) {
        return new Tuple5<>(v1, v2, v3, v4, tuple.getV1());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple2<T5, T6> tuple) {
        return new Tuple6<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple3<T5, T6, T7> tuple) {
        return new Tuple7<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple4<T5, T6, T7, T8> tuple) {
        return new Tuple8<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> concat(Tuple5<T5, T6, T7, T8, T9> tuple) {
        return new Tuple9<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> concat(Tuple6<T5, T6, T7, T8, T9, T10> tuple) {
        return new Tuple10<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9, T10, T11> Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> concat(Tuple7<T5, T6, T7, T8, T9, T10, T11> tuple) {
        return new Tuple11<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9, T10, T11, T12> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> concat(Tuple8<T5, T6, T7, T8, T9, T10, T11, T12> tuple) {
        return new Tuple12<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9, T10, T11, T12, T13> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> concat(Tuple9<T5, T6, T7, T8, T9, T10, T11, T12, T13> tuple) {
        return new Tuple13<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> concat(Tuple10<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> tuple) {
        return new Tuple14<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> concat(Tuple11<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> tuple) {
        return new Tuple15<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10(), tuple.getV11());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> concat(Tuple12<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> tuple) {
        return new Tuple16<>(v1, v2, v3, v4, tuple.getV1(), tuple.getV2(), tuple.getV3(), tuple.getV4(), tuple.getV5(), tuple.getV6(), tuple.getV7(), tuple.getV8(), tuple.getV9(), tuple.getV10(), tuple.getV11(), tuple.getV12());
    }

    /**
     * Split this tuple into two tuples of degree 0 and 4.
     */
    public final Tuple2<Tuple0, Tuple4<T1, T2, T3, T4>> split0() {
        return new Tuple2<>(limit0(), skip0());
    }

    /**
     * Split this tuple into two tuples of degree 1 and 3.
     */
    public final Tuple2<Tuple1<T1>, Tuple3<T2, T3, T4>> split1() {
        return new Tuple2<>(limit1(), skip1());
    }

    /**
     * Split this tuple into two tuples of degree 2 and 2.
     */
    public final Tuple2<Tuple2<T1, T2>, Tuple2<T3, T4>> split2() {
        return new Tuple2<>(limit2(), skip2());
    }

    /**
     * Split this tuple into two tuples of degree 3 and 1.
     */
    public final Tuple2<Tuple3<T1, T2, T3>, Tuple1<T4>> split3() {
        return new Tuple2<>(limit3(), skip3());
    }

    /**
     * Split this tuple into two tuples of degree 4 and 0.
     */
    public final Tuple2<Tuple4<T1, T2, T3, T4>, Tuple0> split4() {
        return new Tuple2<>(limit4(), skip4());
    }

    /**
     * Limit this tuple to degree 0.
     */
    public final Tuple0 limit0() {
        return new Tuple0();
    }

    /**
     * Limit this tuple to degree 1.
     */
    public final Tuple1<T1> limit1() {
        return new Tuple1<>(v1);
    }

    /**
     * Limit this tuple to degree 2.
     */
    public final Tuple2<T1, T2> limit2() {
        return new Tuple2<>(v1, v2);
    }

    /**
     * Limit this tuple to degree 3.
     */
    public final Tuple3<T1, T2, T3> limit3() {
        return new Tuple3<>(v1, v2, v3);
    }

    /**
     * Limit this tuple to degree 4.
     */
    public final Tuple4<T1, T2, T3, T4> limit4() {
        return this;
    }

    /**
     * Skip 0 degrees from this tuple.
     */
    public final Tuple4<T1, T2, T3, T4> skip0() {
        return this;
    }

    /**
     * Skip 1 degrees from this tuple.
     */
    public final Tuple3<T2, T3, T4> skip1() {
        return new Tuple3<>(v2, v3, v4);
    }

    /**
     * Skip 2 degrees from this tuple.
     */
    public final Tuple2<T3, T4> skip2() {
        return new Tuple2<>(v3, v4);
    }

    /**
     * Skip 3 degrees from this tuple.
     */
    public final Tuple1<T4> skip3() {
        return new Tuple1<>(v4);
    }

    /**
     * Skip 4 degrees from this tuple.
     */
    public final Tuple0 skip4() {
        return new Tuple0();
    }

    /**
     * Apply this tuple as arguments to a function.
     */
    public final <R> R map(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> function) {
        return function.apply(v1, v2, v3, v4);
    }

    /**
     * Apply attribute 1 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U1> Tuple4<U1, T2, T3, T4> map1(Function1<? super T1, ? extends U1> function) {
        return new Tuple4<>(function.apply(v1), v2, v3, v4);
    }

    /**
     * Apply attribute 2 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U2> Tuple4<T1, U2, T3, T4> map2(Function1<? super T2, ? extends U2> function) {
        return new Tuple4<>(v1, function.apply(v2), v3, v4);
    }

    /**
     * Apply attribute 3 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U3> Tuple4<T1, T2, U3, T4> map3(Function1<? super T3, ? extends U3> function) {
        return new Tuple4<>(v1, v2, function.apply(v3), v4);
    }

    /**
     * Apply attribute 4 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U4> Tuple4<T1, T2, T3, U4> map4(Function1<? super T4, ? extends U4> function) {
        return new Tuple4<>(v1, v2, v3, function.apply(v4));
    }

    @Override
    public Tuple4<T1, T2, T3, T4> clone() {
        return new Tuple4<>(this);
    }
}

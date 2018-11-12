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
import groovy.util.function.Function3;

/**
 * Represents a list of 3 typed Objects.
 *
 * @since 2.5.0
 */
public class Tuple3<T1, T2, T3> extends Tuple {
    private static final long serialVersionUID = 8469774237154310687L;
    private final T1 v1;
    private final T2 v2;
    private final T3 v3;

    public Tuple3(T1 v1, T2 v2, T3 v3) {
        super(v1, v2, v3);

        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public Tuple3(Tuple3<T1, T2, T3> tuple) {
        this(tuple.v1, tuple.v2, tuple.v3);
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

    public T1 v1() {
        return v1;
    }

    public T2 v2() {
        return v2;
    }

    public T3 v3() {
        return v3;
    }

    /**
     * Concatenate a value to this tuple.
     */
    public final <T4> Tuple4<T1, T2, T3, T4> concat(T4 value) {
        return new Tuple4<>(v1, v2, v3, value);
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4> Tuple4<T1, T2, T3, T4> concat(Tuple1<T4> tuple) {
        return new Tuple4<>(v1, v2, v3, tuple.v1());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5> Tuple5<T1, T2, T3, T4, T5> concat(Tuple2<T4, T5> tuple) {
        return new Tuple5<>(v1, v2, v3, tuple.v1(), tuple.v2());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple3<T4, T5, T6> tuple) {
        return new Tuple6<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple4<T4, T5, T6, T7> tuple) {
        return new Tuple7<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> concat(Tuple5<T4, T5, T6, T7, T8> tuple) {
        return new Tuple8<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9> Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> concat(Tuple6<T4, T5, T6, T7, T8, T9> tuple) {
        return new Tuple9<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9, T10> Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> concat(Tuple7<T4, T5, T6, T7, T8, T9, T10> tuple) {
        return new Tuple10<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6(), tuple.v7());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9, T10, T11> Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> concat(Tuple8<T4, T5, T6, T7, T8, T9, T10, T11> tuple) {
        return new Tuple11<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6(), tuple.v7(), tuple.v8());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9, T10, T11, T12> Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> concat(Tuple9<T4, T5, T6, T7, T8, T9, T10, T11, T12> tuple) {
        return new Tuple12<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6(), tuple.v7(), tuple.v8(), tuple.v9());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> concat(Tuple10<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> tuple) {
        return new Tuple13<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6(), tuple.v7(), tuple.v8(), tuple.v9(), tuple.v10());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> concat(Tuple11<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> tuple) {
        return new Tuple14<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6(), tuple.v7(), tuple.v8(), tuple.v9(), tuple.v10(), tuple.v11());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> concat(Tuple12<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> tuple) {
        return new Tuple15<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6(), tuple.v7(), tuple.v8(), tuple.v9(), tuple.v10(), tuple.v11(), tuple.v12());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> concat(Tuple13<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> tuple) {
        return new Tuple16<>(v1, v2, v3, tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5(), tuple.v6(), tuple.v7(), tuple.v8(), tuple.v9(), tuple.v10(), tuple.v11(), tuple.v12(), tuple.v13());
    }

    /**
     * Split this tuple into two tuples of degree 0 and 3.
     */
    public final Tuple2<Tuple0, Tuple3<T1, T2, T3>> split0() {
        return new Tuple2<>(limit0(), skip0());
    }

    /**
     * Split this tuple into two tuples of degree 1 and 2.
     */
    public final Tuple2<Tuple1<T1>, Tuple2<T2, T3>> split1() {
        return new Tuple2<>(limit1(), skip1());
    }

    /**
     * Split this tuple into two tuples of degree 2 and 1.
     */
    public final Tuple2<Tuple2<T1, T2>, Tuple1<T3>> split2() {
        return new Tuple2<>(limit2(), skip2());
    }

    /**
     * Split this tuple into two tuples of degree 3 and 0.
     */
    public final Tuple2<Tuple3<T1, T2, T3>, Tuple0> split3() {
        return new Tuple2<>(limit3(), skip3());
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
        return this;
    }

    /**
     * Skip 0 degrees from this tuple.
     */
    public final Tuple3<T1, T2, T3> skip0() {
        return this;
    }

    /**
     * Skip 1 degrees from this tuple.
     */
    public final Tuple2<T2, T3> skip1() {
        return new Tuple2<>(v2, v3);
    }

    /**
     * Skip 2 degrees from this tuple.
     */
    public final Tuple1<T3> skip2() {
        return new Tuple1<>(v3);
    }

    /**
     * Skip 3 degrees from this tuple.
     */
    public final Tuple0 skip3() {
        return new Tuple0();
    }

    /**
     * Apply this tuple as arguments to a function.
     */
    public final <R> R map(Function3<? super T1, ? super T2, ? super T3, ? extends R> function) {
        return function.apply(v1, v2, v3);
    }

    /**
     * Apply attribute 1 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U1> Tuple3<U1, T2, T3> map1(Function1<? super T1, ? extends U1> function) {
        return new Tuple3<>(function.apply(v1), v2, v3);
    }

    /**
     * Apply attribute 2 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U2> Tuple3<T1, U2, T3> map2(Function1<? super T2, ? extends U2> function) {
        return new Tuple3<>(v1, function.apply(v2), v3);
    }

    /**
     * Apply attribute 3 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U3> Tuple3<T1, T2, U3> map3(Function1<? super T3, ? extends U3> function) {
        return new Tuple3<>(v1, v2, function.apply(v3));
    }


    @Override
    public Tuple3<T1, T2, T3> clone() {
        return new Tuple3<>(this);
    }
}

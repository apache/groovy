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
import groovy.util.function.Function13;

/**
 * Represents a list of 13 typed Objects.
 *
 * @since 3.0.0
 */
public class Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> extends Tuple {
    private static final long serialVersionUID = -3709443502209750114L;
    private final T1 v1;
    private final T2 v2;
    private final T3 v3;
    private final T4 v4;
    private final T5 v5;
    private final T6 v6;
    private final T7 v7;
    private final T8 v8;
    private final T9 v9;
    private final T10 v10;
    private final T11 v11;
    private final T12 v12;
    private final T13 v13;


    public Tuple13(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13) {
        super(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);

        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
        this.v6 = v6;
        this.v7 = v7;
        this.v8 = v8;
        this.v9 = v9;
        this.v10 = v10;
        this.v11 = v11;
        this.v12 = v12;
        this.v13 = v13;
    }

    public Tuple13(Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> tuple) {
        this(tuple.v1, tuple.v2, tuple.v3, tuple.v4, tuple.v5, tuple.v6, tuple.v7, tuple.v8, tuple.v9, tuple.v10, tuple.v11, tuple.v12, tuple.v13);
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

    public T4 v4() {
        return v4;
    }

    public T5 v5() {
        return v5;
    }

    public T6 v6() {
        return v6;
    }

    public T7 v7() {
        return v7;
    }

    public T8 v8() {
        return v8;
    }

    public T9 v9() {
        return v9;
    }

    public T10 v10() {
        return v10;
    }

    public T11 v11() {
        return v11;
    }

    public T12 v12() {
        return v12;
    }

    public T13 v13() {
        return v13;
    }

    /**
     * Concatenate a value to this tuple.
     */
    public final <T14> Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> concat(T14 value) {
        return new Tuple14<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, value);
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T14> Tuple14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> concat(Tuple1<T14> tuple) {
        return new Tuple14<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, tuple.v1());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T14, T15> Tuple15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> concat(Tuple2<T14, T15> tuple) {
        return new Tuple15<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, tuple.v1(), tuple.v2());
    }

    /**
     * Concatenate a tuple to this tuple.
     */
    public final <T14, T15, T16> Tuple16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> concat(Tuple3<T14, T15, T16> tuple) {
        return new Tuple16<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, tuple.v1(), tuple.v2(), tuple.v3());
    }

    /**
     * Split this tuple into two tuples of degree 0 and 13.
     */
    public final Tuple2<Tuple0, Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> split0() {
        return new Tuple2<>(limit0(), skip0());
    }

    /**
     * Split this tuple into two tuples of degree 1 and 12.
     */
    public final Tuple2<Tuple1<T1>, Tuple12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> split1() {
        return new Tuple2<>(limit1(), skip1());
    }

    /**
     * Split this tuple into two tuples of degree 2 and 11.
     */
    public final Tuple2<Tuple2<T1, T2>, Tuple11<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> split2() {
        return new Tuple2<>(limit2(), skip2());
    }

    /**
     * Split this tuple into two tuples of degree 3 and 10.
     */
    public final Tuple2<Tuple3<T1, T2, T3>, Tuple10<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>> split3() {
        return new Tuple2<>(limit3(), skip3());
    }

    /**
     * Split this tuple into two tuples of degree 4 and 9.
     */
    public final Tuple2<Tuple4<T1, T2, T3, T4>, Tuple9<T5, T6, T7, T8, T9, T10, T11, T12, T13>> split4() {
        return new Tuple2<>(limit4(), skip4());
    }

    /**
     * Split this tuple into two tuples of degree 5 and 8.
     */
    public final Tuple2<Tuple5<T1, T2, T3, T4, T5>, Tuple8<T6, T7, T8, T9, T10, T11, T12, T13>> split5() {
        return new Tuple2<>(limit5(), skip5());
    }

    /**
     * Split this tuple into two tuples of degree 6 and 7.
     */
    public final Tuple2<Tuple6<T1, T2, T3, T4, T5, T6>, Tuple7<T7, T8, T9, T10, T11, T12, T13>> split6() {
        return new Tuple2<>(limit6(), skip6());
    }

    /**
     * Split this tuple into two tuples of degree 7 and 6.
     */
    public final Tuple2<Tuple7<T1, T2, T3, T4, T5, T6, T7>, Tuple6<T8, T9, T10, T11, T12, T13>> split7() {
        return new Tuple2<>(limit7(), skip7());
    }

    /**
     * Split this tuple into two tuples of degree 8 and 5.
     */
    public final Tuple2<Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>, Tuple5<T9, T10, T11, T12, T13>> split8() {
        return new Tuple2<>(limit8(), skip8());
    }

    /**
     * Split this tuple into two tuples of degree 9 and 4.
     */
    public final Tuple2<Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>, Tuple4<T10, T11, T12, T13>> split9() {
        return new Tuple2<>(limit9(), skip9());
    }

    /**
     * Split this tuple into two tuples of degree 10 and 3.
     */
    public final Tuple2<Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>, Tuple3<T11, T12, T13>> split10() {
        return new Tuple2<>(limit10(), skip10());
    }

    /**
     * Split this tuple into two tuples of degree 11 and 2.
     */
    public final Tuple2<Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>, Tuple2<T12, T13>> split11() {
        return new Tuple2<>(limit11(), skip11());
    }

    /**
     * Split this tuple into two tuples of degree 12 and 1.
     */
    public final Tuple2<Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>, Tuple1<T13>> split12() {
        return new Tuple2<>(limit12(), skip12());
    }

    /**
     * Split this tuple into two tuples of degree 13 and 0.
     */
    public final Tuple2<Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>, Tuple0> split13() {
        return new Tuple2<>(limit13(), skip13());
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
        return new Tuple4<>(v1, v2, v3, v4);
    }

    /**
     * Limit this tuple to degree 5.
     */
    public final Tuple5<T1, T2, T3, T4, T5> limit5() {
        return new Tuple5<>(v1, v2, v3, v4, v5);
    }

    /**
     * Limit this tuple to degree 6.
     */
    public final Tuple6<T1, T2, T3, T4, T5, T6> limit6() {
        return new Tuple6<>(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Limit this tuple to degree 7.
     */
    public final Tuple7<T1, T2, T3, T4, T5, T6, T7> limit7() {
        return new Tuple7<>(v1, v2, v3, v4, v5, v6, v7);
    }

    /**
     * Limit this tuple to degree 8.
     */
    public final Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> limit8() {
        return new Tuple8<>(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Limit this tuple to degree 9.
     */
    public final Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9> limit9() {
        return new Tuple9<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
    }

    /**
     * Limit this tuple to degree 10.
     */
    public final Tuple10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> limit10() {
        return new Tuple10<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
    }

    /**
     * Limit this tuple to degree 11.
     */
    public final Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> limit11() {
        return new Tuple11<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);
    }

    /**
     * Limit this tuple to degree 12.
     */
    public final Tuple12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> limit12() {
        return new Tuple12<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
    }

    /**
     * Limit this tuple to degree 13.
     */
    public final Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> limit13() {
        return this;
    }

    /**
     * Skip 0 degrees from this tuple.
     */
    public final Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> skip0() {
        return this;
    }

    /**
     * Skip 1 degrees from this tuple.
     */
    public final Tuple12<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> skip1() {
        return new Tuple12<>(v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Skip 2 degrees from this tuple.
     */
    public final Tuple11<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> skip2() {
        return new Tuple11<>(v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Skip 3 degrees from this tuple.
     */
    public final Tuple10<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> skip3() {
        return new Tuple10<>(v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Skip 4 degrees from this tuple.
     */
    public final Tuple9<T5, T6, T7, T8, T9, T10, T11, T12, T13> skip4() {
        return new Tuple9<>(v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Skip 5 degrees from this tuple.
     */
    public final Tuple8<T6, T7, T8, T9, T10, T11, T12, T13> skip5() {
        return new Tuple8<>(v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Skip 6 degrees from this tuple.
     */
    public final Tuple7<T7, T8, T9, T10, T11, T12, T13> skip6() {
        return new Tuple7<>(v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Skip 7 degrees from this tuple.
     */
    public final Tuple6<T8, T9, T10, T11, T12, T13> skip7() {
        return new Tuple6<>(v8, v9, v10, v11, v12, v13);
    }

    /**
     * Skip 8 degrees from this tuple.
     */
    public final Tuple5<T9, T10, T11, T12, T13> skip8() {
        return new Tuple5<>(v9, v10, v11, v12, v13);
    }

    /**
     * Skip 9 degrees from this tuple.
     */
    public final Tuple4<T10, T11, T12, T13> skip9() {
        return new Tuple4<>(v10, v11, v12, v13);
    }

    /**
     * Skip 10 degrees from this tuple.
     */
    public final Tuple3<T11, T12, T13> skip10() {
        return new Tuple3<>(v11, v12, v13);
    }

    /**
     * Skip 11 degrees from this tuple.
     */
    public final Tuple2<T12, T13> skip11() {
        return new Tuple2<>(v12, v13);
    }

    /**
     * Skip 12 degrees from this tuple.
     */
    public final Tuple1<T13> skip12() {
        return new Tuple1<>(v13);
    }

    /**
     * Skip 13 degrees from this tuple.
     */
    public final Tuple0 skip13() {
        return new Tuple0();
    }

    /**
     * Apply this tuple as arguments to a function.
     */
    public final <R> R map(Function13<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? super T10, ? super T11, ? super T12, ? super T13, ? extends R> function) {
        return function.apply(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 1 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U1> Tuple13<U1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> map1(Function1<? super T1, ? extends U1> function) {
        return new Tuple13<>(function.apply(v1), v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 2 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U2> Tuple13<T1, U2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> map2(Function1<? super T2, ? extends U2> function) {
        return new Tuple13<>(v1, function.apply(v2), v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 3 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U3> Tuple13<T1, T2, U3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> map3(Function1<? super T3, ? extends U3> function) {
        return new Tuple13<>(v1, v2, function.apply(v3), v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 4 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U4> Tuple13<T1, T2, T3, U4, T5, T6, T7, T8, T9, T10, T11, T12, T13> map4(Function1<? super T4, ? extends U4> function) {
        return new Tuple13<>(v1, v2, v3, function.apply(v4), v5, v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 5 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U5> Tuple13<T1, T2, T3, T4, U5, T6, T7, T8, T9, T10, T11, T12, T13> map5(Function1<? super T5, ? extends U5> function) {
        return new Tuple13<>(v1, v2, v3, v4, function.apply(v5), v6, v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 6 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U6> Tuple13<T1, T2, T3, T4, T5, U6, T7, T8, T9, T10, T11, T12, T13> map6(Function1<? super T6, ? extends U6> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, function.apply(v6), v7, v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 7 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U7> Tuple13<T1, T2, T3, T4, T5, T6, U7, T8, T9, T10, T11, T12, T13> map7(Function1<? super T7, ? extends U7> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, function.apply(v7), v8, v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 8 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U8> Tuple13<T1, T2, T3, T4, T5, T6, T7, U8, T9, T10, T11, T12, T13> map8(Function1<? super T8, ? extends U8> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, v7, function.apply(v8), v9, v10, v11, v12, v13);
    }

    /**
     * Apply attribute 9 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U9> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, U9, T10, T11, T12, T13> map9(Function1<? super T9, ? extends U9> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, v7, v8, function.apply(v9), v10, v11, v12, v13);
    }

    /**
     * Apply attribute 10 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U10> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, U10, T11, T12, T13> map10(Function1<? super T10, ? extends U10> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, function.apply(v10), v11, v12, v13);
    }

    /**
     * Apply attribute 11 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U11> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, U11, T12, T13> map11(Function1<? super T11, ? extends U11> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, function.apply(v11), v12, v13);
    }

    /**
     * Apply attribute 12 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U12> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, U12, T13> map12(Function1<? super T12, ? extends U12> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, function.apply(v12), v13);
    }

    /**
     * Apply attribute 13 as argument to a function and return a new tuple with the substituted argument.
     */
    public final <U13> Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, U13> map13(Function1<? super T13, ? extends U13> function) {
        return new Tuple13<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, function.apply(v13));
    }


    @Override
    public Tuple13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> clone() {
        return new Tuple13<>(this);
    }
}

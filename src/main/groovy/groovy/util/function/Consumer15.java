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
package groovy.util.function;

import groovy.lang.Tuple1;
import groovy.lang.Tuple10;
import groovy.lang.Tuple11;
import groovy.lang.Tuple12;
import groovy.lang.Tuple13;
import groovy.lang.Tuple14;
import groovy.lang.Tuple15;
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import groovy.lang.Tuple4;
import groovy.lang.Tuple5;
import groovy.lang.Tuple6;
import groovy.lang.Tuple7;
import groovy.lang.Tuple8;
import groovy.lang.Tuple9;


/**
 * A consumer with 15 arguments.
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface Consumer15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> {

    /**
     * Performs this operation on the given argument.
     *
     * @param args The arguments as a tuple.
     */
    default void accept(Tuple15<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12, ? extends T13, ? extends T14, ? extends T15> args) {
        accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), args.v10(), args.v11(), args.v12(), args.v13(), args.v14(), args.v15());
    }

    /**
     * Performs this operation on the given argument.
     */
    void accept(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14, T15 v15);

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1) {
        return (v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer13<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2) {
        return (v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer12<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3) {
        return (v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer11<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4) {
        return (v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer10<T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        return (v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer9<T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        return (v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer8<T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7) {
        return (v8, v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer7<T9, T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8) {
        return (v9, v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer6<T10, T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9) {
        return (v10, v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer5<T11, T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10) {
        return (v11, v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer4<T12, T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11) {
        return (v12, v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer3<T13, T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12) {
        return (v13, v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer2<T14, T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13) {
        return (v14, v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer1<T15> acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14) {
        return (v15) -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer0 acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14, T15 v15) {
        return () -> accept(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer14<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple1<? extends T1> args) {
        return (v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer13<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple2<? extends T1, ? extends T2> args) {
        return (v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer12<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple3<? extends T1, ? extends T2, ? extends T3> args) {
        return (v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer11<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        return (v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer10<T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple5<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5> args) {
        return (v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), v6, v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer9<T7, T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple6<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6> args) {
        return (v7, v8, v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), v7, v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer8<T8, T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple7<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7> args) {
        return (v8, v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), v8, v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer7<T9, T10, T11, T12, T13, T14, T15> acceptPartially(Tuple8<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8> args) {
        return (v9, v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), v9, v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer6<T10, T11, T12, T13, T14, T15> acceptPartially(Tuple9<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9> args) {
        return (v10, v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), v10, v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer5<T11, T12, T13, T14, T15> acceptPartially(Tuple10<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10> args) {
        return (v11, v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), args.v10(), v11, v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer4<T12, T13, T14, T15> acceptPartially(Tuple11<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11> args) {
        return (v12, v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), args.v10(), args.v11(), v12, v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer3<T13, T14, T15> acceptPartially(Tuple12<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12> args) {
        return (v13, v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), args.v10(), args.v11(), args.v12(), v13, v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer2<T14, T15> acceptPartially(Tuple13<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12, ? extends T13> args) {
        return (v14, v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), args.v10(), args.v11(), args.v12(), args.v13(), v14, v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer1<T15> acceptPartially(Tuple14<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12, ? extends T13, ? extends T14> args) {
        return (v15) -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), args.v10(), args.v11(), args.v12(), args.v13(), args.v14(), v15);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer0 acceptPartially(Tuple15<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12, ? extends T13, ? extends T14, ? extends T15> args) {
        return () -> accept(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6(), args.v7(), args.v8(), args.v9(), args.v10(), args.v11(), args.v12(), args.v13(), args.v14(), args.v15());
    }
}

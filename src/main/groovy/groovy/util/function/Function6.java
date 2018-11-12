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
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import groovy.lang.Tuple4;
import groovy.lang.Tuple5;
import groovy.lang.Tuple6;


/**
 * A function with 6 arguments.
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface Function6<T1, T2, T3, T4, T5, T6, R> {

    /**
     * Apply this function to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default R apply(Tuple6<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6> args) {
        return apply(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6());
    }

    /**
     * Apply this function to the arguments.
     */
    R apply(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6);

    /**
     * Partially apply this function to the arguments.
     */
    default Function5<T2, T3, T4, T5, T6, R> applyPartially(T1 v1) {
        return (v2, v3, v4, v5, v6) -> apply(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function4<T3, T4, T5, T6, R> applyPartially(T1 v1, T2 v2) {
        return (v3, v4, v5, v6) -> apply(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function3<T4, T5, T6, R> applyPartially(T1 v1, T2 v2, T3 v3) {
        return (v4, v5, v6) -> apply(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function2<T5, T6, R> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4) {
        return (v5, v6) -> apply(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function1<T6, R> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        return (v6) -> apply(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function0<R> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        return () -> apply(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function5<T2, T3, T4, T5, T6, R> applyPartially(Tuple1<? extends T1> args) {
        return (v2, v3, v4, v5, v6) -> apply(args.v1(), v2, v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function4<T3, T4, T5, T6, R> applyPartially(Tuple2<? extends T1, ? extends T2> args) {
        return (v3, v4, v5, v6) -> apply(args.v1(), args.v2(), v3, v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function3<T4, T5, T6, R> applyPartially(Tuple3<? extends T1, ? extends T2, ? extends T3> args) {
        return (v4, v5, v6) -> apply(args.v1(), args.v2(), args.v3(), v4, v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function2<T5, T6, R> applyPartially(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        return (v5, v6) -> apply(args.v1(), args.v2(), args.v3(), args.v4(), v5, v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function1<T6, R> applyPartially(Tuple5<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5> args) {
        return (v6) -> apply(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), v6);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function0<R> applyPartially(Tuple6<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6> args) {
        return () -> apply(args.v1(), args.v2(), args.v3(), args.v4(), args.v5(), args.v6());
    }

}

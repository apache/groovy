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

import java.util.function.BiFunction;

/**
 * A function with 2 arguments.
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface Function2<T1, T2, R> extends BiFunction<T1, T2, R> {

    /**
     * Apply this function to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default R apply(Tuple2<? extends T1, ? extends T2> args) {
        return apply(args.v1(), args.v2());
    }

    /**
     * Apply this function to the arguments.
     */
    @Override
    R apply(T1 v1, T2 v2);

    /**
     * Convert this function to a {@link BiFunction}.
     */
    default BiFunction<T1, T2, R> toBiFunction() {
        return this::apply;
    }

    /**
     * Convert to this function from a {@link BiFunction}.
     */
    static <T1, T2, R> Function2<T1, T2, R> from(BiFunction<? super T1, ? super T2, ? extends R> function) {
        return function::apply;
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function1<T2, R> applyPartially(T1 v1) {
        return (v2) -> apply(v1, v2);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function0<R> applyPartially(T1 v1, T2 v2) {
        return () -> apply(v1, v2);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function1<T2, R> applyPartially(Tuple1<? extends T1> args) {
        return (v2) -> apply(args.v1(), v2);
    }

    /**
     * Partially apply this function to the arguments.
     */
    default Function0<R> applyPartially(Tuple2<? extends T1, ? extends T2> args) {
        return () -> apply(args.v1(), args.v2());
    }

}

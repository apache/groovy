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

import java.util.function.BiConsumer;

/**
 * A consumer with 2 arguments.
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface Consumer2<T1, T2> extends BiConsumer<T1, T2> {

    /**
     * Performs this operation on the given argument.
     *
     * @param args The arguments as a tuple.
     */
    default void accept(Tuple2<? extends T1, ? extends T2> args) {
        accept(args.v1(), args.v2());
    }

    /**
     * Performs this operation on the given argument.
     */
    @Override
    void accept(T1 v1, T2 v2);

    /**
     * Convert this consumer to a {@link BiConsumer}.
     */
    default BiConsumer<T1, T2> toBiConsumer() {
        return this::accept;
    }

    /**
     * Convert to this consumer to a {@link BiConsumer}.
     */
    static <T1, T2> Consumer2<T1, T2> from(BiConsumer<? super T1, ? super T2> consumer) {
        return consumer::accept;
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer1<T2> acceptPartially(T1 v1) {
        return (v2) -> accept(v1, v2);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer0 acceptPartially(T1 v1, T2 v2) {
        return () -> accept(v1, v2);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer1<T2> acceptPartially(Tuple1<? extends T1> args) {
        return (v2) -> accept(args.v1(), v2);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer0 acceptPartially(Tuple2<? extends T1, ? extends T2> args) {
        return () -> accept(args.v1(), args.v2());
    }
}

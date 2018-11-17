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


/**
 * A consumer with 4 arguments.
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface Consumer4<T1, T2, T3, T4> {

    /**
     * Performs this operation on the given argument.
     *
     * @param args The arguments as a tuple.
     */
    default void accept(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        accept(args.getV1(), args.getV2(), args.getV3(), args.getV4());
    }

    /**
     * Performs this operation on the given argument.
     */
    void accept(T1 v1, T2 v2, T3 v3, T4 v4);

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer3<T2, T3, T4> acceptPartially(T1 v1) {
        return (v2, v3, v4) -> accept(v1, v2, v3, v4);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer2<T3, T4> acceptPartially(T1 v1, T2 v2) {
        return (v3, v4) -> accept(v1, v2, v3, v4);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer1<T4> acceptPartially(T1 v1, T2 v2, T3 v3) {
        return (v4) -> accept(v1, v2, v3, v4);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer0 acceptPartially(T1 v1, T2 v2, T3 v3, T4 v4) {
        return () -> accept(v1, v2, v3, v4);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer3<T2, T3, T4> acceptPartially(Tuple1<? extends T1> args) {
        return (v2, v3, v4) -> accept(args.getV1(), v2, v3, v4);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer2<T3, T4> acceptPartially(Tuple2<? extends T1, ? extends T2> args) {
        return (v3, v4) -> accept(args.getV1(), args.getV2(), v3, v4);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer1<T4> acceptPartially(Tuple3<? extends T1, ? extends T2, ? extends T3> args) {
        return (v4) -> accept(args.getV1(), args.getV2(), args.getV3(), v4);
    }

    /**
     * Let this consumer partially accept the arguments.
     */
    default Consumer0 acceptPartially(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        return () -> accept(args.getV1(), args.getV2(), args.getV3(), args.getV4());
    }
}

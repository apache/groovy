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

import groovy.lang.Tuple0;


/**
 * A consumer with 0 arguments.
 *
 * @since 3.0.0
 */
@FunctionalInterface
public interface Consumer0 extends Runnable {

    /**
     * Performs this operation on the given argument.
     *
     * @param args The arguments as a tuple.
     */
    default void accept(Tuple0 args) {
        accept();
    }

    /**
     * Performs this operation on the given argument.
     */
    void accept();

    @Override
    default void run() {
        accept();
    }

    /**
     * Convert this consumer to a {@link Runnable}.
     */
    default Runnable toRunnable() {
        return this::accept;
    }

    /**
     * Convert to this consumer from a {@link Runnable}.
     */
    static Consumer0 from(Runnable runnable) {
        return runnable::run;
    }
}

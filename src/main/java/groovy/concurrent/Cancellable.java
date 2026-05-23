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
package groovy.concurrent;

/**
 * A handle for a scheduled action that can be called off before it
 * fires (or that can have its further firings suppressed, in the case
 * of a recurring schedule).
 * <p>
 * Returned by {@link ActorContext#scheduleOnce(Object, java.time.Duration)}
 * and {@link ActorContext#scheduleAtFixedRate(Object, java.time.Duration,
 * java.time.Duration)}.
 *
 * @since 6.0.0
 */
public interface Cancellable {

    /**
     * Attempts to cancel the scheduled action. Returns {@code true} if
     * cancellation prevented at least one further firing, {@code false}
     * if the action had already fired (one-shot), had already been
     * cancelled, or could not be cancelled.
     * <p>
     * Idempotent: calling {@code cancel()} repeatedly is safe.
     */
    boolean cancel();

    /**
     * Returns {@code true} if this scheduled action has been cancelled.
     */
    boolean isCancelled();
}

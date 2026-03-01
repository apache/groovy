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

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the outcome of an asynchronous computation that may have
 * succeeded or failed. This is used by {@code awaitAllSettled()} —
 * the Groovy equivalent of JavaScript's {@code Promise.allSettled()}.
 * <p>
 * An {@code AwaitResult} is either a {@linkplain #isSuccess() success}
 * carrying a value, or a {@linkplain #isFailure() failure} carrying a
 * {@link Throwable}.
 *
 * @param <T> the value type
 * @since 6.0.0
 */
public final class AwaitResult<T> {

    private final T value;
    private final Throwable error;
    private final boolean success;

    private AwaitResult(T value, Throwable error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    /**
     * Creates a successful result with the given value.
     */
    @SuppressWarnings("unchecked")
    public static <T> AwaitResult<T> success(Object value) {
        return new AwaitResult<>((T) value, null, true);
    }

    /**
     * Creates a failure result with the given exception.
     */
    public static <T> AwaitResult<T> failure(Throwable error) {
        return new AwaitResult<>(null, Objects.requireNonNull(error), false);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    /**
     * Returns the value if successful; throws {@link IllegalStateException} if failed.
     */
    public T getValue() {
        if (!success) throw new IllegalStateException("Cannot get value from a failed result");
        return value;
    }

    /**
     * Returns the exception if failed; throws {@link IllegalStateException} if successful.
     */
    public Throwable getError() {
        if (success) throw new IllegalStateException("Cannot get error from a successful result");
        return error;
    }

    /**
     * Returns the value if successful, or applies the given function to
     * the error to produce a fallback value.
     */
    public T getOrElse(Function<Throwable, ? extends T> fallback) {
        return success ? value : fallback.apply(error);
    }

    @Override
    public String toString() {
        return success
            ? "AwaitResult.Success[" + value + "]"
            : "AwaitResult.Failure[" + error + "]";
    }
}

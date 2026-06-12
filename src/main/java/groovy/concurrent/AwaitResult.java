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
 * succeeded or failed.  Returned by
 * {@link Awaitable#allSettled(Object...) Awaitable.allSettled()} to
 * report each individual task's result without short-circuiting on
 * failure.
 * <p>
 * An {@code AwaitResult} is either a {@linkplain #isSuccess() success}
 * carrying a value, or a {@linkplain #isFailure() failure} carrying a
 * {@link Throwable}.
 * <p>
 * This type follows the <em>value object</em> pattern: two instances
 * are {@linkplain #equals(Object) equal} if and only if they have
 * the same success/failure state and carry equal values or errors.
 * Immutability is enforced — all fields are final and no mutating
 * methods are exposed.
 * <p>
 * Functional composition is supported via {@link #map(Function)},
 * enabling transformation chains without unwrapping:
 * <pre>
 * AwaitResult&lt;Integer&gt; length = AwaitResult.success("hello").map(String::length)
 * </pre>
 * <p>
 * Inspired by Kotlin's {@code Result}, Rust's {@code Result<T, E>},
 * and C#'s pattern of structured success/error responses.
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
     *
     * @param value the computation result (may be {@code null})
     * @param <T>   the value type
     * @return a success result wrapping the value
     */
    @SuppressWarnings("unchecked")
    public static <T> AwaitResult<T> success(Object value) {
        return new AwaitResult<>((T) value, null, true);
    }

    /**
     * Creates a failure result with the given exception.
     *
     * @param error the exception that caused the failure; must not be {@code null}
     * @param <T>   the value type (never actually used, since the result is a failure)
     * @return a failure result wrapping the exception
     * @throws NullPointerException if {@code error} is {@code null}
     */
    public static <T> AwaitResult<T> failure(Throwable error) {
        return new AwaitResult<>(null, Objects.requireNonNull(error), false);
    }

    /** Returns {@code true} if this result represents a successful completion. */
    public boolean isSuccess() {
        return success;
    }

    /** Returns {@code true} if this result represents a failed completion. */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Returns the value if successful.
     *
     * @return the computation result
     * @throws IllegalStateException if this result represents a failure
     */
    public T getValue() {
        if (!success) throw new IllegalStateException("Cannot get value from a failed result");
        return value;
    }

    /**
     * Returns the exception if failed.
     *
     * @return the exception that caused the failure
     * @throws IllegalStateException if this result represents a success
     */
    public Throwable getError() {
        if (success) throw new IllegalStateException("Cannot get error from a successful result");
        return error;
    }

    /**
     * Returns the value if successful, or applies the given function to
     * the error to produce a fallback value.
     *
     * @param fallback the function to apply to the error if this result
     *                 is a failure; must not be {@code null}
     * @return the value, or the fallback function's result
     */
    public T getOrElse(Function<Throwable, ? extends T> fallback) {
        return success ? value : fallback.apply(error);
    }

    /**
     * Transforms a successful result's value using the given function.
     * If this result is a failure, the error is propagated unchanged.
     * <p>
     * This is the functor {@code map} operation, enabling value
     * transformation without explicit unwrapping:
     * <pre>
     * AwaitResult&lt;String&gt;  name   = AwaitResult.success("Groovy")
     * AwaitResult&lt;Integer&gt; length = name.map(String::length)
     * assert length.value == 6
     * </pre>
     *
     * @param fn  the mapping function; must not be {@code null}
     * @param <U> the type of the mapped value
     * @return a new success result with the mapped value, or the
     *         original failure unchanged
     * @throws NullPointerException if {@code fn} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public <U> AwaitResult<U> map(Function<? super T, ? extends U> fn) {
        Objects.requireNonNull(fn, "mapping function must not be null");
        if (!success) {
            return (AwaitResult<U>) this;
        }
        return AwaitResult.success(fn.apply(value));
    }

    /**
     * Compares this result to another object for equality.
     * <p>
     * Two {@code AwaitResult} instances are equal if and only if they
     * have the same success/failure state and carry
     * {@linkplain Objects#equals(Object, Object) equal} values or errors.
     *
     * @param o the object to compare with
     * @return {@code true} if the given object is an equal {@code AwaitResult}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AwaitResult<?> that)) return false;
        if (success != that.success) return false;
        return success
                ? Objects.equals(value, that.value)
                : Objects.equals(error, that.error);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return success
                ? Objects.hash(true, value)
                : Objects.hash(false, error);
    }

    /**
     * Returns a human-readable representation of this result:
     * {@code AwaitResult.Success[value]} or {@code AwaitResult.Failure[error]}.
     */
    @Override
    public String toString() {
        return success
            ? "AwaitResult.Success[" + value + "]"
            : "AwaitResult.Failure[" + error + "]";
    }
}

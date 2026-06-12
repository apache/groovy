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
package org.apache.groovy.util;

import groovy.lang.Closure;

import java.io.Serial;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Helpers that return hybrid one-argument functions which are both
 * {@link Closure} and the appropriate {@link java.util.function} SAM
 * ({@link Predicate}, {@link Function} or {@link Consumer}).
 * <p>
 * For variants whose results are only the SAM type, see {@link Lambdas}.
 *
 * @since 6.0.0
 */
public class Closures {

    private Closures() {}

    /**
     * Lifts a {@link Predicate} into a hybrid that is also a
     * {@link Closure}. If {@code p} is already a {@link PredicateClosure}
     * it is returned unchanged.
     * <pre class="language-groovy">
     * Predicate&lt;Integer&gt; isEven = n -&gt; n % 2 == 0
     * assert [1, 2, 3, 4].findAll(Closures.from(isEven)) == [2, 4]
     * </pre>
     *
     * @since 6.0.0
     */
    public static <T> PredicateClosure<T> from(Predicate<T> p) {
        if (p instanceof @SuppressWarnings("unchecked") PredicateClosure<T> pc) {
            return pc;
        }
        return new PredicateClosure<>(p);
    }

    /**
     * Lifts a {@link Function} into a hybrid that is also a
     * {@link Closure}. If {@code f} is already a {@link FunctionClosure}
     * it is returned unchanged.
     *
     * @since 6.0.0
     */
    public static <T, R> FunctionClosure<T, R> from(Function<T, R> f) {
        if (f instanceof @SuppressWarnings("unchecked") FunctionClosure<T, R> fc) {
            return fc;
        }
        return new FunctionClosure<>(f);
    }

    /**
     * Lifts a {@link Consumer} into a hybrid that is also a
     * {@link Closure}. If {@code c} is already a {@link ConsumerClosure}
     * it is returned unchanged.
     *
     * @since 6.0.0
     */
    public static <T> ConsumerClosure<T> from(Consumer<T> c) {
        if (c instanceof @SuppressWarnings("unchecked") ConsumerClosure<T> cc) {
            return cc;
        }
        return new ConsumerClosure<>(c);
    }

    /**
     * Right-partials a {@link BiPredicate} and lifts the result into a
     * hybrid usable as both {@link Closure} and {@link Predicate}.
     * Equivalent to {@code from(Lambdas.curryWith(bp, p))}.
     * <pre class="language-groovy">
     * BiPredicate&lt;Integer,Integer&gt; divisibleBy = (n, d) -&gt; n % d == 0
     * assert [1, 2, 3, 4, 5].findAll(curryWith(divisibleBy, 2)) == [2, 4]
     * </pre>
     *
     * @since 6.0.0
     */
    public static <T, P> PredicateClosure<T> curryWith(BiPredicate<? super T, ? super P> bp, P p) {
        return from(Lambdas.curryWith(bp, p));
    }

    /**
     * Right-partials a {@link BiFunction} and lifts the result into a
     * hybrid usable as both {@link Closure} and {@link Function}.
     * Equivalent to {@code from(Lambdas.curryWith(bf, p))}.
     *
     * @since 6.0.0
     */
    public static <T, P, R> FunctionClosure<T, R> curryWith(BiFunction<? super T, ? super P, ? extends R> bf, P p) {
        return from(Lambdas.curryWith(bf, p));
    }

    /**
     * Right-partials a {@link BiConsumer} and lifts the result into a
     * hybrid usable as both {@link Closure} and {@link Consumer}.
     * Equivalent to {@code from(Lambdas.curryWith(bc, p))}.
     *
     * @since 6.0.0
     */
    public static <T, P> ConsumerClosure<T> curryWith(BiConsumer<? super T, ? super P> bc, P p) {
        return from(Lambdas.curryWith(bc, p));
    }

    /**
     * Hybrid one-argument function that is both a {@link Closure} and a
     * {@link Predicate}.
     *
     * @since 6.0.0
     */
    public static class PredicateClosure<T> extends Closure<Boolean> implements Predicate<T> {
        @Serial private static final long serialVersionUID = 1L;
        private final Predicate<T> delegate;

        PredicateClosure(Predicate<T> delegate) {
            super(null, null);
            this.delegate = delegate;
            this.maximumNumberOfParameters = 1;
            this.parameterTypes = new Class<?>[]{Object.class};
        }

        @Override
        public boolean test(T t) {
            return delegate.test(t);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Boolean call(Object arg) {
            return test((T) arg);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Boolean call(Object... args) {
            return test((T) args[0]);
        }
    }

    /**
     * Hybrid one-argument function that is both a {@link Closure} and a
     * {@link Function}.
     *
     * @since 6.0.0
     */
    public static class FunctionClosure<T, R> extends Closure<R> implements Function<T, R> {
        @Serial private static final long serialVersionUID = 1L;
        private final Function<T, R> delegate;

        FunctionClosure(Function<T, R> delegate) {
            super(null, null);
            this.delegate = delegate;
            this.maximumNumberOfParameters = 1;
            this.parameterTypes = new Class<?>[]{Object.class};
        }

        @Override
        public R apply(T t) {
            return delegate.apply(t);
        }

        @Override
        @SuppressWarnings("unchecked")
        public R call(Object arg) {
            return apply((T) arg);
        }

        @Override
        @SuppressWarnings("unchecked")
        public R call(Object... args) {
            return apply((T) args[0]);
        }
    }

    /**
     * Hybrid one-argument function that is both a {@link Closure} and a
     * {@link Consumer}.
     *
     * @since 6.0.0
     */
    public static class ConsumerClosure<T> extends Closure<Void> implements Consumer<T> {
        @Serial private static final long serialVersionUID = 1L;
        private final Consumer<T> delegate;

        ConsumerClosure(Consumer<T> delegate) {
            super(null, null);
            this.delegate = delegate;
            this.maximumNumberOfParameters = 1;
            this.parameterTypes = new Class<?>[]{Object.class};
        }

        @Override
        public void accept(T t) {
            delegate.accept(t);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Void call(Object arg) {
            accept((T) arg);
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Void call(Object... args) {
            accept((T) args[0]);
            return null;
        }
    }
}

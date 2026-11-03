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
package org.apache.groovy.runtime.async;

import groovy.concurrent.Awaitable;
import groovy.lang.Closure;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;

/**
 * Closure-specific async utilities that have no pure-Java equivalent.
 * <p>
 * These methods create {@link Closure} subclasses and are therefore
 * inherently Groovy-specific. They predate the {@code async} keyword
 * and are retained for programmatic use. The {@code async { ... }}
 * keyword syntax is preferred for most use cases.
 *
 * @see AsyncSupport
 * @since 6.0.0
 */
public final class AsyncClosureUtils {

    private AsyncClosureUtils() { }

    /**
     * Wraps a closure so that each invocation executes the body
     * asynchronously and returns an {@link Awaitable}. The returned closure
     * reads and sets the delegate and resolve strategy of the wrapped one.
     *
     * <pre>{@code
     * def asyncTask = AsyncClosureUtils.wrapAsync { expensiveWork() }
     * def result = await asyncTask()
     * }</pre>
     *
     * @param closure the closure to wrap
     * @param <T>     the result type
     * @return a closure that returns an Awaitable on each call
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<Awaitable<T>> wrapAsync(Closure<T> closure) {
        Objects.requireNonNull(closure, "closure must not be null");
        return new AsyncClosure<>(closure);
    }

    /**
     * Wraps a generator closure so that each invocation returns an
     * {@link Iterable} backed by a {@link GeneratorBridge}. The returned closure
     * reads and sets the delegate and resolve strategy of the wrapped one.
     *
     * @param closure the generator closure; receives a GeneratorBridge
     *                as its first parameter
     * @param <T>     the element type
     * @return a closure that produces an Iterable on each call
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<Iterable<T>> wrapAsyncGenerator(Closure<?> closure) {
        Objects.requireNonNull(closure, "closure must not be null");
        return new AsyncGeneratorClosure<>(closure);
    }

    /**
     * A closure which calls the closure it wraps. The wrapped closure resolves the names its body
     * uses, so the delegate and resolve strategy are read from and written to it, and copies of
     * this closure wrap copies of it.
     */
    private abstract static class WrappingClosure<V> extends Closure<V> {
        @Serial
        private static final long serialVersionUID = 4507262338211567417L;
        Closure<?> closure;

        WrappingClosure(final Closure<?> closure) {
            super(closure.getOwner(), closure.getThisObject());
            this.closure = closure;
        }

        @Override
        public Object getDelegate() {
            return closure.getDelegate();
        }

        @Override
        public void setDelegate(final Object delegate) {
            closure.setDelegate(delegate);
        }

        @Override
        public int getResolveStrategy() {
            return closure.getResolveStrategy();
        }

        @Override
        public void setResolveStrategy(final int resolveStrategy) {
            closure.setResolveStrategy(resolveStrategy);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object clone() {
            WrappingClosure<V> result = (WrappingClosure<V>) super.clone();
            result.closure = (Closure<?>) closure.clone();
            return result;
        }

        @Override
        public Closure<V> dehydrate() {
            WrappingClosure<V> result = (WrappingClosure<V>) super.dehydrate();
            result.closure = closure.dehydrate();
            return result;
        }

        @Override
        public Closure<V> rehydrate(final Object delegate, final Object owner, final Object thisObject) {
            WrappingClosure<V> result = (WrappingClosure<V>) super.rehydrate(delegate, owner, thisObject);
            result.closure = closure.rehydrate(delegate, owner, thisObject);
            return result;
        }

        /**
         * The wrapped closure is called and forwarded to, but is held outside the
         * {@code owner}/{@code delegate}/{@code thisObject} references the cycle check walks by default.
         */
        @Override
        protected Object[] additionalReferences() {
            return new Object[]{closure};
        }

        /**
         * Rejects a deserialized closure whose references form a cycle, which would otherwise recurse
         * indefinitely on invocation. See {@link Closure#checkForReferenceCycle}. Not private, so that
         * serialization finds it for the subclasses.
         */
        @Serial
        Object readResolve() throws ObjectStreamException {
            Closure.checkForReferenceCycle(this);
            return this;
        }
    }

    private static final class AsyncClosure<T> extends WrappingClosure<Awaitable<T>> {
        @Serial
        private static final long serialVersionUID = -3326208329880254370L;

        AsyncClosure(final Closure<T> closure) {
            super(closure);
            parameterTypes = closure.getParameterTypes();
            maximumNumberOfParameters = closure.getMaximumNumberOfParameters();
        }

        @SuppressWarnings({"unchecked", "unused"})
        public Awaitable<T> doCall(Object... args) {
            return AsyncSupport.async(() -> (T) closure.call(args));
        }
    }

    /**
     * Reports the parameters of the wrapped closure after the leading {@link GeneratorBridge},
     * which this closure supplies itself.
     */
    private static final class AsyncGeneratorClosure<T> extends WrappingClosure<Iterable<T>> {
        @Serial
        private static final long serialVersionUID = 2105939587003781539L;

        AsyncGeneratorClosure(final Closure<?> closure) {
            super(closure);
            Class<?>[] types = closure.getParameterTypes();
            if (types.length > 0) {
                parameterTypes = Arrays.copyOfRange(types, 1, types.length);
                maximumNumberOfParameters = parameterTypes.length;
            }
        }

        @SuppressWarnings("unused")
        public Iterable<T> doCall(Object... args) {
            return AsyncSupport.asyncGenerator(bridge -> closure.call(prependArgument(bridge, args)));
        }
    }

    private static Object[] prependArgument(Object argument, Object[] args) {
        Object[] allArgs = new Object[args.length + 1];
        allArgs[0] = argument;
        System.arraycopy(args, 0, allArgs, 1, args.length);
        return allArgs;
    }
}

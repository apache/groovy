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
package groovy.lang;

/**
 * A TrampolineClosure wraps a closure that needs to be executed on a functional trampoline.
 * Upon calling, a TrampolineClosure will call the original closure waiting for its result.
 * If the outcome of the call is another instance of a TrampolineClosure, created perhaps as a result to a call to the TrampolineClosure.trampoline()
 * method, the TrampolineClosure will again be invoked. This repetitive invocation of returned TrampolineClosure instances will continue
 * until a value other than TrampolineClosure is returned.
 * That value will become the final result of the trampoline.
 */
final class TrampolineClosure<V> extends Closure<V> {

    private static final long serialVersionUID = -4096349147398489925L;
    private final Closure<V> original;

    TrampolineClosure(final Closure<V> original) {
        super(original.getOwner(), original.getDelegate());
        this.original = original;
    }

    /**
     * Delegates to the wrapped closure
     */
    @Override
    public int getMaximumNumberOfParameters() {
        return original.maximumNumberOfParameters;
    }

    /**
     * Delegates to the wrapped closure
     */
    @Override
    public Class[] getParameterTypes() {
        return original.parameterTypes;
    }

    /**
     * Starts the trampoline loop and calls the wrapped closure as the first step.
     * @return The final result of the trampoline
     */
    @Override
    public V call() {
        return loop(original.call());
    }

    /**
     * Starts the trampoline loop and calls the wrapped closure as the first step.
     * @return The final result of the trampoline
     */
    @Override
    public V call(final Object arguments) {
        return loop(original.call(arguments));
    }

    /**
     * Starts the trampoline loop and calls the wrapped closure as the first step.
     * @return The final result of the trampoline
     */
    @Override
    public V call(final Object... args) {
        return loop(original.call(args));
    }

    private V loop(final Object lastResult) {
        Object result = lastResult;

        for (;;) {
            if (result instanceof TrampolineClosure) {
                result = ((TrampolineClosure)result).original.call();
            } else return (V) result;
        }
    }

    /**
     * Builds a trampolined variant of the current closure.
     * @param args Parameters to curry to the underlying closure.
     * @return An instance of TrampolineClosure wrapping the original closure after currying.
     */
    @Override
   public Closure<V> trampoline(final Object... args) {
        return new TrampolineClosure<V>(original.curry(args));
    }

    /**
     * Returns itself, since it is a good enough trampolined variant of the current closure.
     * @return An instance of TrampolineClosure wrapping the original closure.
     */
    @Override
    public Closure<V> trampoline() {
        return this;
    }
}

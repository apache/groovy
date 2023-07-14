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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;

import static org.codehaus.groovy.runtime.ArrayGroovyMethods.last;

/**
 * A wrapper for Closure to support currying. Normally used only internally
 * through the <code>curry()</code>, <code>rcurry()</code> or
 * <code>ncurry()</code> methods on <code>Closure</code>.
 * <p>
 * Typical usages:
 * <pre class="groovyTestCase">
 * // normal usage
 * def unitAdder = { first, second, unit {@code ->} "${first + second} $unit" }
 * assert unitAdder(10, 15, "minutes") == "25 minutes"
 * assert unitAdder.curry(60)(15, "minutes") == "75 minutes"
 * def minuteAdder = unitAdder.rcurry("minutes")
 * assert minuteAdder(15, 60) == "75 minutes"
 *
 * // explicit creation
 * import org.codehaus.groovy.runtime.CurriedClosure
 * assert new CurriedClosure(unitAdder, 45)(15, "minutes") == "60 minutes"
 * assert new CurriedClosure(unitAdder, "six", "ty")("minutes") == "sixty minutes"
 * </pre>
 *
 * Notes:
 * <ul>
 *     <li>Caters for Groovy's lazy (rcurry) and eager (ncurry) calculation of argument position</li>
 * </ul>
 */
public final class CurriedClosure<V> extends Closure<V> {

    private static final long serialVersionUID = 2077643745780234126L;
    private final Object[] curriedArguments;
    private final int minParamsExpected;
    private int index;
    /** the last parameter type, if it's an array */
    private Class<?> varargType;

    /**
     * @param index the position where the parameters should be injected (-ve for lazy)
     * @param uncurriedClosure the closure to be called after the curried parameters are injected
     * @param arguments the supplied parameters
     */
    public CurriedClosure(final int index, final Closure<V> uncurriedClosure, final Object... arguments) {
        super(uncurriedClosure.clone());

        this.index = index;
        this.curriedArguments = arguments;
        int maxLen = uncurriedClosure.getMaximumNumberOfParameters();
        this.maximumNumberOfParameters = (maxLen - arguments.length);

        Class<?>[] parameterTypes = uncurriedClosure.getParameterTypes();
        if (parameterTypes.length > 0 && last(parameterTypes).isArray()){
            this.varargType = last(parameterTypes);
        }

        if (isVararg()) {
            this.minParamsExpected = 0;
        } else {
            // perform some early param checking for non-vararg case
            if (index < 0) {
                // normalise
                this.index += maxLen;
                this.minParamsExpected = 0;
            } else {
                this.minParamsExpected = index + arguments.length;
            }

            if (this.maximumNumberOfParameters < 0) {
                throw new IllegalArgumentException("Can't curry " + arguments.length + " arguments for a closure with " + maxLen + " parameters.");
            }
            if (index < 0) {
                int lower = -maxLen;
                int upper = -arguments.length;
                if (index < lower || index > upper)
                    throw new IllegalArgumentException("To curry " + arguments.length + " argument(s) expect index range " + lower + ".." + upper + " but found " + index);
            } else if (index > this.maximumNumberOfParameters) {
                throw new IllegalArgumentException("To curry " + arguments.length + " argument(s) expect index range 0.." + this.maximumNumberOfParameters + " but found " + index);
            }
        }
    }

    public CurriedClosure(final Closure<V> uncurriedClosure, final Object... arguments) {
        this(0, uncurriedClosure, arguments);
    }

    //--------------------------------------------------------------------------

    public Object[] getUncurriedArguments(final Object... arguments) {
        if (isVararg()) {
            int normalizedIndex = index < 0 ? index + arguments.length + curriedArguments.length : index;
            if (normalizedIndex < 0 || normalizedIndex > arguments.length) {
                throw new IllegalArgumentException("When currying expected index range between " +
                        (-arguments.length - curriedArguments.length) + ".." + (arguments.length + curriedArguments.length) + " but found " + index);
            }
            return getArguments(normalizedIndex, arguments);
        }
        if (curriedArguments.length + arguments.length < minParamsExpected) {
            throw new IllegalArgumentException("When currying expected at least " + index + " argument(s) to be supplied before known curried arguments but found " + arguments.length);
        }
        int newIndex = Math.min(index, curriedArguments.length + arguments.length - 1);
        // rcurried arguments are done lazily to allow normal method selection between overloaded alternatives
        newIndex = Math.min(newIndex, arguments.length);
        return getArguments(newIndex, arguments);
    }

    private Object[] getArguments(final int index, final Object[] arguments) {
        Object[] newArguments = new Object[curriedArguments.length + arguments.length];
        System.arraycopy(arguments, 0, newArguments, 0, index);
        System.arraycopy(curriedArguments, 0, newArguments, index, curriedArguments.length);
        if (arguments.length - index > 0)
            System.arraycopy(arguments, index, newArguments, curriedArguments.length + index, arguments.length - index);
        return newArguments;
    }

    @Override
    public void setDelegate(final Object delegate) {
        getOwner().setDelegate(delegate);
    }

    @Override
    public Object getDelegate() {
        return getOwner().getDelegate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Closure<V> getOwner() {
        return (Closure<V>) super.getOwner();
    }

    @Override
    public void setResolveStrategy(final int resolveStrategy) {
        getOwner().setResolveStrategy(resolveStrategy);
    }

    @Override
    public int getResolveStrategy() {
        return getOwner().getResolveStrategy();
    }

    @Override
    public Object clone() {
        @SuppressWarnings("unchecked")
        Closure<V> uncurriedClosure = (Closure<V>) getOwner().clone();
        return new CurriedClosure<V>(index, uncurriedClosure, curriedArguments);
    }

    @Override
    public Class[] getParameterTypes() {
        Class[] oldParams = getOwner().getParameterTypes();
        int extraParams = 0;
        int gobbledParams = curriedArguments.length;
        if (isVararg()) {
            int numNonVarargs = oldParams.length - 1;
            if (index < 0) {
                int absIndex = -index;
                // do -ve indexes based on actual args, so can't accurately calculate type here
                // so work out minimal type params and vararg on end will allow for other possibilities
                if (absIndex > numNonVarargs) gobbledParams = numNonVarargs;
                int newNumNonVarargs = numNonVarargs - gobbledParams;
                if (absIndex - curriedArguments.length > newNumNonVarargs) extraParams = absIndex - curriedArguments.length - newNumNonVarargs;
                int keptParams = Math.max(numNonVarargs - absIndex, 0);
                Class[] newParams = new Class[keptParams + newNumNonVarargs + extraParams + 1];
                System.arraycopy(oldParams, 0, newParams, 0, keptParams);
                for (int i = 0; i < newNumNonVarargs; i++) newParams[keptParams + i] = Object.class;
                for (int i = 0; i < extraParams; i++) newParams[keptParams + newNumNonVarargs + i] = varargType.getComponentType();
                newParams[newParams.length - 1] = varargType;
                return newParams;
            }
            int leadingKept = Math.min(index, numNonVarargs);
            int trailingKept = Math.max(numNonVarargs - leadingKept - curriedArguments.length, 0);
            if (index > leadingKept) extraParams = index - leadingKept;
            Class[] newParams = new Class[leadingKept + trailingKept + extraParams + 1];
            System.arraycopy(oldParams, 0, newParams, 0, leadingKept);
            if (trailingKept > 0) System.arraycopy(oldParams, leadingKept + curriedArguments.length, newParams, leadingKept, trailingKept);
            for (int i = 0; i < extraParams; i++) newParams[leadingKept + trailingKept + i] = varargType.getComponentType();
            newParams[newParams.length - 1] = varargType;
            return newParams;
        }
        Class[] newParams = new Class[oldParams.length - gobbledParams + extraParams];
        System.arraycopy(oldParams, 0, newParams, 0, index);
        if (newParams.length - index > 0)
            System.arraycopy(oldParams, curriedArguments.length + index, newParams, index, newParams.length - index);
        return newParams;
    }

    private boolean isVararg() {
        return varargType != null;
    }
}

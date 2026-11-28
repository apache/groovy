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
import groovy.transform.Internal;
import org.apache.groovy.lang.annotation.GroovyABI;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.runtime.ArrayGroovyMethods.last;

/**
 * A wrapper for Closure to support currying. Normally used only internally
 * through the <code>curry()</code>, <code>rcurry()</code> or
 * <code>ncurry()</code> methods on <code>Closure</code>.
 * <p>
 * Typical usages:
 * <pre class="language-groovy groovyTestCase">
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

    @Serial private static final long serialVersionUID = 2077643745780234126L;
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

    /**
     * Curries {@code arguments} at index {@code 0} of {@code uncurriedClosure}.
     * <p>A non-vararg closure given more arguments than it can accept is rejected.
     *
     * @param uncurriedClosure the closure the curried arguments are injected into
     * @param arguments values placed at the front of each later call
     */
    @GroovyABI(since = "1.0.0")
    public CurriedClosure(final Closure<V> uncurriedClosure, final Object... arguments) {
        this(0, uncurriedClosure, arguments);
    }

    //--------------------------------------------------------------------------

    /**
     * Fast path for the dominant rcurry/curry shape — one curried argument and a
     * single incoming argument against a binary owner. Skips the wrapping
     * {@code Object[1]} that {@link Closure#call(Object)} would otherwise allocate
     * before dispatch, leaving just the one combined {@code Object[2]} that the
     * underlying call already requires.
     */
    @Override
    @SuppressWarnings("unchecked")
    public V call(final Object argument) {
        if (varargType == null
                && curriedArguments.length == 1
                && maximumNumberOfParameters == 1
                && (index == 0 || index == 1)) {
            Object[] combined = index == 0
                    ? new Object[]{curriedArguments[0], argument}
                    : new Object[]{argument, curriedArguments[0]};
            return (V) getOwner().call(combined);
        }
        return super.call(argument);
    }

    /**
     * Inserts the curried arguments into {@code arguments} at the curry index.
     * A vararg closure normalizes a negative index against the combined length and
     * rejects an index outside {@code 0..arguments.length}. A non-vararg closure
     * rejects a call that supplies fewer arguments than the curry index requires,
     * then inserts at {@code min(index, arguments.length)}.
     *
     * @param arguments arguments supplied by the call, which may be empty
     * @return a new array with the curried values inserted
     */
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
    public Object getThisObject() {
        return getOwner().getThisObject();
    }

    @Override
    public Object clone() {
        @SuppressWarnings("unchecked")
        Closure<V> uncurriedClosure = (Closure<V>) getOwner().clone();
        return new CurriedClosure<V>(recreationIndex(), uncurriedClosure, curriedArguments);
    }

    /**
     * Index to pass back to the constructor. An {@code rcurry} closure stores the
     * normalised position, and constructing again from that position would treat it
     * as an eager {@code ncurry}.
     */
    private int recreationIndex() {
        // rcurry normalises a negative index onto the longest parameter list and
        // records no minimum. ncurry at that same slot still requires the leading arguments.
        boolean lazy = !isVararg()
                && minParamsExpected == 0
                && curriedArguments.length > 0
                && index == maximumNumberOfParameters;
        return lazy ? -curriedArguments.length : index;
    }

    /**
     * Dehydrates the wrapped closure and curries the copy: the owner of a curried closure is the
     * closure it wraps, so clearing it would leave nothing to call.
     */
    @Override
    public Closure<V> dehydrate() {
        return new CurriedClosure<V>(recreationIndex(), getOwner().dehydrate(), curriedArguments);
    }

    /**
     * Rehydrates the wrapped closure and curries the copy: the owner of a curried closure is the
     * closure it wraps, so replacing it would leave nothing to call.
     */
    @Override
    public Closure<V> rehydrate(final Object delegate, final Object owner, final Object thisObject) {
        return new CurriedClosure<V>(recreationIndex(), getOwner().rehydrate(delegate, owner, thisObject), curriedArguments);
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

    /**
     * Parameter types that remain when each signature is called through this curry.
     * Nested curries are applied from the inside out. A signature this curry cannot
     * accept is dropped. Returns {@code null} when a curry in the chain is varargs,
     * because a varargs closure keeps a single parameter list.
     */
    @Internal
    public List<Class<?>[]> expandSignatures(final List<Class<?>[]> signatures) {
        if (signatures == null) {
            return null;
        }
        List<Class<?>[]> current = signatures;
        Closure<?> uncurried = getOwner();
        if (uncurried instanceof CurriedClosure<?> nested) {
            current = nested.expandSignatures(current);
            if (current == null) {
                return null;
            }
        }
        if (isVararg()) {
            return null;
        }
        List<Class<?>[]> expanded = new ArrayList<>(current.size());
        for (Class<?>[] signature : current) {
            Class<?>[] remaining = parametersWithoutCurried(signature);
            if (remaining != null) {
                expanded.add(remaining);
            }
        }
        return expanded;
    }

    /**
     * Drops the parameters this curry binds. The insertion point is the one
     * {@link #getUncurriedArguments(Object...)} uses for a call that supplies
     * exactly the unbound parameters, so the types that stay are the ones the
     * caller passes in. Returns {@code null} when this signature cannot accept
     * the curried arguments.
     */
    private Class<?>[] parametersWithoutCurried(final Class<?>[] parameterTypes) {
        int curriedLen = curriedArguments.length;
        int arity = parameterTypes.length;
        if (arity < curriedLen) {
            return null;
        }
        int supplied = arity - curriedLen;
        // same acceptance test as getUncurriedArguments for a non-vararg call
        if (curriedLen + supplied < minParamsExpected) {
            return null;
        }
        int insertAt = Math.min(index, curriedLen + supplied - 1);
        if (insertAt < 0) {
            return null;
        }
        insertAt = Math.min(insertAt, supplied);
        Class<?>[] remaining = new Class<?>[supplied];
        System.arraycopy(parameterTypes, 0, remaining, 0, insertAt);
        int tail = arity - insertAt - curriedLen;
        if (tail > 0) {
            System.arraycopy(parameterTypes, insertAt + curriedLen, remaining, insertAt, tail);
        }
        return remaining;
    }

    /**
     * Rejects a deserialized closure whose owner/delegate/thisObject references form a cycle, which would
     * otherwise recurse indefinitely on invocation. See {@link Closure#checkForReferenceCycle}.
     */
    @Serial
    private Object readResolve() throws ObjectStreamException {
        Closure.checkForReferenceCycle(this);
        return this;
    }
}

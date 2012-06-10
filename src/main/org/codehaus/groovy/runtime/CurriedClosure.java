/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;

/**
 * A wrapper for Closure to support currying.
 * Normally used only internally through the <code>curry()</code>, <code>rcurry()</code> or
 * <code>ncurry()</code> methods on <code>Closure</code>.
 * Typical usages:
 * <pre>
 * // normal usage
 * def unitAdder = { first, second, unit -> "${first + second} $unit" }
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
 * @author Jochen Theodorou
 * @author Paul King
 */
public final class CurriedClosure<V> extends Closure<V> {

    private Object[] curriedParams;
    private int index;
    private int numTrailingArgs = 0;
    private Class varargType = null;

    public CurriedClosure(int index, Closure<V> uncurriedClosure, Object... arguments) {
        super(uncurriedClosure.clone());
        curriedParams = arguments;
        this.index = index;
        final int origMaxLen = uncurriedClosure.getMaximumNumberOfParameters();
        maximumNumberOfParameters = origMaxLen - arguments.length;
        Class[] classes = uncurriedClosure.getParameterTypes();
        Class lastType = classes.length == 0 ? null : classes[classes.length-1];
        if (lastType != null && lastType.isArray()) {
            varargType = lastType;
        }

        if (isVararg()) {
            if (index < 0) {
                numTrailingArgs = (-index) - arguments.length;
            }
        } else {
            // perform some early param checking for non-vararg case
            if (index < 0) {
                // normalise
                this.index += origMaxLen;
            }
            if (maximumNumberOfParameters < 0) {
                throw new IllegalArgumentException("Can't curry " + arguments.length + " arguments for a closure with " + origMaxLen + " parameters.");
            }
            if (index < 0) {
                if (index < -origMaxLen || index > -arguments.length)
                    throw new IllegalArgumentException("To curry " + arguments.length + " argument(s) expect index range " +
                            (-origMaxLen) + ".." + (-arguments.length) + " but found " + index);
            } else if (index > maximumNumberOfParameters) {
                throw new IllegalArgumentException("To curry " + arguments.length + " argument(s) expect index range 0.." +
                        maximumNumberOfParameters + " but found " + index);
            }
        }
    }

    public CurriedClosure(Closure<V> uncurriedClosure, Object... arguments) {
        this(0, uncurriedClosure, arguments);
    }

    public Object[] getUncurriedArguments(Object... arguments) {
        if (isVararg()) {
            int normalizedIndex = index < 0 ? index + arguments.length + curriedParams.length : index;
            if (normalizedIndex < 0 || normalizedIndex > arguments.length) {
                throw new IllegalArgumentException("When currying expected index range between " +
                        (-arguments.length - curriedParams.length) + ".." + (arguments.length + curriedParams.length) + " but found " + index);
            }
            final Object newCurriedParams[] = new Object[curriedParams.length + arguments.length];
            System.arraycopy(arguments, 0, newCurriedParams, 0, normalizedIndex);
            System.arraycopy(curriedParams, 0, newCurriedParams, normalizedIndex, curriedParams.length);
            if (arguments.length - normalizedIndex > 0)
                System.arraycopy(arguments, normalizedIndex, newCurriedParams, curriedParams.length + normalizedIndex, arguments.length - normalizedIndex);
            return newCurriedParams;
        }
        final Object newCurriedParams[] = new Object[curriedParams.length + arguments.length];
        int newIndex = Math.min(index, curriedParams.length + arguments.length - 1);
        System.arraycopy(arguments, 0, newCurriedParams, 0, newIndex);
        System.arraycopy(curriedParams, 0, newCurriedParams, newIndex, curriedParams.length);
        if (arguments.length - newIndex > 0)
            System.arraycopy(arguments, newIndex, newCurriedParams, curriedParams.length + newIndex, arguments.length - newIndex);
        return newCurriedParams;
    }

    public void setDelegate(Object delegate) {
        ((Closure) getOwner()).setDelegate(delegate);
    }

    public Object getDelegate() {
        return ((Closure) getOwner()).getDelegate();
    }

    public void setResolveStrategy(int resolveStrategy) {
        ((Closure) getOwner()).setResolveStrategy(resolveStrategy);
    }

    public int getResolveStrategy() {
        return ((Closure) getOwner()).getResolveStrategy();
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        Closure<V> uncurriedClosure = (Closure<V>) ((Closure) getOwner()).clone();
        return new CurriedClosure<V>(index, uncurriedClosure, curriedParams);
    }

    public Class[] getParameterTypes() {
        Class[] oldParams = ((Closure) getOwner()).getParameterTypes();
        int extraParams = 0;
        int gobbledParams = curriedParams.length;
        if (isVararg()) {
            int numNonVarargs = oldParams.length - 1;
            if (index < 0) {
                int absIndex = index < 0 ? -index : index;
                // do -ve indexes based on actual args, so can't accurately calculate type here
                // so work out minimal type params and vararg on end will allow for other possibilities
                if (absIndex > numNonVarargs) gobbledParams = numNonVarargs;
                int newNumNonVarargs = numNonVarargs - gobbledParams;
                if (absIndex - curriedParams.length > newNumNonVarargs) extraParams = absIndex - curriedParams.length - newNumNonVarargs;
                int keptParams = Math.max(numNonVarargs - absIndex, 0);
                Class[] newParams = new Class[keptParams + newNumNonVarargs + extraParams + 1];
                System.arraycopy(oldParams, 0, newParams, 0, keptParams);
                for (int i = 0; i < newNumNonVarargs; i++) newParams[keptParams + i] = Object.class;
                for (int i = 0; i < extraParams; i++) newParams[keptParams + newNumNonVarargs + i] = varargType.getComponentType();
                newParams[newParams.length - 1] = varargType;
                return newParams;
            }
            int leadingKept = Math.min(index, numNonVarargs);
            int trailingKept = Math.max(numNonVarargs - leadingKept - curriedParams.length, 0);
            if (index > leadingKept) extraParams = index - leadingKept;
            Class[] newParams = new Class[leadingKept + trailingKept + extraParams + 1];
            System.arraycopy(oldParams, 0, newParams, 0, leadingKept);
            if (trailingKept > 0) System.arraycopy(oldParams, leadingKept + curriedParams.length, newParams, leadingKept, trailingKept);
            for (int i = 0; i < extraParams; i++) newParams[leadingKept + trailingKept + i] = varargType.getComponentType();
            newParams[newParams.length - 1] = varargType;
            return newParams;
        }
        Class[] newParams = new Class[oldParams.length - gobbledParams + extraParams];
        System.arraycopy(oldParams, 0, newParams, 0, index);
        if (newParams.length - index > 0)
            System.arraycopy(oldParams, curriedParams.length + index, newParams, index, newParams.length - index);
        return newParams;
    }

    private boolean isVararg() {
        return varargType != null;
    }
}

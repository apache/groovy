/*
 * Copyright 2003-2010 the original author or authors.
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
public final class CurriedClosure extends Closure {

    private Object[] curriedParams;
    private int index;

    public CurriedClosure(int index, Closure uncurriedClosure, Object[] arguments) {
        super(uncurriedClosure.clone());
        curriedParams = arguments;
        this.index = index;
        final int origMaxLen = uncurriedClosure.getMaximumNumberOfParameters();
        maximumNumberOfParameters = origMaxLen - arguments.length;
        // normalise
        if (index < 0) {
            if (index < -origMaxLen || index > -arguments.length)
                throw new IllegalArgumentException("To curry " + arguments.length + " argument(s) expect index range " +
                        (-origMaxLen) + ".." + (-arguments.length) + " but found " + index);
            this.index += origMaxLen;
        } else if (index > maximumNumberOfParameters) {
            throw new IllegalArgumentException("To curry " + arguments.length + " argument(s) expect index range 0.." +
                    maximumNumberOfParameters + " but found " + index);
        }
    }

    public CurriedClosure(Closure uncurriedClosure, Object[] arguments) {
        this(0, uncurriedClosure, arguments);
    }

    @Deprecated
    public CurriedClosure(Closure uncurriedClosure, int i) {
        this(uncurriedClosure, new Object[]{Integer.valueOf(i)});
    }

    public Object[] getUncurriedArguments(Object[] arguments) {
        final Object newCurriedParams[] = new Object[curriedParams.length + arguments.length];
        System.arraycopy(arguments, 0, newCurriedParams, 0, index);
        System.arraycopy(curriedParams, 0, newCurriedParams, index, curriedParams.length);
        if (arguments.length - index > 0)
            System.arraycopy(arguments, index, newCurriedParams, curriedParams.length + index, arguments.length - index);
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

    public Object clone() {
        Closure uncurriedClosure = (Closure) ((Closure) getOwner()).clone();
        return new CurriedClosure(uncurriedClosure, curriedParams);
    }

    public Class[] getParameterTypes() {
        Class[] oldParams = ((Closure) getOwner()).getParameterTypes();
        Class[] newParams = new Class[oldParams.length - curriedParams.length];
        System.arraycopy(oldParams, 0, newParams, 0, index);
        if (newParams.length - index > 0)
            System.arraycopy(oldParams, curriedParams.length + index, newParams, index, newParams.length - index);
        return newParams;
    }
}

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

import java.util.List;

/**
 * A wrapper for Closure to support composition.
 * Normally used only internally through the <code>rightShift()</code> and
 * <code>leftShift()</code> methods on <code>Closure</code>.
 * <p>
 * Typical usages:
 * <pre class="groovyTestCase">
 * def twice = { a {@code ->} a * 2 }
 * def inc = { b {@code ->} b + 1 }
 * def f = { x {@code ->} twice(inc(x)) } // longhand
 * def g = inc {@code >>} twice
 * def h = twice {@code <<} inc
 * assert f(10) == 22
 * assert g(10) == 22
 * assert h(10) == 22
 *
 * def s2c = { it.chars[0] }
 * {@code def p = Integer.&toHexString >> s2c >> Character.&toUpperCase}
 * assert p(15) == 'F'
 *
 * def multiply = { a, b {@code ->} a * b }
 * def identity = { a {@code ->} [a, a] }
 * def sq = identity {@code >>} multiply
 * assert (1..5).collect{ sq(it) } == [1, 4, 9, 16, 25]
 *
 * def add3 = { a, b, c {@code ->} a + b + c }
 * def add2plus10 = add3.curry(10)
 * def multBoth = { a, b, c {@code ->} [a*c, b*c] }
 * def twiceBoth = multBoth.rcurry(2)
 * def twiceBothPlus10 = twiceBoth {@code >>} add2plus10
 * assert twiceBothPlus10(5, 10) == 40
 * </pre>
 */
public final class ComposedClosure<V> extends Closure<V> {

    private static final long serialVersionUID = -4816724431590921285L;
    private Closure first;
    private Closure<V> second;

    public ComposedClosure(Closure first, Closure<V> second) {
        super(first.clone());
        this.first = (Closure) getOwner();
        this.second = (Closure<V>) second.clone();
        maximumNumberOfParameters = first.getMaximumNumberOfParameters();
    }

    @Override
    public void setDelegate(Object delegate) {
        ((Closure) getOwner()).setDelegate(delegate);
        second.setDelegate(delegate);
    }

    @Override
    public Object getDelegate() {
        return ((Closure) getOwner()).getDelegate();
    }

    @Override
    public void setResolveStrategy(int resolveStrategy) {
        ((Closure) getOwner()).setResolveStrategy(resolveStrategy);
        second.setResolveStrategy(resolveStrategy);
    }

    @Override
    public int getResolveStrategy() {
        return ((Closure) getOwner()).getResolveStrategy();
    }

    @Override
    public Object clone() {
        return new ComposedClosure<V>(first, second);
    }

    @Override
    public Class[] getParameterTypes() {
        return first.getParameterTypes();
    }

    public Object doCall(Object... args) {
        return call(args);
    }

    @Override
    public V call(Object... args) {
        Object temp = first.call(args);
        if (temp instanceof List && second.getParameterTypes().length > 1) temp = ((List) temp).toArray();
        return temp instanceof Object[] ? second.call((Object[]) temp) : second.call(temp);
    }
}

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

import java.util.List;

/**
 * A Range represents the list of discrete items between some starting (or <code>from</code>)
 * value and <em>working up</em> towards some ending (or <code>to</code>) value.
 * For a reverse range, the list is obtained by starting at the <code>to</code> value and
 * <em>working down</em> towards the <code>from</code> value.
 *
 * The concept of <em>working up</em> and <em>working down</em> is dependent on the range implementation.
 * In the general case, working up involves successive calls to the first item's <code>next()</code>
 * method while working down involves calling the <code>previous()</code> method. Optimized
 * numerical ranges may apply numeric addition or subtraction of some numerical step size.
 *
 * Particular range implementations may also support the notion of inclusivity
 * and exclusivity with respect to the ending value in the range.
 * E.g. {@code 1..3 == [1, 2, 3]}; but {@code 1..<3 == [1, 2]}.
 *
 * In general, the second boundary may not be contained in the range,
 * and <code>a..b</code> may produce a different set of elements than <code>(b..a).reversed()</code>.
 * E.g.  <code>1..2.5 == [1, 2]</code>; but <code>2.5..1 == [2.5, 1.5]</code>.
 *
 * Implementations can be memory efficient by storing just the <code>from</code> and <code>to</code> boundary
 * values rather than eagerly creating all discrete items in the conceptual list. The actual discrete items
 * can be lazily calculated on an as needed basis (e.g. when calling methods from the <code>java.util.List</code>
 * interface or the additional <code>step</code> methods in the <code>Range</code> interface).
 *
 * In addition to the methods related to a Range's "discrete items" abstraction, there is a method,
 * <code>containsWithinBounds</code> which, for numerical ranges, allows checking within the continuous
 * interval between the Range's boundary values.
 */
public interface Range<T extends Comparable> extends List<T> {
    /**
     * The lower value in the range.
     *
     * @return the lower value in the range.
     */
    T getFrom();

    /**
     * The upper value in the range.
     *
     * @return the upper value in the range
     */
    T getTo();

    /**
     * Indicates whether this is a reverse range which iterates backwards
     * starting from the to value and ending on the from value
     *
     * @return <code>true</code> if this is a reverse range
     */
    boolean isReverse();

    /**
     * Indicates whether an object is greater than or equal to the <code>from</code>
     * value for the range and less than or equal to the <code>to</code> value.
     * <p>
     * This may be true even for values not contained in the range.
     *
     * Example: from = 1.5, to = 3, next() increments by 1
     * containsWithinBounds(2) == true
     * contains(2) == false
     *
     * @param o the object to check against the boundaries of the range
     * @return <code>true</code> if the object is between the from and to values
     */
    boolean containsWithinBounds(Object o);

    /**
     * Steps through the range, calling a closure for each item.
     *
     * @param step    the amount by which to step. If negative, steps through the range backwards.
     * @param closure the {@link Closure} to call
     */
    void step(int step, Closure closure);

    /**
     * Forms a list by stepping through the range by the indicated interval.
     *
     * @param step the amount by which to step. If negative, steps through the range backwards.
     * @return the list formed by stepping through the range by the indicated interval.
     */
    List<T> step(int step);

    /**
     * @return the verbose {@link String} representation of this {@link Range} as would be typed into a console to create the {@link Range} instance
     */
    String inspect();
}

/*
 * Copyright 2003-2008 the original author or authors.
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
package groovy.lang;

import java.util.List;

/**
 * A Range represents the list of all items obtained by starting from a
 * <code>from</code> value and calling <code>next()</code> successively
 * until you reach the <code>to</code> value. For a reverse range,
 * the list is obtained by starting at the <code>to</code> value and
 * successively calling <code>previous()</code> until the <code>from</code>
 * value is reached.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public interface Range extends List {
    /**
     * The lower value in the range.
     *
     * @return the lower value in the range.
     */
    Comparable getFrom();

    /**
     * The upper value in the range.
     *
     * @return the upper value in the range
     */
    Comparable getTo();

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
     *
     * @param o the object to check against the boundaries of the range
     * @return <code>true</code> if the object is between the from and to values
     */
    boolean containsWithinBounds(Object o);

    /**
     * Steps through the range, calling a closure for each number.
     *
     * @param step    the amount by which to step. If negative, steps through the
     *                range backwards.
     * @param closure the {@link Closure} to call
     */
    void step(int step, Closure closure);

    /**
     * Forms a list by stepping through the range by the indicated interval.
     *
     * @param step the amount by which to step. If negative, steps through the
     *             range backwards.
     * @return the list formed by stepping through the range by the indicated
     *         interval.
     */
    List step(int step);

    /**
     * @return the verbose {@link String} representation of this {@link Range} as would be typed into a console
     *         to create the {@link Range} instance
     */
    String inspect();
}

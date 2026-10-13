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
package groovy.time;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable holder for the outcome of a timed execution: the value produced
 * together with the elapsed time in nanoseconds. Instances are created by the
 * {@code timed} extension method rather than being constructed directly.
 * <p>
 * The elapsed time is measured with {@link System#nanoTime()}, which is
 * monotonic and unaffected by wall-clock adjustments. Convenience views of the
 * duration are available via {@link #getDuration()} and {@link #getMillis()},
 * which Groovy exposes as the {@code duration} and {@code millis} properties.
 * <pre class="groovyTestCase">
 * def t = System.timed { (1..1_000).sum() }
 * assert t.result == 500_500
 * assert t.nanos &gt;= 0
 * assert t.duration instanceof Duration
 * </pre>
 * Like a Groovy record, it also supports positional access to its components,
 * so it can be used with multiple assignment:
 * <pre class="groovyTestCase">
 * def (r, n) = System.timed { 'done' }
 * assert r == 'done'
 * assert n instanceof Long
 * </pre>
 *
 * @param <T>    the type of the timed result
 * @param result the value returned by the timed code
 * @param nanos  the elapsed time in nanoseconds, as measured by {@link System#nanoTime()}
 * @since 6.0.0
 */
public record Timed<T>(T result, long nanos) {

    /**
     * Returns the elapsed time as a {@link Duration}. Exposed to Groovy
     * as the read-only {@code duration} property.
     *
     * @return the elapsed time as a {@code Duration}
     * @since 6.0.0
     */
    public Duration getDuration() {
        return Duration.ofNanos(nanos);
    }

    /**
     * Returns the elapsed time in whole milliseconds, truncated toward zero.
     * Exposed to Groovy as the read-only {@code millis} property.
     *
     * @return the elapsed time in milliseconds
     * @since 6.0.0
     */
    public long getMillis() {
        return nanos / 1_000_000;
    }

    /**
     * Returns the record component at the given index: {@code 0} for the
     * {@link #result() result}, {@code 1} for the {@link #nanos() nanos}.
     * Supports Groovy's subscript operator and positional multiple assignment.
     * <pre class="groovyTestCase">
     * def t = System.timed { 42 }
     * assert t[0] == 42
     * assert t[1] == t.nanos
     * def (r, n) = t
     * assert r == 42 &amp;&amp; n == t.nanos
     * </pre>
     *
     * @param i the component index
     * @return the component value
     * @throws IllegalArgumentException if the index is not {@code 0} or {@code 1}
     * @since 6.0.1
     */
    public Object getAt(int i) {
        switch (i) {
            case 0: return result;
            case 1: return nanos;
            default: throw new IllegalArgumentException("No record component with index: " + i);
        }
    }

    /**
     * Returns the record components, in declaration order, as an unmodifiable list.
     * <pre class="groovyTestCase">
     * def t = System.timed { 'x' }
     * assert t.toList() == ['x', t.nanos]
     * </pre>
     *
     * @return an unmodifiable list containing the result and the nanos
     * @since 6.0.1
     */
    public List<Object> toList() {
        return Collections.unmodifiableList(Arrays.asList(result, nanos));
    }

    /**
     * Returns the record components, in declaration order, as an unmodifiable
     * map keyed by component name.
     * <pre class="groovyTestCase">
     * def t = System.timed { 'x' }
     * assert t.toMap() == [result: 'x', nanos: t.nanos]
     * </pre>
     *
     * @return an unmodifiable map with {@code result} and {@code nanos} entries
     * @since 6.0.1
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("result", result);
        map.put("nanos", nanos);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the number of record components, which is always {@code 2}.
     * <pre class="groovyTestCase">
     * assert System.timed { null }.size() == 2
     * </pre>
     *
     * @return the number of record components
     * @since 6.0.1
     */
    public int size() {
        return 2;
    }
}

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
 * assert t.duration instanceof java.time.Duration
 * </pre>
 *
 * @param <T>    the type of the timed result
 * @param result the value returned by the timed code
 * @param nanos  the elapsed time in nanoseconds, as measured by {@link System#nanoTime()}
 * @since 6.0.0
 */
public record Timed<T>(T result, long nanos) {

    /**
     * Returns the elapsed time as a {@link java.time.Duration}. Exposed to Groovy
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
}

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
package groovy.transform;

import org.apache.groovy.lang.annotation.Incubating;

/**
 * The packing behaviour of a {@link PackedClosures} scope. {@link #LENIENT}, {@link #WARN} and
 * {@link #STRICT} all pack, differing only in how they report a closure that could <em>not</em> be
 * packed (declines are otherwise silent). {@link #DISABLED} is the opposite: it opts the scope out
 * of packing entirely, so the most-specific declaration wins — a {@code DISABLED} method inside a
 * packed class, or vice versa, and it overrides the automatic {@code groovy.target.closure.pack} flag
 * as well.
 *
 * @see PackedClosures#mode()
 */
@Incubating
public enum PackMode {

    /** Do not pack any closure in this scope; overrides an enclosing opt-in and the automatic flag. */
    DISABLED,

    /** Silently fall back to a generated closure class for any closure that cannot be packed (default). */
    LENIENT,

    /** Emit a compiler warning, naming the closure and why it declined, for each that cannot be packed. */
    WARN,

    /** Emit a compiler error for any closure in the scope that cannot be packed. */
    STRICT
}

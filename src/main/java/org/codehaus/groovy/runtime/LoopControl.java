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

/**
 * Compiler-internal loop-control signals used to implement {@code break} and
 * {@code continue} inside closures passed to cooperating iterator methods
 * (GROOVY-12126). The compiler lowers {@code break}/{@code continue} in an
 * eligible closure to {@code throw LoopControl.BREAK}/{@code CONTINUE};
 * iterator methods marked {@link groovy.transform.SupportsLoopControl} catch
 * the signal per element and stop or skip accordingly.
 * <p>
 * The two signals are preallocated, carry no stack trace, and cannot be
 * instantiated by user code, so the per-element cost of a cooperating loop is
 * a try/catch region that is free unless a signal is actually thrown. If a
 * signal reaches a non-cooperating caller it surfaces with an explanatory
 * message rather than being silently swallowed.
 *
 * @since 6.0.0
 */
public final class LoopControl extends RuntimeException {

    private static final long serialVersionUID = 7972103766131505698L;

    /** Signal that the current iteration should stop (loop {@code break}). */
    public static final LoopControl BREAK = new LoopControl("break");

    /** Signal that the current element should be skipped (loop {@code continue}). */
    public static final LoopControl CONTINUE = new LoopControl("continue");

    private LoopControl(final String kind) {
        super("'" + kind + "' used in a closure passed to a method that does not support loop control",
                null, false, false);
    }
}

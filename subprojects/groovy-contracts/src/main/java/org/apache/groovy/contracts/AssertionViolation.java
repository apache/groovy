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
package org.apache.groovy.contracts;

/**
 * <p>Abstract base class for all assertion violations.</p>
 */
public abstract class AssertionViolation extends AssertionError {

    /**
     * Creates a violation without an explicit detail message and registers it with the current tracker.
     */
    protected AssertionViolation() {
        ViolationTracker.INSTANCE.get().track(this);
    }

    /**
     * Creates a violation with an object-valued detail and registers it with the current tracker.
     *
     * @param o the detail object passed to {@link AssertionError}
     */
    protected AssertionViolation(Object o) {
        super(o);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    /**
     * Creates a violation with a boolean detail and registers it with the current tracker.
     *
     * @param b the detail value passed to {@link AssertionError}
     */
    protected AssertionViolation(boolean b) {
        super(b);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    /**
     * Creates a violation with a character detail and registers it with the current tracker.
     *
     * @param c the detail value passed to {@link AssertionError}
     */
    protected AssertionViolation(char c) {
        super(c);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    /**
     * Creates a violation with an integer detail and registers it with the current tracker.
     *
     * @param i the detail value passed to {@link AssertionError}
     */
    protected AssertionViolation(int i) {
        super(i);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    /**
     * Creates a violation with a long detail and registers it with the current tracker.
     *
     * @param l the detail value passed to {@link AssertionError}
     */
    protected AssertionViolation(long l) {
        super(l);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    /**
     * Creates a violation with a floating-point detail and registers it with the current tracker.
     *
     * @param v the detail value passed to {@link AssertionError}
     */
    protected AssertionViolation(float v) {
        super(v);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    /**
     * Creates a violation with a double-precision detail and registers it with the current tracker.
     *
     * @param v the detail value passed to {@link AssertionError}
     */
    protected AssertionViolation(double v) {
        super(v);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }
}

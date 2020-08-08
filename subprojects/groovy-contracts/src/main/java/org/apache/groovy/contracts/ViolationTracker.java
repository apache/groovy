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

import org.apache.groovy.contracts.util.Validate;

import java.util.TreeMap;

/**
 * <p>
 * A violation tracker is used to keep a list of pre-, post-condition or class-invariant
 * violations in chronological order. This is necessary to evaluate all parts of a pre- or postcondition, and still
 * being able to rethrow assertion errors.
 * </p>
 */
public class ViolationTracker {

    public static ThreadLocal<ViolationTracker> INSTANCE = new ThreadLocal<ViolationTracker>();

    public static void init() {
        INSTANCE.set(new ViolationTracker());
    }

    public static void deinit() {
        INSTANCE.remove();
    }

    public static boolean violationsOccurred() {
        return INSTANCE.get().hasViolations();
    }

    public static void rethrowFirst() {
        throw INSTANCE.get().first();
    }

    public static void rethrowLast() {
        throw INSTANCE.get().last();
    }

    private TreeMap<Long, AssertionViolation> violations = new TreeMap<Long, AssertionViolation>();

    public void track(final AssertionViolation assertionViolation) {
        Validate.notNull(assertionViolation);

        violations.put(System.nanoTime(), assertionViolation);
    }

    public boolean hasViolations() {
        return violations.size() > 0;
    }

    public AssertionViolation first() {
        return violations.firstEntry().getValue();
    }

    public AssertionViolation last() {
        return violations.lastEntry().getValue();
    }
}

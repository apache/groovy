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
 * <p>Thrown whenever a class invariant violation occurs.</p>
 *
 * @see AssertionViolation
 */
public class ClassInvariantViolation extends AssertionViolation {

    /**
     * Creates a class-invariant violation without an explicit detail message.
     */
    public ClassInvariantViolation() {
    }

    /**
     * Creates a class-invariant violation with an object-valued detail.
     *
     * @param o the detail object
     */
    public ClassInvariantViolation(Object o) {
        super(o);
    }

    /**
     * Creates a class-invariant violation with a boolean detail.
     *
     * @param b the detail value
     */
    public ClassInvariantViolation(boolean b) {
        super(b);
    }

    /**
     * Creates a class-invariant violation with a character detail.
     *
     * @param c the detail value
     */
    public ClassInvariantViolation(char c) {
        super(c);
    }

    /**
     * Creates a class-invariant violation with an integer detail.
     *
     * @param i the detail value
     */
    public ClassInvariantViolation(int i) {
        super(i);
    }

    /**
     * Creates a class-invariant violation with a long detail.
     *
     * @param l the detail value
     */
    public ClassInvariantViolation(long l) {
        super(l);
    }

    /**
     * Creates a class-invariant violation with a floating-point detail.
     *
     * @param v the detail value
     */
    public ClassInvariantViolation(float v) {
        super(v);
    }

    /**
     * Creates a class-invariant violation with a double-precision detail.
     *
     * @param v the detail value
     */
    public ClassInvariantViolation(double v) {
        super(v);
    }
}

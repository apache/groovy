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
 * Thrown whenever a {@code @ThrowsIf} exceptional-contract violation is detected
 * by a {@code checked} arm-set: the method returned normally although a
 * must-throw condition held, or threw a matching exception although no arm's
 * condition held (the latter checked only for {@code exhaustive} arm-sets).
 * <p>
 * Deliberately <em>not</em> the arm's declared exception: the declared exception
 * is defined behaviour delivered at method entry, whereas this violation reports
 * a broken <em>implementation</em> of the contract — late-throwing the declared
 * exception would masquerade the bug as defined behaviour.
 *
 * @see AssertionViolation
 * @since 6.0.0
 */
public class ThrowsIfViolation extends AssertionViolation {

    /**
     * Creates a throws-if violation without an explicit detail message.
     */
    public ThrowsIfViolation() {
    }

    /**
     * Creates a throws-if violation with an object-valued detail.
     *
     * @param o the detail object
     */
    public ThrowsIfViolation(Object o) {
        super(o);
    }

    /**
     * Creates a throws-if violation with a boolean detail.
     *
     * @param b the detail value
     */
    public ThrowsIfViolation(boolean b) {
        super(b);
    }

    /**
     * Creates a throws-if violation with a char detail.
     *
     * @param c the detail value
     */
    public ThrowsIfViolation(char c) {
        super(c);
    }

    /**
     * Creates a throws-if violation with an int detail.
     *
     * @param i the detail value
     */
    public ThrowsIfViolation(int i) {
        super(i);
    }

    /**
     * Creates a throws-if violation with a long detail.
     *
     * @param l the detail value
     */
    public ThrowsIfViolation(long l) {
        super(l);
    }

    /**
     * Creates a throws-if violation with a float detail.
     *
     * @param f the detail value
     */
    public ThrowsIfViolation(float f) {
        super(f);
    }

    /**
     * Creates a throws-if violation with a double detail.
     *
     * @param d the detail value
     */
    public ThrowsIfViolation(double d) {
        super(d);
    }
}

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
 * Thrown whenever a loop variant (decreases/increases) violation occurs.
 *
 * @see AssertionViolation
 * @since 6.0.0
 */
public class LoopVariantViolation extends AssertionViolation {

    /**
     * Creates a loop-variant violation without an explicit detail message.
     */
    public LoopVariantViolation() {
    }

    /**
     * Creates a loop-variant violation with an object-valued detail.
     *
     * @param o the detail object
     */
    public LoopVariantViolation(Object o) {
        super(o);
    }

    /**
     * Creates a loop-variant violation with a boolean detail.
     *
     * @param b the detail value
     */
    public LoopVariantViolation(boolean b) {
        super(b);
    }

    /**
     * Creates a loop-variant violation with a character detail.
     *
     * @param c the detail value
     */
    public LoopVariantViolation(char c) {
        super(c);
    }

    /**
     * Creates a loop-variant violation with an integer detail.
     *
     * @param i the detail value
     */
    public LoopVariantViolation(int i) {
        super(i);
    }

    /**
     * Creates a loop-variant violation with a long detail.
     *
     * @param l the detail value
     */
    public LoopVariantViolation(long l) {
        super(l);
    }

    /**
     * Creates a loop-variant violation with a floating-point detail.
     *
     * @param f the detail value
     */
    public LoopVariantViolation(float f) {
        super(f);
    }

    /**
     * Creates a loop-variant violation with a double-precision detail.
     *
     * @param d the detail value
     */
    public LoopVariantViolation(double d) {
        super(d);
    }
}

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
 * <p>Thrown whenever a postcondition violation occurs.</p>
 *
 * @see AssertionViolation
 */
public class PostconditionViolation extends AssertionViolation {

    /**
     * Creates a postcondition violation without an explicit detail message.
     */
    public PostconditionViolation() {
    }

    /**
     * Creates a postcondition violation with an object-valued detail.
     *
     * @param o the detail object
     */
    public PostconditionViolation(Object o) {
        super(o);
    }

    /**
     * Creates a postcondition violation with a boolean detail.
     *
     * @param b the detail value
     */
    public PostconditionViolation(boolean b) {
        super(b);
    }

    /**
     * Creates a postcondition violation with a character detail.
     *
     * @param c the detail value
     */
    public PostconditionViolation(char c) {
        super(c);
    }

    /**
     * Creates a postcondition violation with an integer detail.
     *
     * @param i the detail value
     */
    public PostconditionViolation(int i) {
        super(i);
    }

    /**
     * Creates a postcondition violation with a long detail.
     *
     * @param l the detail value
     */
    public PostconditionViolation(long l) {
        super(l);
    }

    /**
     * Creates a postcondition violation with a floating-point detail.
     *
     * @param v the detail value
     */
    public PostconditionViolation(float v) {
        super(v);
    }

    /**
     * Creates a postcondition violation with a double-precision detail.
     *
     * @param v the detail value
     */
    public PostconditionViolation(double v) {
        super(v);
    }
}

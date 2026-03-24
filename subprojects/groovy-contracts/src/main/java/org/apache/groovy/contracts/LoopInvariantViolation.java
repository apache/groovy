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
 * <p>Thrown whenever a loop invariant violation occurs.</p>
 *
 * @see AssertionViolation
 * @since 6.0.0
 */
public class LoopInvariantViolation extends AssertionViolation {

    public LoopInvariantViolation() {
    }

    public LoopInvariantViolation(Object o) {
        super(o);
    }

    public LoopInvariantViolation(boolean b) {
        super(b);
    }

    public LoopInvariantViolation(char c) {
        super(c);
    }

    public LoopInvariantViolation(int i) {
        super(i);
    }

    public LoopInvariantViolation(long l) {
        super(l);
    }

    public LoopInvariantViolation(float f) {
        super(f);
    }

    public LoopInvariantViolation(double d) {
        super(d);
    }
}


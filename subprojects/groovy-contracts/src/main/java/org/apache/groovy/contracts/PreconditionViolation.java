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
 * <p>Thrown whenever a precondition violation occurs.</p>
 *
 * @see AssertionViolation
 */
public class PreconditionViolation extends AssertionViolation {

    public PreconditionViolation() {
    }

    public PreconditionViolation(Object o) {
        super(o);
    }

    public PreconditionViolation(boolean b) {
        super(b);
    }

    public PreconditionViolation(char c) {
        super(c);
    }

    public PreconditionViolation(int i) {
        super(i);
    }

    public PreconditionViolation(long l) {
        super(l);
    }

    public PreconditionViolation(float v) {
        super(v);
    }

    public PreconditionViolation(double v) {
        super(v);
    }
}

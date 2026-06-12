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
 * Thrown when a method-level {@link groovy.contracts.Decreases} recursion
 * termination measure fails — either a recursive re-entry did not strictly
 * decrease the measure, or the measure became negative (not well-founded).
 *
 * @see AssertionViolation
 * @see LoopVariantViolation
 * @since 6.0.0
 */
public class RecursionVariantViolation extends AssertionViolation {

    /**
     * Creates a recursion-variant violation without an explicit detail message.
     */
    public RecursionVariantViolation() {
    }

    /**
     * Creates a recursion-variant violation with an object-valued detail.
     *
     * @param o the detail object
     */
    public RecursionVariantViolation(Object o) {
        super(o);
    }
}
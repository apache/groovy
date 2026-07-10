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
package org.codehaus.groovy.classgen.asm;

/**
 * The compilation strategy the GEP-27 capability analysis selects for a closure or lambda literal.
 * <p>
 * The lattice runs from most to least machinery: a full generated class preserves every {@code
 * Closure}/lambda capability; a packed adapter keeps {@code Closure} identity while removing the
 * per-closure class; a metafactory instance removes the class entirely for functional-interface
 * targets; elision removes the object. This enum is the shared vocabulary the closure and lambda
 * writers both select over; only the first two values are wired for closures today, with the
 * metafactory value already used by the lambda path and elision reserved for later work.
 */
public enum PackStrategy {

    /** S0 — generate the per-closure/per-lambda class (today's behaviour); preserves all capabilities. */
    FULL_CLASS,

    /** S1 — hoist the body to a method on the enclosing class behind a shared {@code PackedClosure} adapter. */
    PACKED_ADAPTER,

    /** S2 — hoist the body and bootstrap {@code LambdaMetafactory} against it; no generated class (SAM targets). */
    METAFACTORY,

    /** S3 — neither escapes nor needs an object; elide it entirely. */
    ELIDE
}

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
 * The compilation strategy the GEP-27 capability analysis selects for a closure literal: keep today's
 * per-closure class, or pack the closure by hoisting its body to a method on the enclosing class behind
 * a shared {@code PackedClosure} adapter. The GEP-27 lattice also foresees a metafactory strategy
 * (functional-interface targets) and object elision, but those are separate, later work and are not
 * represented here yet.
 */
public enum PackStrategy {

    /** S0 — generate the per-closure class (today's behaviour); preserves all {@code Closure} capabilities. */
    FULL_CLASS,

    /** S1 — hoist the body to a method on the enclosing class behind a shared {@code PackedClosure} adapter. */
    PACKED_ADAPTER,

    /**
     * GEP-27 OpenClosure spike (Groovy 7 reference implementation, {@code groovy.spike.openclosure}):
     * hoist the body with its free-name uses rewritten into calls on a leading
     * {@code OpenClosure.Resolver} parameter, and emit an {@code OpenClosure.AsClosure} adapter whose
     * {@code ClassicResolver} restores the full owner/delegate/resolveStrategy contract — packing the
     * free-name closures S1 must decline, still without a per-closure class.
     */
    OPEN_ADAPTER
}

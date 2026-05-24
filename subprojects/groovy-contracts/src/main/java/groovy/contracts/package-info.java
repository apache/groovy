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

/**
 * Design-by-contract (DbC) support with compile-time assertion generation.
 *
 * <p>
 * Stable annotations:
 * </p>
 * <ul>
 *   <li>{@link groovy.contracts.Requires @Requires} &ndash; method precondition</li>
 *   <li>{@link groovy.contracts.Ensures @Ensures} &ndash; method postcondition; closures
 *       may reference {@code result} and {@code old}</li>
 *   <li>{@link groovy.contracts.Invariant @Invariant} &ndash; class invariant, also usable
 *       as a loop invariant on {@code for} / {@code while} / {@code do-while} loops</li>
 *   <li>{@link groovy.contracts.Contracted @Contracted} &ndash; package- or class-level
 *       opt-in marker that enables contract processing</li>
 * </ul>
 * <p>
 * Incubating in 6.0.0 (semantics may evolve in a subsequent 6.x release):
 * </p>
 * <ul>
 *   <li>{@link groovy.contracts.Modifies @Modifies} &ndash; frame condition declaring
 *       which fields and parameters a method may modify</li>
 *   <li>{@link groovy.contracts.Decreases @Decreases} &ndash; loop termination measure
 *       (a strictly-decreasing, non-negative variant)</li>
 * </ul>
 */
package groovy.contracts;

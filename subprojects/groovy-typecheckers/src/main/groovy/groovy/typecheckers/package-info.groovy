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
 * Custom type checking extensions for compile-time validation, activated via
 * {@code @TypeChecked(extensions = '...')} or {@code @CompileStatic(extensions = '...')}.
 *
 * <p>
 * Stable checkers:
 * </p>
 * <ul>
 *   <li>{@link groovy.typecheckers.RegexChecker} &ndash; validates regex patterns and
 *       group counts at compile time</li>
 *   <li>{@link groovy.typecheckers.FormatStringChecker} &ndash; validates
 *       {@code printf} / {@code String.format} specifiers against argument types,
 *       paired with the {@link groovy.typecheckers.FormatMethod @FormatMethod} marker
 *       annotation for user-defined format methods</li>
 * </ul>
 * <p>
 * Incubating in 6.0.0 (semantics may evolve in a subsequent 6.x release):
 * </p>
 * <ul>
 *   <li>{@link groovy.typecheckers.NullChecker} &ndash; null-safety analysis using
 *       {@code @Nullable} / {@code @NonNull} / {@code @MonotonicNonNull} annotations,
 *       with an optional strict flow-sensitive mode</li>
 *   <li>{@link groovy.typecheckers.ModifiesChecker} &ndash; verifies method bodies
 *       comply with their {@link groovy.contracts.Modifies @Modifies} frame conditions</li>
 *   <li>{@link groovy.typecheckers.PurityChecker} &ndash; enforces that {@code @Pure}
 *       methods have no side effects, with configurable {@code allows} categories</li>
 *   <li>{@link groovy.typecheckers.CombinerChecker} &ndash; verifies that the combiner
 *       passed to a parallel reduction ({@code sumParallel}, {@code injectParallel},
 *       {@code Stream.reduce}) carries the associativity contract those methods
 *       silently require</li>
 *   <li>{@link groovy.typecheckers.MonadicChecker} and
 *       {@link groovy.typecheckers.MonadicShapeChecker} &ndash; type-checking support
 *       for the {@code DO} macro and hand-written monadic chains over the standard
 *       carriers ({@code Optional}, {@code Stream}, {@code Awaitable}) and
 *       {@code @Monadic}-annotated types</li>
 * </ul>
 */
package groovy.typecheckers;

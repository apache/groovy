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
 * Internal utilities for the ANTLR4 (Parrot) parser: ATN management, friendly
 * error strategies ({@link DescriptiveErrorStrategy} fail-fast,
 * {@link RecoveringDescriptiveErrorStrategy} multi-error), and related
 * diagnostics ({@link MissingDelimiterDiagnostic} for relocated missing-closer
 * carets; fallback wording lives on {@link AbstractFriendlyErrorStrategy},
 * including reserved keywords, a leading {@code ?[} without a path to
 * attach to, array-creation mistakes, an unclosed type argument, a method
 * header in a statement position, an unmatched {@code do}, enum constants
 * that need {@code ;} before members, and a singleton expected-punctuation
 * token after optional newlines are ignored). Reporting is one listener
 * dispatch after locate/refine. Diagnostic helpers run only after
 * recognition has already failed so they do not affect steady-state parse
 * performance.
 */
package org.apache.groovy.parser.antlr4.internal;

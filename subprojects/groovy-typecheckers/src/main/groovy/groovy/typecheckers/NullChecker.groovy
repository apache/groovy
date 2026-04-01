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
package groovy.typecheckers

import org.apache.groovy.lang.annotation.Incubating
import org.apache.groovy.typecheckers.NullCheckingVisitor
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport

/**
 * A compile-time type checker that detects potential null dereferences and null-safety violations
 * in code annotated with {@code @Nullable}, {@code @NonNull}, and {@code @MonotonicNonNull} annotations.
 * <p>
 * This checker performs annotation-based null checking only. For additional flow-sensitive analysis
 * that tracks nullability through assignments and control flow (even in unannotated code),
 * use {@link StrictNullChecker} instead.
 * <p>
 * Supported annotations are recognized by simple name from any package:
 * <ul>
 *     <li>Nullable: {@code @Nullable}, {@code @CheckForNull}, {@code @MonotonicNonNull}</li>
 *     <li>Non-null: {@code @NonNull}, {@code @NotNull}, {@code @Nonnull}</li>
 * </ul>
 * <p>
 * Detected errors include:
 * <ul>
 *     <li>Assigning {@code null} to a {@code @NonNull} variable</li>
 *     <li>Passing {@code null} or a {@code @Nullable} value to a {@code @NonNull} parameter</li>
 *     <li>Returning {@code null} or a {@code @Nullable} value from a {@code @NonNull} method</li>
 *     <li>Dereferencing a {@code @Nullable} variable without a null check or safe navigation ({@code ?.})</li>
 *     <li>Dereferencing the result of a {@code @Nullable}-returning method without a null check</li>
 *     <li>Re-assigning {@code null} to a {@code @MonotonicNonNull} field after initialization</li>
 * </ul>
 * <p>
 * The checker recognizes null guards ({@code if (x != null)}), early exit patterns
 * ({@code if (x == null) return/throw}), and safe navigation ({@code ?.}).
 *
 * <pre>
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.NullChecker')}
 * void process(@Nullable String input) {
 *     // input.length()     // error: potential null dereference
 *     input?.length()       // ok: safe navigation
 *     if (input != null) {
 *         input.length()    // ok: null guard
 *     }
 * }
 * </pre>
 *
 * Over time, the idea would be to support more cases as per:
 * https://checkerframework.org/manual/#nullness-checker
 *
 * @see StrictNullChecker
 * @see NullCheckingVisitor
 */
@Incubating
class NullChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    @Override
    Object run() {
        NullCheckingVisitor.install(this, false)
    }
}

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
 * A compile-time type checker that performs all the annotation-based null checks of {@link NullChecker}
 * plus flow-sensitive null tracking for unannotated code.
 * <p>
 * In addition to the annotation-based checks, this checker tracks nullability through variable
 * assignments and control flow. This means it can detect potential null dereferences even when
 * code does not use {@code @Nullable}/{@code @NonNull} annotations:
 *
 * <pre>
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.StrictNullChecker')}
 * void process() {
 *     def x = null
 *     // x.toString()   // error: x may be null
 *     x = 'hello'
 *     x.toString()      // ok: x reassigned non-null
 * }
 * </pre>
 * <p>
 * The flow-sensitive tracking also recognizes return values from {@code @Nullable} methods
 * assigned to variables, null guards, and early exit patterns.
 * <p>
 * Use this checker for code that benefits from stricter null analysis. For code bases
 * with a mix of strictness requirements, apply {@code NullChecker} to relaxed code and
 * {@code StrictNullChecker} to strict code via per-class or per-method
 * {@code @TypeChecked} annotations.
 *
 * @see NullChecker
 * @see NullCheckingVisitor
 */
@Incubating
class StrictNullChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    @Override
    Object run() {
        NullCheckingVisitor.install(this, true)
    }
}

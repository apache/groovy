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
package groovy.transform;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guards the regex operators within the annotated scope against Regular
 * Expression Denial of Service (ReDoS). Match ({@code ==~}) and find
 * ({@code =~}) expressions are rewritten at compile time to deadline-guarded
 * {@link groovy.util.regex.RegexGuard} calls which throw
 * {@link groovy.util.regex.RegexTimeoutException} if evaluation, e.g. due to
 * catastrophic backtracking on adversarial input, exceeds the configured
 * timeout. Matching semantics are otherwise unchanged, including left-to-right
 * evaluation of the operands, which the generated call preserves by taking them
 * in the operator's order.
 * <pre>
 * &#64;SafeRegex(millis = 200)
 * class Handler {
 *     boolean check(String input) {
 *         input ==~ /(a+)+$/    // rewritten to RegexGuard.matchRegex(input, /(a+)+$/, 200)
 *     }
 * }
 * </pre>
 * For the find operator, the deadline covers the whole use of the returned
 * matcher, i.e. it starts when the matcher is created and later matcher
 * operations such as {@code find()} throw once it has passed.
 * <p>
 * When placed on a field or local variable declaration, only regex operators
 * lexically within the initializer expression are guarded; later uses of the
 * variable are unaffected. A guarded field initializer runs, as usual, in the
 * constructor (or in the static initializer for a static field, where a
 * timeout surfaces as an {@code ExceptionInInitializerError} whose cause is
 * the {@link groovy.util.regex.RegexTimeoutException}).
 * <p>
 * Limitations: only regex operators lexically visible within the annotated
 * scope are rewritten. Regex evaluation via method calls such as
 * {@code String#matches}, {@code replaceAll} or {@code split}, or occurring
 * in code called from the annotated scope, is not guarded; use
 * {@link groovy.util.regex.RegexGuard} explicitly for those. This is an
 * opt-in facility, never a blanket default.
 *
 * @see groovy.util.regex.RegexGuard
 * @since 6.0.0
 */
@Documented
@Incubating
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.SafeRegexASTTransformation")
public @interface SafeRegex {
    /**
     * Returns the timeout in milliseconds applied to each guarded regex
     * evaluation within the annotated scope. Must be positive.
     *
     * @return the timeout in milliseconds
     */
    long millis() default 1000L;
}

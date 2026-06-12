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
package groovy.contracts;

import groovy.lang.annotation.ExtendedElementType;
import groovy.lang.annotation.ExtendedTarget;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a termination measure, either for a <em>loop</em> or for a
 * <em>(recursive) method</em>. The closure must return a {@link Comparable}
 * value that <em>strictly decreases</em> and remains non-negative (i.e.,
 * {@code >= 0} for numeric types) — a well-founded measure.
 * <p>
 * <b>On a loop</b> ({@code for}/{@code while}/{@code do-while}) the expression is
 * evaluated at the start and end of each iteration and must decrease per
 * iteration; a {@link org.apache.groovy.contracts.LoopVariantViolation
 * LoopVariantViolation} is thrown otherwise. The measure may also be a
 * {@link java.util.List} compared lexicographically.
 * <pre>
 * int n = 10
 * {@code @Decreases}({ n })
 * while (n &gt; 0) { n-- }
 * </pre>
 * <p>
 * <b>On a method</b> the expression is a function of the method's parameters and
 * must strictly decrease (and stay {@code >= 0}) on every <em>recursive</em>
 * re-entry — a recursion termination measure. At runtime the value is captured on
 * entry and compared against the nearest enclosing invocation of the same method
 * (per thread); a {@link org.apache.groovy.contracts.RecursionVariantViolation
 * RecursionVariantViolation} is thrown if a recursive call fails to decrease it
 * or it becomes negative, turning a non-terminating recursion into an immediate,
 * localised error rather than a {@code StackOverflowError}.
 * <pre>
 * {@code @Requires}({ n &gt;= 0 })
 * {@code @Ensures}({ result &gt;= n })
 * {@code @Decreases}({ n })
 * static int sumUp(int n) { n == 0 ? 0 : sumUp(n - 1) + n }
 * </pre>
 *
 * @since 6.0.0
 * @see Invariant
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendedTarget(ExtendedElementType.LOOP)
@Incubating
@GroovyASTTransformationClass({
        "org.apache.groovy.contracts.ast.LoopVariantASTTransformation",
        "org.apache.groovy.contracts.ast.MethodVariantASTTransformation"
})
public @interface Decreases {
    /**
     * Returns the closure class that computes the variant value.
     *
     * @return the generated closure class backing the variant expression
     */
    Class value();
}

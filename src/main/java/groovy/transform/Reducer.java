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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a two-argument combining method is <em>associative</em> and
 * has an <em>identity element</em> &mdash; together, the algebraic contract
 * a seeded parallel reduction relies on. Equivalent to {@link Associative}
 * plus a declared zero such that {@code combine(zero, a) == a == combine(a, zero)}.
 * <p>
 * The {@link #zero()} member names the identity element as a constant
 * expression (for example {@code "0"}, {@code "''"}, or a fully-qualified
 * static field reference). When supplied, tooling such as
 * {@code groovy.typecheckers.CombinerChecker} can additionally check that the
 * {@code seed} passed to {@code injectParallel} matches the declared identity
 * where both are statically determinable. An empty value asserts associativity
 * and the existence of an identity without naming it.
 * <p>
 * As with {@link Associative}, this annotation <em>asserts</em> the laws; it
 * does not prove them. The assertion is intended to be backed by tests.
 *
 * @since 6.0.0
 */
@Documented
@Incubating
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reducer {
    /**
     * The identity element, as a constant expression. Empty means the
     * identity exists but is not named (associativity is still asserted).
     */
    String zero() default "";
}

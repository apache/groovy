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
 * Declares that a two-argument combining method is <em>associative</em>:
 * {@code combine(a, combine(b, c))} produces the same result as
 * {@code combine(combine(a, b), c)} for all inputs.
 * <p>
 * Associativity is the contract that makes a combiner safe to use with
 * order-independent reductions such as the parallel {@code Collection}
 * methods ({@code sumParallel}, {@code injectParallel}). Tooling such as
 * {@code groovy.typecheckers.CombinerChecker} consumes this declaration to
 * verify, at compile time, that a combiner passed to a parallel reduction
 * carries the contract.
 * <p>
 * This annotation <em>asserts</em> the law; it does not prove it. The
 * assertion is intended to be backed by tests (for example auto-derived
 * property-based tests). See also {@link Reducer}, which additionally
 * declares an identity element.
 *
 * @since 6.0.0
 */
@Documented
@Incubating
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Associative {
}

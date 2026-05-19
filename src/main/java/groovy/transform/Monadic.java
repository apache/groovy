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
 * Marks a type as participating in monadic comprehensions (the {@code DO} macro).
 * Optional members declare the bind/map method names when they diverge from the
 * structural convention ({@code flatMap}/{@code map}). When both are omitted, the
 * annotation merely opts the type in and the structural defaults apply.
 * <p>
 * Modelled on {@link groovy.transform.Reducer}: a pure marker, read by tooling,
 * with no AST transformation. The runtime dispatcher and the type checker match
 * this annotation <em>by simple name</em> ({@code Monadic}), exactly as
 * {@code groovy.typecheckers.CombinerChecker} matches {@code @Reducer}/{@code @Associative}.
 *
 * @since 6.0.0
 */
@Documented
@Incubating
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monadic {
    /** The flatMap-shaped bind method name. Empty means the structural default {@code flatMap}. */
    String bind() default "";

    /** The map method name. Empty means the structural default {@code map}. */
    String map() default "";
}

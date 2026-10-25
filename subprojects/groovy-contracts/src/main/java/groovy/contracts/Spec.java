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
 * Records a prose specification for a method, loop or local variable declaration.
 * <p>
 * The specification is written for both people and tools: it sits next to the typed
 * signature and any {@link Requires}/{@link Ensures} contracts, and a tool outside the
 * compiler can use it as the prompt for generating the annotated code ahead of time.
 * Unlike a plain comment, the specification can be found by tools, and the compiler
 * can tell a filled hole from an unfilled one.
 * <p>
 * An annotated element with no code yet is an <em>unfilled hole</em>, and reaching it
 * at runtime throws {@link UnsupportedOperationException} rather than silently returning
 * {@code null} or doing nothing:
 * <ul>
 *   <li>a method whose body is empty, including a {@code void} method (a filled method
 *   that intentionally does nothing can say so with an explicit {@code return})</li>
 *   <li>a {@code for}, {@code while} or {@code do-while} loop whose body is empty; the
 *   exception is thrown when execution reaches the loop</li>
 *   <li>a local variable declaration with no initializer; the exception is thrown when
 *   execution reaches the declaration</li>
 * </ul>
 * Abstract methods carry no body and are left unchanged. The compiler never generates
 * code from the specification itself. Annotations on loops and local variable
 * declarations are not written to the class file, so those specifications exist only
 * in source.
 * <p>
 * Example:
 * <pre>
 * class Dates {
 *     {@code @Spec}('''
 *         Parse an ISO-8601 calendar date (yyyy-MM-dd) into its year, month
 *         and day. Reject dates that do not exist, such as 2026-02-31.
 *     ''')
 *     {@code @Requires}({ text != null })
 *     static LocalDate parse(String text) { }   // unfilled: throws until filled
 * }
 * </pre>
 *
 * @since 7.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@ExtendedTarget(ExtendedElementType.LOOP)
@Incubating
@GroovyASTTransformationClass("org.apache.groovy.contracts.ast.SpecASTTransformation")
public @interface Spec {
    /**
     * Returns the prose specification.
     *
     * @return the specification text
     */
    String value();

    /**
     * Information written by tooling when it fills the hole, with no effect on bytecode.
     * Typically a record of the specification and generated code at the time of the fill,
     * so a tool can tell whether either has changed since. The format belongs to the tool
     * that writes it; the empty string (default) means no fill has been recorded.
     *
     * @return the tool-defined fill record
     */
    String fill() default "";
}

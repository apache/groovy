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
package groovy.transform.stc;

import org.apache.groovy.lang.annotation.Incubating;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter annotation marking a {@code Class} parameter as <em>compiler-supplied</em> under
 * static compilation. On the JVM, a method that needs the runtime type of a generic type
 * parameter cannot recover it (erasure), so the API must take an explicit {@code Class<T>}
 * token. That token is redundant with a type the compiler already knows &mdash; the receiver's
 * type argument. {@code @ClassTag} removes the redundancy.
 * <p>
 * Under {@code @CompileStatic} / {@code @TypeChecked}, when a call omits the {@code Class} token
 * argument(s), the type checker may select an overload whose parameters, beyond those matching the
 * supplied arguments, are all {@code @ClassTag Class<X>} and synthesise the {@code X.class}
 * argument(s) from the receiver's matching type argument(s), rewriting the call. Each
 * {@code @ClassTag} parameter is filled at its declared position &mdash; anywhere in the parameter
 * list, not just trailing &mdash; and the caller's supplied arguments map to the remaining
 * (non-tag) parameters in order.
 * <p>
 * As an example, {@code asChecked} declares an explicit token:
 * <pre>
 * public static &lt;T&gt; List&lt;T&gt; asChecked(List&lt;T&gt; self, &#64;ClassTag Class&lt;T&gt; type)
 * </pre>
 * which lets statically-typed callers omit it:
 * <pre>
 * List&lt;String&gt; list = [].asChecked()      // compiler injects String.class
 * </pre>
 * <p>
 * Selection is normally <em>additive</em>: the tagged overload is chosen only when the call as
 * written matches no method (as above &mdash; there is no token-less {@code asChecked()}). When a
 * token-less overload also matches, choosing the tagged one instead (<em>preemption</em>) changes
 * the meaning of existing source, so three conditions gate it:
 * <ul>
 *   <li><em>Intent</em> &mdash; the API author must declare the tagged overload preemptive with
 *   {@link #preempt() preempt=true}; without it the overload is only ever selected additively.
 *   Groovy's own checked {@code withDefault} overloads declare it (their lenient sibling predates
 *   them and could silently corrupt a typed map's key set).</li>
 *   <li><em>Containment</em> &mdash; a tagged overload may only preempt a token-less overload
 *   declared by the same class (or, for instance methods, the same class hierarchy). A library
 *   can upgrade callers of <em>its own</em> lenient API but can never capture calls owned by
 *   another library, so adding a jar to the compile classpath cannot re-route existing calls it
 *   does not already own.</li>
 *   <li><em>Consent</em> &mdash; the consumer may veto all preemption globally via
 *   {@link org.codehaus.groovy.control.CompilerConfiguration#setClassTagPreemptionDisabled} (also
 *   seeded from the {@code groovy.classtag.preemption.disable} system property). Code compiled
 *   with the veto behaves exactly as it did before any library declared preemption; a
 *   finer-grained (per-method or per-call-site) opt-out may be added later if experience shows
 *   the need.</li>
 * </ul>
 * <p>
 * Boundaries:
 * <ul>
 *   <li><em>Static only.</em> Dynamic Groovy ignores the annotation and binds the original
 *   overload; the shorter (token-less) spelling is static-only syntax. If no token can be
 *   synthesised there is no zero-token method to bind, so the call fails to resolve.</li>
 *   <li><em>The type variable must be statically known.</em> {@code List<String>} works;
 *   {@code def}, raw {@code List}, and wildcard-only types do not &mdash; no token is synthesised.</li>
 *   <li><em>Receiver type arguments only.</em> A type variable declared by the method itself
 *   (which shadows any like-named class variable) cannot be reified from the receiver; tagging
 *   one on an instance method compiled from source is a compile-time error. Static methods are
 *   exempt: in the extension-method authoring pattern the type variable is necessarily
 *   method-declared and connects to the receiver through the self parameter.</li>
 *   <li><em>Erased fidelity.</em> The synthesised {@code Class<X>} reifies only the erased class:
 *   {@code List<String>} and {@code List<Integer>} are indistinguishable.</li>
 *   <li><em>Nothing-to-gain guard.</em> When every token would erase to {@code Object} (e.g. a
 *   {@code List<Object>} receiver), no token is synthesised &mdash; a checked {@code Object} view
 *   could never reject anything &mdash; so an additive call fails to resolve and a preemptive
 *   upgrade is skipped (the lenient overload is kept).</li>
 *   <li><em>Escape hatch.</em> Passing the {@code Class} explicitly always works, in every mode.</li>
 * </ul>
 *
 * @since 6.0.0
 */
@Documented
@Incubating
@Retention(RetentionPolicy.RUNTIME) // read off (possibly reflection-backed) extension-method parameters by the STC
@Target(ElementType.PARAMETER)
public @interface ClassTag {
    /**
     * Optionally names the type variable to reify, for when the parameter type cannot carry it
     * (e.g. a raw or wildcard {@code Class} token, or a method whose own type variables do not
     * line up with the receiver's). When empty (the default), the type variable is read from the
     * parameter type {@code Class<X>}.
     * <p>
     * Note: {@code for} is a Java reserved word and cannot be an annotation member name, so the
     * override is spelled {@code @ClassTag("K")} (or {@code @ClassTag(value = "K")}). For a method
     * compiled from source in the same unit, the static type checker rejects a name that does not
     * resolve to a type variable in scope, so typos fail at compile time. The check cannot reach an
     * extension method supplied by an already-compiled library (it is never visited as source); such
     * a typo silently disables injection rather than being reported.
     *
     * @return the name of the type variable to reify, or empty to read it from the parameter type
     */
    String value() default "";

    /**
     * Declares that the overload carrying this parameter may <em>preempt</em> a matching
     * token-less overload declared by the same class (or class hierarchy): an existing call that
     * binds the token-less overload is transparently upgraded to this one under static
     * compilation. Because that changes the meaning of existing source, it is off by default;
     * without it the overload is only selected additively (when the call as written matches no
     * method). Consumers retain a veto via
     * {@link org.codehaus.groovy.control.CompilerConfiguration#setClassTagPreemptionDisabled}.
     * By convention, set it on every {@code @ClassTag} parameter of the overload (any one
     * suffices to declare the intent).
     *
     * @return whether this overload may preempt its same-owner token-less sibling
     */
    boolean preempt() default false;
}

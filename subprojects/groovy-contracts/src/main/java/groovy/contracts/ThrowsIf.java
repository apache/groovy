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

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a method's <b>exceptional contract</b>: the method throws the given
 * exception when the given condition holds on entry.
 * <pre>
 * {@code @ThrowsIf}(value = { b == 0 }, exception = ArithmeticException)
 * int divide(int a, int b) { a.intdiv(b) }
 * </pre>
 * <p>
 * This is the exceptional counterpart of {@link Requires}: a precondition says
 * <em>the caller must not do this</em> (violating it is the caller's bug), whereas
 * a {@code @ThrowsIf} arm says <em>this input is handled, by throwing</em> — it is
 * <b>defined behaviour</b>, part of the contract, that callers may rely on (and
 * catch). The condition is a closure over the method's parameters, using the same
 * conventions as {@link Requires}.
 * <p>
 * <b>Weaving.</b> With the default {@code woven = true} the guard-throw is
 * <em>generated</em> at method entry — the general form of the pattern
 * {@code groovy.transform.NullCheck} provides for the null-check special case —
 * so the annotation is the implementation, not a comment about one:
 * <pre>
 * if (b == 0) throw new ArithmeticException(...)   // inserted; message derived from the condition
 * </pre>
 * On a constructor whose first statement is an explicit {@code super(...)} or
 * {@code this(...)} call, the guard is inserted immediately <em>after</em> that
 * call, which must stay first — a language constraint.
 * <p>
 * With {@code woven = false} nothing is generated: the throw already exists.
 * <em>Where</em> it exists is the {@code direct} member — pure information for
 * readers and tools, with no effect on bytecode. {@code direct = true} (the
 * default): a hand-written {@code throw} statement lives in this body — checkable
 * documentation of a guard that is already there. {@code direct = false}: the
 * exception arises from code this method <em>executes</em> — a call, possibly
 * transitively (a third-party library), or a runtime operation — so there is no
 * throw statement in this body to find (and weaving a wrong specification about
 * invoked code would silently change behaviour, which is why such arms are
 * spelled {@code woven = false}). For woven code {@code direct} is implicitly
 * true and the member is ignored — most users never set it; a tool that cannot
 * find the promised throw in the body is the usual prompt to add
 * {@code direct = false}. The attributes are per-arm because real methods mix
 * modes:
 * <pre>
 * {@code @ThrowsIf}(value = { x == null }, exception = NullPointerException)                 // woven for x
 * {@code @ThrowsIf}(value = { y == null }, exception = NullPointerException, woven = false)  // body guards y
 * Object process(Object x, Object y) { ... }
 * </pre>
 * <p>
 * <b>Semantics.</b> Read as an <b>iff</b> by default: the method throws a matching
 * exception <em>exactly when</em> some arm's condition holds — each arm is a
 * <em>must-throw</em> (condition on entry &rArr; the method throws, not returns),
 * and the arm-set as a whole claims <em>only-when</em> (a matching throw &rArr;
 * some arm's condition held). {@code exhaustive = false} disclaims the only-when
 * half for the whole arm-set (a one-directional, JML {@code signals}-style arm):
 * the condition is <em>sufficient</em> for the throw but the set does not claim to
 * list every reason — useful when the full condition is inexpressible (for
 * example, {@code Integer.parseInt}: {@code s == null} is a true sufficient
 * condition, while "malformed or out of range" is not reasonably a parameter
 * closure). Note what neither mode claims: exhaustiveness is (at most) over the
 * <em>conditions for the exception types mentioned</em>, never over exception
 * types themselves — declaring an {@code ArithmeticException} arm says nothing
 * about whether the method can throw anything else. And no claim, however
 * exhaustive, reasons about VM resource conditions: an {@code OutOfMemoryError}
 * or {@code StackOverflowError} is outside contract semantics — the checked
 * wrapper passes any {@link VirtualMachineError} through unjudged, whatever the
 * arm types.
 * <p>
 * <b>Checking.</b> Weaving <em>implements</em> the contract; {@code checked = true}
 * additionally <em>verifies</em> it at runtime, in the same assertion style as
 * {@link Ensures}: on a normal return, no non-woven arm's condition may have held
 * (must-throw), and an escaping exception matching some arm's type must be
 * justified by a matching arm's condition having held on entry (only-when —
 * checked only for {@code exhaustive} arm-sets). A broken implementation raises
 * {@link org.apache.groovy.contracts.ThrowsIfViolation} — never the declared
 * exception, which is defined behaviour delivered at entry; a justified throw
 * always propagates untouched. {@code checked} is <em>set-level</em>: if any arm
 * is {@code checked}, the whole arm-set is checked — every non-woven arm is
 * must-throw checked, and every arm serves as an only-when justifier (a sibling
 * cannot silently opt out). A pleasant corollary:
 * {@code woven = false, direct = false} plus {@code checked = true}
 * runtime-validates a claim about invoked third-party code — a wrong
 * specification is exposed, not silently believed.
 * <p>
 * The annotation is runtime-retained and repeatable, so the exceptional contract
 * is structured metadata available to documentation generators, static analysers,
 * verification tools, and AI coding agents — none of which can reliably consume
 * javadoc prose or discover guarded throw sites by body traversal. Only the
 * must-throw guard of a woven arm and the {@code checked} wrapper affect
 * generated code; {@code exception}, {@code exhaustive} and {@code direct} are
 * otherwise consumed by tools.
 *
 * @since 6.0.0
 * @see Requires
 * @see ThrowsIfConditions
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Incubating
@Repeatable(ThrowsIfConditions.class)
@GroovyASTTransformationClass("org.apache.groovy.contracts.ast.ThrowsIfASTTransformation")
public @interface ThrowsIf {
    /**
     * Returns the closure class that evaluates the throw condition, a boolean
     * expression over the method's parameters.
     *
     * @return the generated closure class backing the condition
     */
    Class value();

    /**
     * The exception type thrown when the condition holds. A woven arm generates
     * {@code new <exception>(String)}, so the type should provide a
     * {@code (String)} constructor (all the standard JDK exceptions do).
     *
     * @return the exception type
     */
    Class<? extends Throwable> exception() default Throwable.class;

    /**
     * {@code true} (default): generate the guard-throw at method entry.
     * {@code false}: the throw already exists (see {@code direct} for where) —
     * nothing is generated.
     *
     * @return whether the guard is generated
     */
    boolean woven() default true;

    /**
     * Information for readers and tools, with no effect on bytecode; ignored
     * (implicitly {@code true}) for woven arms. {@code true} (default): a
     * hand-written {@code throw} statement lives in this body. {@code false}:
     * the exception arises from code this method executes — a call, possibly
     * transitively, or a runtime operation — so there is no throw statement in
     * this body to find. Most users never set this; a verification or analysis
     * tool unable to find the promised throw in the body is the usual prompt.
     *
     * @return whether the body itself contains the throw
     */
    boolean direct() default true;

    /**
     * No effect on generated code by itself. Under {@code checked = true} it
     * gates the escaping-throw check: {@code true} (default) claims the listed
     * conditions are the <em>only</em> reasons a matching exception is thrown,
     * so an escaping match with no condition held is a
     * {@link org.apache.groovy.contracts.ThrowsIfViolation}; {@code false}
     * says the method may throw the same exception for other, unlisted reasons
     * — escaping throws are never judged (and verification tools likewise skip
     * the only-when direction). One {@code false} arm disclaims the check for
     * the whole arm-set. In specification terms: {@code true} is an iff,
     * {@code false} a one-directional (JML {@code signals}-style) sufficient
     * condition — the honest choice when the full condition is unstatable
     * ({@code Integer.parseInt}: {@code s == null} is a true sufficient half;
     * "malformed or out of range" is not a parameter closure).
     *
     * @return whether the arm-set claims to list every reason a matching
     *         exception is thrown
     */
    boolean exhaustive() default true;

    /**
     * {@code true}: verify the contract at runtime in the assertion style of
     * {@link Ensures} — a normal return with a non-woven arm's condition held, or
     * an unjustified escaping throw of a matching type ({@code exhaustive}
     * arm-sets only), raises a
     * {@link org.apache.groovy.contracts.ThrowsIfViolation}. {@code false}
     * (default): no runtime checking beyond any {@code WOVEN} guard.
     *
     * @return whether the contract is runtime-checked
     */
    boolean checked() default false;
}

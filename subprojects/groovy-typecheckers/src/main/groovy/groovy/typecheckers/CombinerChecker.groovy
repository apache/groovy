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
import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

/**
 * A compile-time checker that verifies the combiner passed to an
 * order-independent parallel reduction ({@code sumParallel},
 * {@code injectParallel}) carries the associativity contract those methods
 * silently require &mdash; mirroring the design of {@code PurityChecker}.
 * <p>
 * Associativity is undecidable in general, so this checker is deliberately
 * conservative. It has two modes, selected via the extension option
 * {@code mode}:
 * <pre>
 * // default (lenient): only flag high-confidence problems
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.CombinerChecker')}
 *
 * // strict: additionally require the combiner to be declared @Associative/@Reducer
 * {@code @TypeChecked(extensions = "groovy.typecheckers.CombinerChecker(mode: 'strict')")}
 * </pre>
 * <p>
 * What it does, by combiner shape:
 * <ul>
 *   <li><b>Method reference</b> to a method annotated {@link groovy.transform.Associative}
 *       or {@link groovy.transform.Reducer} &mdash; accepted.</li>
 *   <li><b>Inline closure</b> whose combine expression applies a non-associative
 *       operator ({@code -}, {@code /}, {@code %}, {@code **}) directly to the
 *       two combiner parameters (e.g. <code>{ a, b {@literal ->} a - b }</code>)
 *       &mdash; flagged in <em>any</em> mode (high confidence).</li>
 *   <li><b>{@code injectParallel}</b> seed that contradicts a {@code @Reducer}'s
 *       declared {@code zero()} (both statically constant) &mdash; flagged.</li>
 *   <li>Un-annotated method references and other inline closures &mdash; flagged
 *       only in <b>strict</b> mode (a declared, verifiable contract is required).</li>
 * </ul>
 * The default mode never errors on an ordinary associative inline closure such
 * as <code>{ a, b {@literal ->} a + b }</code>; the single error channel of a
 * type-checking extension makes false-positive aversion essential.
 *
 * @since 6.0.0
 * @see groovy.transform.Associative
 * @see groovy.transform.Reducer
 */
@Incubating
class CombinerChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    /** GDK extension class providing the parallel reductions we guard. */
    private static final String PARALLEL_EXT = 'org.codehaus.groovy.runtime.ParallelCollectionExtensions'

    /** Reductions whose correctness depends on an associative combiner. */
    private static final Set<String> SEEDED_REDUCTIONS = Set.of('injectParallel')
    private static final Set<String> UNSEEDED_REDUCTIONS = Set.of('sumParallel')

    private static final Set<String> COMBINER_ANNOS = Set.of('Associative', 'Reducer')

    /**
     * Non-associative binary operators (also non-commutative). Matched purely
     * by operator token, assuming conventional operator meaning: it does not
     * resolve overloaded operators. Notably, overloading a normally-associative
     * operator (e.g. {@code +}, {@code *}) to be non-associative is poor style
     * and an explicit non-goal — not detected by design. The declaration paths
     * (@Associative/@Reducer, Monoid/Semigroup) remain authoritative.
     */
    private static final Set<String> NON_ASSOCIATIVE_OPS = Set.of('-', '/', '%', '**')

    // Simple type names that *carry* the associativity contract by construction.
    // A Monoid additionally carries an identity; a Semigroup does not. These are
    // matched by simple name (no dependency on Functional Java / Palatable /
    // Purefun — all converge on these names), and are extensible via the
    // 'monoids'/'semigroups' extension options.
    private static final Set<String> DEFAULT_MONOID_TYPES = Set.of('Monoid')
    private static final Set<String> DEFAULT_SEMIGROUP_TYPES = Set.of('Semigroup')

    private boolean strict
    private Set<String> monoidTypes
    private Set<String> semigroupTypes

    @Override
    Object run() {
        strict = (options?.mode as String)?.equalsIgnoreCase('strict')
        monoidTypes = DEFAULT_MONOID_TYPES + parsePipe(options?.monoids as String)
        semigroupTypes = DEFAULT_SEMIGROUP_TYPES + parsePipe(options?.semigroups as String)
        // Mirror PurityChecker's proven architecture: visit method bodies with a
        // CheckingVisitor AIC and do the work there. The afterMethodCall event
        // hook has an unreliable dispatch context, and a primitive-typed
        // makeVisitor(boolean) arg also fails dynamic dispatch in the rewritten
        // DSL context — so makeVisitor is no-arg and reads a field. Spike finding.
        afterVisitMethod { MethodNode mn ->
            mn.code?.visit(makeVisitor())
        }
    }

    private CheckingVisitor makeVisitor() {
        boolean strict = this.strict
        Set<String> monoidTypes = this.monoidTypes
        Set<String> semigroupTypes = this.semigroupTypes
        new CheckingVisitor() {

            /** Owner type of a method-ref combiner, or of the receiver a thin
             *  delegating closure forwards to; null otherwise. Uses getType,
             *  which resolves inside the AIC (cf. PurityChecker). */
            private ClassNode carrierOwner(Expression combiner) {
                if (combiner instanceof MethodPointerExpression) {
                    return safeType(((MethodPointerExpression) combiner).expression)
                }
                if (combiner instanceof ClosureExpression) {
                    MethodCallExpression d = thinDelegate((ClosureExpression) combiner)
                    if (d != null) return safeType(d.objectExpression)
                }
                null
            }

            private MethodNode resolveRefMethod(MethodPointerExpression ref) {
                String rn = ref.methodName?.text
                ClassNode owner = (ref.expression instanceof ClassExpression) ?
                        ((ClassExpression) ref.expression).type : safeType(ref.expression)
                owner = owner?.redirect()
                def os = (rn && owner) ? owner.getMethods(rn) : null
                os ? (os.find { CombinerChecker.hasCombinerAnno(it) } ?: os[0]) : null
            }

            private ClassNode safeType(Expression e) {
                try { getType(e) } catch (ignored) { null }
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                String name = call.methodAsString
                def target = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                String dc = (target instanceof MethodNode) ?
                        ((MethodNode) target).declaringClass?.name : null

                // GDK parallel reductions: name-based (extension methods do not
                // expose a resolvable DIRECT_METHOD_CALL_TARGET, cf. PurityChecker
                // matching by simple name). If a target IS resolvable and belongs
                // to an unrelated user class, skip (avoid hijacking same-named).
                boolean isGdk = (name in SEEDED_REDUCTIONS) || (name in UNSEEDED_REDUCTIONS)
                if (isGdk && dc && dc != PARALLEL_EXT && !dc.startsWith('java.') &&
                        !dc.startsWith('org.codehaus.groovy.runtime')) return

                // JDK stream reduction: java.util.stream.{Stream,IntStream,...}#reduce.
                // Unlike GDK extensions these resolve to a real interface method,
                // so a receiver-type gate works (and is needed — 'reduce' is a
                // generic name). reduce's contract requires associativity even
                // for sequential streams, so flagging is spec-correct regardless
                // of .parallel(). cf. RegexChecker gating on java.util.regex.
                boolean isStreamReduce = name == 'reduce' && dc != null &&
                        dc.startsWith('java.util.stream.')
                if (!isGdk && !isStreamReduce) return

                List<Expression> args = (call.arguments instanceof ArgumentListExpression) ?
                        ((ArgumentListExpression) call.arguments).expressions : []
                if (args.isEmpty()) return

                // GDK injectParallel always carries a seed; sumParallel never.
                // Stream.reduce has an identity iff >= 2 args (reduce(id, acc) /
                // reduce(id, acc, combiner)). The associativity-critical function
                // is always the last argument (the merge BinaryOperator for the
                // 3-arg form; the 2-arg accumulator BiFunction is not analysed).
                boolean seeded = isGdk ? (name in SEEDED_REDUCTIONS) : (args.size() >= 2)

                Expression combiner = args[-1]
                Expression seed = seeded ? args[0] : null

                MethodNode refMethod = (combiner instanceof MethodPointerExpression) ?
                        resolveRefMethod((MethodPointerExpression) combiner) : null
                String carrier = classify(carrierOwner(combiner), monoidTypes, semigroupTypes)
                String msg = diagnose(combiner, seed, name, strict, carrier, seeded, refMethod)
                if (msg) addStaticTypeError(msg, call)
            }
        }
    }

    /** Pure static analysis: returns a diagnostic message, or null if acceptable. */
    private static String diagnose(Expression combiner, Expression seed, String name,
                                   boolean strict, String carrier, boolean seeded,
                                   MethodNode refMethod) {
        // A Monoid/Semigroup instance *is* the associativity contract (same
        // trust level as @Associative — an assertion carrier, not a proof).
        if (carrier == 'SEMIGROUP') {
            if (seeded) {
                return "CombinerChecker: '${name}' is seeded but the combiner is a Semigroup, " +
                        "which carries no identity element. A seeded parallel reduction needs a " +
                        "Monoid (its identity must match the seed)."
            }
            return null // Semigroup + unseeded sumParallel: associativity carried, OK
        }
        if (carrier == 'MONOID') {
            return null // associativity + identity carried by the Monoid
        }
        if (combiner instanceof ClosureExpression) {
            return closureMsg((ClosureExpression) combiner, name, strict)
        }
        if (combiner instanceof MethodPointerExpression) {
            return methodRefMsg((MethodPointerExpression) combiner, refMethod, seed, name, strict)
        }
        return strict ? "CombinerChecker: combiner passed to '${name}' cannot be statically " +
                "verified; use a method annotated @Associative/@Reducer" : null
    }

    // ---- inline closure combiner ----

    private static String closureMsg(ClosureExpression closure, String name, boolean strict) {
        Set<String> params = (closure.parameters ?: []).collect { it.name } as Set<String>

        // High-confidence only: a non-associative operator applied directly to
        // the two combiner parameters (e.g. { a, b -> a - b }). Array holder:
        // an anonymous visitor cannot mutate an enclosing local.
        final boolean[] badOp = [false]
        if (params.size() == 2 && closure.code != null) {
            closure.code.visit(new CodeVisitorSupport() {
                @Override
                void visitBinaryExpression(BinaryExpression be) {
                    super.visitBinaryExpression(be)
                    if (be.operation.text in NON_ASSOCIATIVE_OPS &&
                            isParamRef(be.leftExpression, params) &&
                            isParamRef(be.rightExpression, params)) {
                        badOp[0] = true
                    }
                }
            })
        }

        if (badOp[0]) {
            return "CombinerChecker: combiner passed to '${name}' applies a non-associative " +
                    "operator to its arguments; parallel reduction will be non-deterministic. " +
                    "Use an associative combiner."
        }
        return strict ? "CombinerChecker (strict): inline combiner passed to '${name}' is not " +
                "declared @Associative/@Reducer; extract a named, annotated method." : null
    }

    /**
     * Returns the delegated call iff the closure is a <em>thin</em> delegate:
     * its single result expression is exactly {@code recv.m(p0, p1)} over the
     * two (distinct) combiner parameters. A closure that merely <em>contains</em>
     * such a call (e.g. {@code { a, b -> recv.m(a, b) - a }}) is not thin and
     * must not bypass the non-associative-operator scan.
     */
    private static MethodCallExpression thinDelegate(ClosureExpression closure) {
        List<String> ps = (closure.parameters ?: []).collect { it.name }
        if (ps.size() != 2) return null
        Expression result = unwrapResult(closure.code)
        if (!(result instanceof MethodCallExpression)) return null
        MethodCallExpression mce = (MethodCallExpression) result
        def a = (mce.arguments instanceof ArgumentListExpression) ?
                ((ArgumentListExpression) mce.arguments).expressions : []
        Set<String> set = ps as Set<String>
        if (a.size() == 2 && isParamRef(a[0], set) && isParamRef(a[1], set) &&
                ((VariableExpression) a[0]).name != ((VariableExpression) a[1]).name) {
            return mce
        }
        null
    }

    private static Expression unwrapResult(Statement code) {
        Statement s = code
        if (s instanceof BlockStatement) {
            def st = ((BlockStatement) s).statements
            if (st.size() != 1) return null
            s = st[0]
        }
        if (s instanceof ReturnStatement) return ((ReturnStatement) s).expression
        if (s instanceof ExpressionStatement) return ((ExpressionStatement) s).expression
        null
    }

    private static boolean isParamRef(Expression e, Set<String> params) {
        e instanceof VariableExpression && ((VariableExpression) e).name in params
    }

    // ---- method-reference combiner ----

    // 'resolved' is pre-computed in the AIC (where getType resolves), so this
    // handles both class-qualified (Foo.&bar / Foo::bar) and instance-bound
    // (obj.&bar) references uniformly.
    private static String methodRefMsg(MethodPointerExpression ref, MethodNode resolved,
                                       Expression seed, String name, boolean strict) {
        if (resolved != null && hasCombinerAnno(resolved)) {
            return seedMismatchMsg(resolved, seed, name)
        }
        return strict ? "CombinerChecker (strict): combiner '${ref.methodName?.text}' passed " +
                "to '${name}' is not declared @Associative/@Reducer." : null
    }

    private static String seedMismatchMsg(MethodNode combiner, Expression seed, String name) {
        if (!(seed instanceof ConstantExpression)) return null
        def reducer = combiner.annotations?.find { it.classNode?.nameWithoutPackage == 'Reducer' }
        String zero = reducer?.getMember('zero')?.text
        String seedText = ((ConstantExpression) seed).text
        if (zero != null && !zero.isEmpty() && zero != seedText) {
            return "CombinerChecker: seed '${seedText}' passed to '${name}' does not match the " +
                    "@Reducer(zero='${zero}') of '${combiner.name}'."
        }
        return null
    }

    private static boolean hasCombinerAnno(MethodNode mn) {
        mn?.annotations?.any { it.classNode?.nameWithoutPackage in COMBINER_ANNOS } ?: false
    }

    // ---- Monoid / Semigroup carrier recognition ----

    private static Set<String> parsePipe(String s) {
        s ? (s.split('\\|')*.trim().findAll { it } as Set<String>) : Collections.emptySet()
    }

    /**
     * Classifies the carrier owner type: MONOID (associative + identity),
     * SEMIGROUP (associative only), or null. A type qualifies if its own simple
     * name, or that of any superclass/implemented interface, is in the
     * configured sets. Monoid wins over Semigroup (a Monoid IS-A Semigroup in
     * Functional Java / Palatable / Purefun).
     */
    private static String classify(ClassNode type, Set<String> monoids, Set<String> semigroups) {
        if (type == null) return null
        Set<String> names = collectSimpleNames(type)
        if (names.any { it in monoids }) return 'MONOID'
        if (names.any { it in semigroups }) return 'SEMIGROUP'
        null
    }

    private static Set<String> collectSimpleNames(ClassNode type) {
        Set<String> names = []
        ClassNode c = type?.redirect()
        while (c != null && c.name != 'java.lang.Object') {
            names << c.nameWithoutPackage
            c.interfaces?.each { names << it.nameWithoutPackage }
            try { c.allInterfaces?.each { names << it.nameWithoutPackage } } catch (ignored) { }
            c = c.superClass
        }
        names
    }
}

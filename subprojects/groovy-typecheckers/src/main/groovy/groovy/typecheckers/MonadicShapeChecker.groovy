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
import org.apache.groovy.runtime.MonadicCarrierRegistry
import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Compile-time lint for native monadic chains over the standard carrier
 * allow-list ({@link MonadicCarrierRegistry}).
 * <p>
 * Sister to {@link MonadicChecker}: that one repairs erasure on calls routed
 * through the {@code DO} macro's runtime dispatcher; this one catches shape
 * bugs in <em>hand-written</em> {@code flatMap}/{@code map} (and
 * {@code thenCompose}/{@code thenApply}/etc.) chains that the JDK generics
 * cannot reject:
 * <ul>
 *   <li><b>{@code bind} returning a non-carrier</b> &mdash; e.g.
 *       {@code Optional.flatMap { it + 1 }} where the JDK expects an
 *       {@code Optional}. STC can silently let closures pass this gap.</li>
 *   <li><b>{@code bind} returning a different carrier</b> &mdash; e.g.
 *       {@code Stream.flatMap { Optional.of(it) }}. Almost certainly a bug.</li>
 *   <li><b>{@code map} returning the same carrier</b> &mdash; e.g.
 *       {@code Optional.map { Optional.of(it) }} producing
 *       {@code Optional<Optional<T>>}; usually a missed {@code flatMap} (or
 *       {@code thenCompose}).</li>
 * </ul>
 * Carriers, and the canonical names of their bind/map methods, are read
 * entirely from {@link MonadicCarrierRegistry}; types annotated
 * {@link groovy.transform.Monadic} also participate (matched by simple name,
 * like {@code @Reducer}). Calls whose target is {@code Comprehensions} are
 * skipped &mdash; they are {@link MonadicChecker}'s domain.
 * <p>
 * Two modes, selected via the extension option {@code mode}:
 * <pre>
 * // default (lenient): only flag high-confidence problems
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.MonadicShapeChecker')}
 *
 * // strict: also flag chains whose function return cannot be statically resolved
 * {@code @TypeChecked(extensions = "groovy.typecheckers.MonadicShapeChecker(mode: 'strict')")}
 * </pre>
 *
 * @since 6.0.0
 * @see MonadicCarrierRegistry
 * @see MonadicChecker
 * @see groovy.transform.Monadic
 */
@Incubating
class MonadicShapeChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    /** The {@code DO}-macro dispatcher target; calls here are {@link MonadicChecker}'s. */
    private static final String DISPATCHER = 'org.apache.groovy.runtime.Comprehensions'

    private boolean strict

    @Override
    Object run() {
        strict = (options?.mode as String)?.equalsIgnoreCase('strict')
        // Visit method bodies with a CheckingVisitor — same shape as
        // CombinerChecker/PurityChecker; avoids the spotty dispatch context
        // of the afterMethodCall hook in the rewritten DSL.
        afterVisitMethod { MethodNode mn ->
            mn.code?.visit(makeVisitor())
        }
    }

    private CheckingVisitor makeVisitor() {
        boolean strict = this.strict
        new CheckingVisitor() {

            private ClassNode safeType(Expression e) {
                try { getType(e) } catch (ignored) { null }
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)

                MethodNode target = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                // Skip the DO-macro dispatcher — that's MonadicChecker's territory.
                if (target?.declaringClass?.name == DISPATCHER) return

                String name = call.methodAsString
                if (!name) return

                ClassNode receiverType = safeType(call.objectExpression)
                CarrierInfo carrier = carrierFor(receiverType)
                if (carrier == null) return

                String role
                if (name == carrier.bind) role = 'bind'
                else if (name == carrier.map) role = 'map'
                else return

                List<Expression> args = (call.arguments instanceof ArgumentListExpression) ?
                        ((ArgumentListExpression) call.arguments).expressions : []
                if (args.isEmpty()) return
                Expression fn = args[-1] // bind/map take a single function arg

                ClassNode produced = functionReturnType(fn)
                String msg = diagnose(carrier, role, produced, name, strict)
                if (msg) addStaticTypeError(msg, call)
            }

            /**
             * Best-effort static return-type for the function argument:
             * STC metadata on a closure literal, generics on a method
             * reference's resolved method, or null if neither applies.
             */
            private ClassNode functionReturnType(Expression fn) {
                if (fn instanceof ClosureExpression) {
                    ClassNode inferred = fn.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE)
                    if (inferred != null && inferred != OBJECT_TYPE) return inferred
                    // Fallback: read Closure<R> generics off the closure expression.
                    def t = safeType(fn)
                    def gts = t?.genericsTypes
                    return (gts && gts.length > 0 && gts[0].type) ? gts[0].type : null
                }
                if (fn instanceof MethodPointerExpression) {
                    MethodPointerExpression ref = (MethodPointerExpression) fn
                    String rn = ref.methodName?.text
                    ClassNode owner = (ref.expression instanceof ClassExpression) ?
                            ((ClassExpression) ref.expression).type : safeType(ref.expression)
                    if (rn && owner) {
                        def ms = owner.redirect().getMethods(rn)
                        if (ms) {
                            // prefer the single-arg overload (bind/map shape)
                            MethodNode m = ms.find { it.parameters?.size() == 1 } ?: ms[0]
                            return m?.returnType
                        }
                    }
                    return null
                }
                null // ordinary value/variable — no reliable static handle
            }
        }
    }

    /** Pure static analysis: a diagnostic message, or null if the call is acceptable. */
    private static String diagnose(CarrierInfo carrier, String role,
                                   ClassNode produced, String methodName, boolean strict) {
        boolean knownReturn = produced != null && produced != OBJECT_TYPE
        CarrierInfo returnCarrier = knownReturn ? carrierFor(produced) : null

        if (role == 'bind') {
            if (!knownReturn) {
                return strict ? "MonadicShapeChecker (strict): cannot statically verify that the " +
                        "function passed to '${methodName}' on ${carrier.canonical} returns another " +
                        "${carrier.canonical}." : null
            }
            if (returnCarrier == null) {
                return "MonadicShapeChecker: '${methodName}' on ${carrier.canonical} expects its " +
                        "function to return another ${carrier.canonical}; got ${typeName(produced)}."
            }
            if (returnCarrier.canonical != carrier.canonical) {
                return "MonadicShapeChecker: '${methodName}' on ${carrier.canonical} expects its " +
                        "function to return another ${carrier.canonical}; got ${returnCarrier.canonical} " +
                        "(crossing carrier types is almost certainly a bug)."
            }
            return null
        }
        // role == 'map'
        if (!knownReturn) {
            return strict ? "MonadicShapeChecker (strict): cannot statically verify that the " +
                    "function passed to '${methodName}' on ${carrier.canonical} returns a plain " +
                    "value (and not another ${carrier.canonical})." : null
        }
        if (returnCarrier != null && returnCarrier.canonical == carrier.canonical) {
            return "MonadicShapeChecker: '${methodName}' on ${carrier.canonical} returns its " +
                    "function's result wrapped, producing ${carrier.canonical}<${carrier.canonical}<...>>; " +
                    "did you mean '${carrier.bind}'?"
        }
        return null
    }

    private static String typeName(ClassNode cn) {
        cn == null ? '<unknown>' : cn.toString(false)
    }

    // ---- carrier identification ----

    /** Carrier info: canonical name (for same-carrier comparison) and method names. */
    private static class CarrierInfo {
        final String canonical
        final String bind
        final String map
        CarrierInfo(String canonical, String bind, String map) {
            this.canonical = canonical
            this.bind = bind
            this.map = map
        }
    }

    /** Carrier info for the given type, or {@code null} if it is not a known carrier. */
    private static CarrierInfo carrierFor(ClassNode cn) {
        if (cn == null) return null
        ClassNode bare = cn.redirect() ?: cn

        // 1. Class-keyed allow-list (assignability)
        for (e in MonadicCarrierRegistry.entries()) {
            ClassNode t = make(e.carrier())
            if (assignableTo(bare, t)) {
                return new CarrierInfo(t.name, e.bind(), e.map())
            }
        }
        // 2. Name-keyed allow-list (hierarchy walk by FQ name; no library dependency)
        for (e in MonadicCarrierRegistry.namedEntries()) {
            if (hasInHierarchyByName(bare, e.carrierName())) {
                return new CarrierInfo(e.carrierName(), e.bind(), e.map())
            }
        }
        // 3. @Monadic (matched by simple name, walking the hierarchy)
        for (ClassNode c = bare; c != null && c.name != 'java.lang.Object'; c = c.superClass) {
            AnnotationNode ann = c.annotations?.find { it.classNode?.nameWithoutPackage == 'Monadic' }
            if (ann != null) {
                String bind = readStringMember(ann, 'bind') ?: 'flatMap'
                String map = readStringMember(ann, 'map') ?: 'map'
                return new CarrierInfo(c.name, bind, map)
            }
        }
        null
    }

    private static String readStringMember(AnnotationNode ann, String member) {
        Expression e = ann.getMember(member)
        if (e == null) return null
        String s = e.text
        s.isEmpty() ? null : s
    }

    private static boolean assignableTo(ClassNode cn, ClassNode t) {
        cn == t || cn.isDerivedFrom(t) || cn.implementsInterface(t)
    }

    private static boolean hasInHierarchyByName(ClassNode cn, String fq) {
        for (ClassNode c = cn; c != null && c.name != 'java.lang.Object'; c = c.superClass) {
            if (c.name == fq) return true
            ClassNode[] ifaces = c.interfaces
            if (ifaces != null) {
                for (i in ifaces) {
                    if (i.name == fq) return true
                    if (hasInHierarchyByName(i, fq)) return true
                }
            }
        }
        false
    }
}

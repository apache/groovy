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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.apache.groovy.runtime.MonadicCarrierRegistry
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafeWithGenerics
import static org.objectweb.asm.Opcodes.ACC_BRIDGE

/**
 * Teaches {@code @CompileStatic}/{@code @TypeChecked} about the {@code DO} macro's
 * desugared output: calls to {@code org.apache.groovy.runtime.Comprehensions.bind}
 * and {@code .map}, declared {@code (Object, Closure):Object}.
 *
 * Three jobs:
 * <ul>
 *   <li><b>Enforce receiver shape</b>: the carrier must participate (allow-list,
 *       structural {@code flatMap}/{@code map}, or {@code @Monadic}); otherwise a
 *       precise compile error naming the type and the missing shape.</li>
 *   <li><b>Enforce closure-return shape</b> (trusted carriers only &mdash; registry
 *       or {@code @Monadic}, not structural): {@code bind}'s closure must yield the
 *       <em>same</em> carrier (catches a bare body or a cross-carrier body inside
 *       {@code DO}, which the erased dispatcher signature otherwise lets through);
 *       {@code map}'s closure must <em>not</em> yield the same carrier (the
 *       {@code M<M<T>>} foot-gun for hand-written {@code Comprehensions.map}).</li>
 *   <li><b>Assist inference</b>: type the generator closure's parameter as the
 *       carrier's element type (so the body type-checks), and restore the
 *       comprehension's result type (so {@code .get()}/nesting type-check) instead
 *       of the erased {@code Object} the dispatcher signature would yield.</li>
 * </ul>
 *
 * Closure-parameter typing works by pre-setting {@code CLOSURE_ARGUMENTS} on the
 * closure node, which {@code StaticTypeCheckingVisitor.getTypeFromClosureArguments}
 * consults by parameter name &mdash; independent of the {@code Closure<?>} parameter
 * not being a SAM type.
 *
 * Activate with {@code @CompileStatic(extensions='groovy.typecheckers.MonadicChecker')}.
 */
class MonadicChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    private static final String DISPATCHER = 'org.apache.groovy.runtime.Comprehensions'

    @Override
    Object run() {
        // Fires after method selection (carrier argument already typed) but before
        // the generator closure body is visited: the window to type the closure param.
        onMethodSelection { expr, MethodNode target ->
            if (!isDispatcherCall(expr, target)) return
            MethodCallExpression call = (MethodCallExpression) expr
            def args = call.arguments.expressions
            def carrierType = safeType(args[0])
            String role = call.methodAsString

            if (carrierType == null || !participates(carrierType)) {
                addStaticTypeError(
                    "Type ${typeName(carrierType)} does not participate in monadic comprehensions (DO): " +
                    "no ${role == 'bind' ? "bind (flatMap-shaped)" : 'map'} method " +
                    "(not in the standard carrier allow-list, has no structural " +
                    "'${role == 'bind' ? 'flatMap' : 'map'}' method, and is not annotated @Monadic)",
                    args[0])
                return
            }

            def closure = args.find { it instanceof ClosureExpression } as ClosureExpression
            if (closure != null) {
                ClassNode elem = elementType(carrierType)
                closure.putNodeMetaData(StaticTypesMarker.CLOSURE_ARGUMENTS, [elem] as ClassNode[])
            }
        }

        // The dispatcher returns erased Object; restore the comprehension's real type.
        afterMethodCall { call ->
            if (!(call instanceof MethodCallExpression)) return
            MethodNode target = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
            if (!isDispatcherCall(call, target)) return
            def args = call.arguments.expressions
            def carrierType = safeType(args[0])
            if (carrierType == null) return
            def closure = args.find { it instanceof ClosureExpression } as ClosureExpression
            ClassNode produced = closureReturnType(closure)

            // Closure-return shape check (trusted carriers only). The dispatcher
            // signature is (Object, Closure):Object — STC cannot see that the
            // body must yield the same carrier (bind) or a non-carrier (map);
            // restore the contract here. Skipped for structural-only carriers
            // (intentionally permissive, like participates()).
            //
            // Anchor at the closure (the actual offender) when present; the DO
            // macro propagates source positions onto its synthetic lambda, so
            // STC's addStaticTypeError (which drops positionless nodes) will
            // surface this. Fall through to args[0] for the hand-written
            // Comprehensions.bind/map shape where the closure may not be a
            // literal at this argument slot.
            String role = call.methodAsString
            String shape = shapeMsg(role, carrierType, produced)
            if (shape) addStaticTypeError(shape, closure ?: args[0])

            ClassNode result
            if (role == 'bind') {
                // closure yields M<B>; bind yields the same carrier
                result = produced ?: carrierType
            } else {
                // map: closure yields B; result is M<B>
                ClassNode b = produced ?: OBJECT_TYPE
                result = makeClassSafeWithGenerics(carrierType.plainNodeReference, new GenericsType(b))
            }
            storeType(call, result)
        }
    }

    /**
     * Diagnostic for the dispatcher closure-return contract, or null if acceptable.
     * Tolerates unknown returns (null/Object); only flags when the carrier mismatch
     * is statically demonstrable and the receiver is a trusted (registry- or
     * {@code @Monadic}-keyed) carrier.
     */
    private String shapeMsg(String role, ClassNode receiver, ClassNode produced) {
        if (produced == null || produced == OBJECT_TYPE) return null
        String recv = trustedCarrierName(receiver)
        if (recv == null) return null
        String ret = trustedCarrierName(produced)
        if (role == 'bind') {
            if (ret == null) {
                return "Closure passed to Comprehensions.bind on ${recv} must yield ${recv}; " +
                    "got ${typeName(produced)} (not a carrier). In a DO comprehension, the body " +
                    "must produce the same carrier (e.g. ${recv}.of(...))."
            }
            if (ret != recv) {
                return "Closure passed to Comprehensions.bind on ${recv} must yield ${recv}; " +
                    "got ${ret}. Mixing carriers in a comprehension is not supported."
            }
            return null
        }
        // role == 'map'
        if (ret == recv) {
            return "Closure passed to Comprehensions.map on ${recv} returns a ${recv}, " +
                "producing ${recv}<${recv}<...>>; use Comprehensions.bind instead."
        }
        null
    }

    /**
     * The canonical carrier name &mdash; the key for same-carrier comparison &mdash;
     * for the given type, restricted to <em>trusted</em> participation paths
     * (registry allow-list, {@code @Monadic}). Returns {@code null} for
     * structural-only or non-carrier types; structural participation is
     * intentionally permissive and not asserted against.
     */
    private String trustedCarrierName(ClassNode cn) {
        if (cn == null) return null
        ClassNode bare = cn.redirect() ?: cn
        for (e in MonadicCarrierRegistry.entries()) {
            if (assignableTo(bare, make(e.carrier()))) return make(e.carrier()).name
        }
        for (e in MonadicCarrierRegistry.namedEntries()) {
            if (assignableTo(bare, make(e.carrierName()))) return e.carrierName()
        }
        // @Monadic walks super + interfaces, mirroring the runtime
        ClassNode mon = classWithMonadic(bare)
        mon?.name
    }

    private boolean isDispatcherCall(expr, MethodNode target) {
        expr instanceof MethodCallExpression &&
            expr.methodAsString in ['bind', 'map'] &&
            target?.declaringClass?.name == DISPATCHER
    }

    private ClassNode safeType(expr) {
        try { getType(expr) } catch (ignored) { null }
    }

    private static String typeName(ClassNode cn) {
        cn == null ? '<unknown>' : cn.toString(false)
    }

    private boolean participates(ClassNode cn) {
        ClassNode bare = cn.redirect() ?: cn
        // 1. standard allow-list (shared with the runtime dispatcher), Class- and name-keyed
        if (MonadicCarrierRegistry.entries().any { assignableTo(bare, make(it.carrier())) }) return true
        if (MonadicCarrierRegistry.namedEntries().any { assignableTo(bare, make(it.carrierName())) }) return true
        // 2. structural (flatMap covers bind; map covers the map role); arity-1 only
        // — aligns with the runtime dispatcher's findSingleArgMethod
        if (hasSingleArgMethod(bare, 'flatMap') || hasSingleArgMethod(bare, 'map')) return true
        // 3. @Monadic opt-in (matched by simple name, like @Reducer/@Associative);
        // walk super + interfaces, mirroring the runtime dispatcher
        return classWithMonadic(bare) != null
    }

    private boolean assignableTo(ClassNode cn, ClassNode t) {
        cn == t || cn.isDerivedFrom(t) || cn.implementsInterface(t)
    }

    /**
     * True iff any class in the type's superclass-then-interfaces walk declares a
     * single-argument, non-bridge, non-synthetic method with the given name. The
     * arity/bridge/synthetic filter aligns the static check with the runtime
     * dispatcher's {@code findSingleArgMethod}: without it, a 2-arg
     * {@code flatMap(state, fn)} would pass participation here yet fail at runtime.
     */
    private boolean hasSingleArgMethod(ClassNode cn, String name) {
        for (ClassNode c = cn; c != null && c != OBJECT_TYPE; c = c.superClass) {
            if (hasSingleArgIn(c, name)) return true
            if (c.interfaces?.any { hasSingleArgIn(it, name) }) return true
        }
        false
    }

    private static boolean hasSingleArgIn(ClassNode cn, String name) {
        cn.getMethods(name)?.any { isSingleArgUserMethod(it) } ?: false
    }

    private static boolean isSingleArgUserMethod(MethodNode m) {
        m.parameters?.length == 1 && !m.isSynthetic() && (m.modifiers & ACC_BRIDGE) == 0
    }

    /**
     * Walks superclasses and their direct interfaces looking for a
     * {@code @Monadic} annotation (simple-name match, in the manner of
     * {@code @Reducer}/{@code @Associative}). Returns the {@code ClassNode}
     * that carries the annotation, or {@code null} if none. Mirrors the
     * runtime dispatcher's {@code monadicMethodName} walk.
     */
    private static ClassNode classWithMonadic(ClassNode start) {
        for (ClassNode c = start; c != null && c.name != 'java.lang.Object'; c = c.superClass) {
            if (hasMonadicAnno(c)) return c
            ClassNode[] ifaces = c.interfaces
            if (ifaces != null) {
                for (ClassNode i : ifaces) {
                    if (hasMonadicAnno(i)) return i
                }
            }
        }
        null
    }

    private static boolean hasMonadicAnno(ClassNode cn) {
        cn.annotations?.any { it.classNode?.nameWithoutPackage == 'Monadic' } ?: false
    }

    private ClassNode elementType(ClassNode carrier) {
        def gts = carrier?.genericsTypes
        (gts && gts.length > 0 && gts[0].type) ? gts[0].type : OBJECT_TYPE
    }

    private ClassNode closureReturnType(ClosureExpression closure) {
        if (closure == null) return null
        // STC writes the inferred body-return type as metadata on the closure
        // (not always reflected as Closure<R> generics on the closure's own
        // type, especially for bare-expression bodies); consult both.
        ClassNode inferred = closure.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE)
        if (inferred != null) return inferred
        def t = safeType(closure)
        def gts = t?.genericsTypes
        (gts && gts.length > 0 && gts[0].type) ? gts[0].type : null
    }
}

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
package org.codehaus.groovy.vmplugin.v8;

import groovy.lang.MetaClass;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * GEP-15: resolver for dynamic compound-assignment operators
 * ({@code +=}, {@code -=}, ...).
 *
 * <p>This class holds only the GEP-15-specific <em>policy</em>; the call-site
 * <em>lifecycle</em> is owned by {@link IndyInterface}. A compound-assignment
 * {@code op=} is emitted as an {@code invokedynamic} to
 * {@link IndyInterface#bootstrap} with call type
 * {@link IndyInterface.CallType#COMPOUND_ASSIGN}, so it rides the same boot
 * handle, per-receiver-class inline cache, monomorphic-promotion and
 * deopt-storm protection as a normal method call. {@link IndyInterface#fallback}
 * routes resolution here via {@link #resolve}.
 *
 * <p>What remains GEP-15-specific:
 * <ul>
 *   <li>the two operator names ({@code assignName}/{@code baseName}) packed into
 *       the bootstrap {@code name} and unpacked here;</li>
 *   <li>a {@code respondsTo} probe to pick {@code *Assign} vs base — run only on
 *       a cache miss;</li>
 *   <li>the in-place return-receiver composition for the assign branch;</li>
 *   <li>a {@code (receiver class, arg class)} guard (arg class is part of the
 *       key so overloads stay correct), under the shared MOP switch point;</li>
 *   <li>a generic fall back to {@link ScriptBytecodeAdapter#compoundAssign} for
 *       null/unresolved receivers.</li>
 * </ul>
 *
 * <p>The actual invocation is built by {@link Selector#selectInvokeHandle}, so
 * selection/coercion/vargs/category/exception handling match a normal call.
 *
 * <p>WARNING: internal, indy-only. Not for use outside this package.
 *
 * @since 6.0.0
 */
public final class IndyCompoundAssign {

    /** Separator packing {@code assignName} and {@code baseName} into one bootstrap constant (NUL cannot appear in a JVM method name, even a Groovy quoted one). */
    public static final char NAME_SEPARATOR = '\u0000';

    private static final MethodHandle GUARD;          // (Class,Class,Object,Object) -> boolean
    private static final MethodHandle COMPOUND_ASSIGN; // (Object,Object,String,String) -> Object

    /** (Object result, Object receiver, Object arg) -> receiver; folded over the assign invoke. */
    private static final MethodHandle RETURN_RECEIVER;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            GUARD = l.findStatic(IndyCompoundAssign.class, "guard",
                    MethodType.methodType(boolean.class, Class.class, Class.class, Object.class, Object.class));
            COMPOUND_ASSIGN = l.findStatic(ScriptBytecodeAdapter.class, "compoundAssign",
                    MethodType.methodType(Object.class, Object.class, Object.class, String.class, String.class));

            MethodHandle pick = MethodHandles.identity(Object.class);     // (receiver) -> receiver
            pick = MethodHandles.dropArguments(pick, 1, Object.class);    // (receiver, arg) -> receiver
            RETURN_RECEIVER = MethodHandles.dropArguments(pick, 0, Object.class); // (result, receiver, arg) -> receiver
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        }
    }

    private IndyCompoundAssign() {
    }

    /**
     * Packs the two operator names into a single bootstrap {@code name} constant.
     * Called from code generation.
     */
    public static String packNames(final String assignName, final String baseName) {
        return assignName + NAME_SEPARATOR + baseName;
    }

    /**
     * Resolves the invocation for one receiver/arg shape and returns a
     * {@link MethodHandleWrapper} for {@link IndyInterface}'s inline cache. The
     * wrapper's target handle is {@code (Object receiver, Object arg) -> Object},
     * guarded on the receiver/arg classes and the shared MOP switch point with
     * the call site's fallback (re-resolve) path as the else-branch.
     *
     * @param callSite the compound-assignment call site (type {@code (Object,Object)->Object})
     * @param sender the sending class
     * @param packedNames {@code assignName} and {@code baseName} joined by {@link #NAME_SEPARATOR}
     * @param arguments the runtime arguments: {@code [receiver, arg]}
     */
    public static MethodHandleWrapper resolve(final CacheableCallSite callSite, final Class<?> sender,
                                              final String packedNames, final Object[] arguments) {
        int sep = packedNames.indexOf(NAME_SEPARATOR);
        if (sep < 0) throw new GroovyBugError("compound-assign bootstrap name not packed with NAME_SEPARATOR: " + packedNames);
        String assignName = packedNames.substring(0, sep);
        String baseName = packedNames.substring(sep + 1);

        Object receiver = arguments[0];
        Object arg = arguments[1];

        if (receiver == null) return genericWrapper(assignName, baseName); // legacy helper handles null receiver

        MetaClass mc = InvokerHelper.getMetaClass(receiver);
        boolean useAssign;
        String name;
        if (!mc.respondsTo(receiver, assignName, new Object[]{arg}).isEmpty()) {
            useAssign = true;
            name = assignName;
        } else if (!mc.respondsTo(receiver, baseName, new Object[]{arg}).isEmpty()) {
            useAssign = false;
            name = baseName;
        } else {
            return genericWrapper(assignName, baseName); // neither responds: legacy helper raises MissingMethodException
        }

        MethodHandle invoke = Selector.selectInvokeHandle(callSite, sender, name, new Object[]{receiver, arg});
        if (useAssign) invoke = MethodHandles.foldArguments(RETURN_RECEIVER, invoke);

        // Guard on (receiver class, arg class); else-branch re-resolves via the
        // call site's fallback (select) path, overwriting the inline cache entry.
        MethodHandle elseTarget = callSite.getFallbackTarget();
        Class<?> rc = receiver.getClass();
        Class<?> ac = (arg == null) ? null : arg.getClass();
        MethodHandle test = MethodHandles.insertArguments(GUARD, 0, rc, ac);
        MethodHandle guarded = MethodHandles.guardWithTest(test, invoke, elseTarget);
        // a metaclass change invalidates the site, exactly as for a normal indy call.
        guarded = IndyInterface.switchPoint.guardWithTest(guarded, elseTarget);

        // Per-instance metaclasses make a class-keyed cache unsound, so mark such
        // shapes uncacheable (the wrapper is still used for the current call) —
        // mirrors Selector's own cacheability rule.
        boolean cacheable = !ClassInfo.getClassInfo(rc).hasPerInstanceMetaClasses();
        return wrap(guarded, cacheable);
    }

    /** Uncacheable wrapper delegating to the legacy helper (identical semantics for the declined shape). */
    private static MethodHandleWrapper genericWrapper(final String assignName, final String baseName) {
        MethodHandle generic = MethodHandles.insertArguments(COMPOUND_ASSIGN, 2, assignName, baseName);
        return wrap(generic, false);
    }

    /** Wraps a {@code (Object,Object)->Object} target into the cached + relink form IndyInterface expects. */
    private static MethodHandleWrapper wrap(final MethodHandle target, final boolean cacheable) {
        MethodHandle cached = target.asSpreader(Object[].class, 2).asType(MethodType.methodType(Object.class, Object[].class));
        return new MethodHandleWrapper(cached, target, null, cacheable);
    }

    /** Guard: receiver and argument runtime classes both match the cached shape. */
    @SuppressWarnings("unused")
    public static boolean guard(final Class<?> rc, final Class<?> ac, final Object receiver, final Object arg) {
        if (receiver == null || receiver.getClass() != rc) return false;
        return (ac == null) ? (arg == null) : (arg != null && arg.getClass() == ac);
    }
}

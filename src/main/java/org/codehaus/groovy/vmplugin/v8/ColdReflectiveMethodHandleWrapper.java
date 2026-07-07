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

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Experimental reflective cold-tier wrapper, guarded by the
 * {@code groovy.indy.cold.reflection} system property.
 * <p>
 * The regular cold tier builds a fully guarded MethodHandle chain on the
 * first invocation of every receiver shape — paying the one-time LambdaForm
 * creation cost for a MethodType shape determined by the call site's arity
 * and primitive pattern. This wrapper instead carries only the selected
 * {@link MetaMethod} plus the data needed to re-validate the selection in
 * plain Java; invocation happens reflectively via
 * {@link IndyInterface#invokeColdReflective}. Its bound invocation handle has
 * the same {@code (Object[])Object} shape for every call site, so the cold
 * tier spins no per-shape LambdaForms. The full guarded chain is built only
 * at promotion time (see {@code IndyInterface.fromCacheHandle}), leaving the
 * hot path unchanged.
 * <p>
 * The plain-Java validity checks in {@link #isValidFor} mirror the guards the
 * full chain would install: switch-point freshness (which also covers
 * category enter/leave, as those invalidate the switch point), exact argument
 * classes (at least as strict as the chain's same-class guards), and
 * metaclass identity for {@code GroovyObject} receivers.
 */
class ColdReflectiveMethodHandleWrapper extends MethodHandleWrapper {

    final CacheableCallSite callSite;
    final Class<?> sender;
    final String methodName;
    final int callID;
    final Boolean safeNavigation;
    final Boolean thisCall;
    final Boolean spreadCall;
    private final MetaClass selectionMetaClass;
    private final SwitchPoint validity;
    private final Class<?>[] argClasses;
    private final MethodHandle reflectiveHandle;
    /**
     * Cumulative reflective invocations, independent of the PIC's
     * consecutive-hit counter (which resets whenever receivers alternate, so
     * polymorphic sites would otherwise stay on the reflective tier forever).
     * Racy increments only delay promotion slightly, which is harmless.
     */
    private int reflectiveHits;

    private ColdReflectiveMethodHandleWrapper(MetaMethod method, CacheableCallSite callSite, Class<?> sender,
            String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall,
            MetaClass selectionMetaClass, SwitchPoint validity, Class<?>[] argClasses) {
        super(null, null, method, true);
        this.callSite = callSite;
        this.sender = sender;
        this.methodName = methodName;
        this.callID = callID;
        this.safeNavigation = safeNavigation;
        this.thisCall = thisCall;
        this.spreadCall = spreadCall;
        this.selectionMetaClass = selectionMetaClass;
        this.validity = validity;
        this.argClasses = argClasses;
        this.reflectiveHandle = IndyInterface.COLD_REFLECTIVE_INVOKER.bindTo(this);
    }

    @Override
    public MethodHandle getCachedMethodHandle() {
        return reflectiveHandle;
    }

    /**
     * Counts a reflective invocation.
     *
     * @return the cumulative reflective invocation count
     */
    int incrementReflectiveHits() {
        return ++reflectiveHits;
    }

    /**
     * Plain-Java equivalent of the guards the full handle chain would install.
     *
     * @param arguments receiver-first invocation arguments
     * @return {@code true} if the cached selection is still valid for them
     */
    boolean isValidFor(Object[] arguments) {
        if (validity.hasBeenInvalidated()) return false;
        Class<?>[] classes = argClasses;
        if (arguments.length != classes.length) return false;
        for (int i = 0; i < classes.length; i++) {
            Object a = arguments[i];
            if (a == null ? classes[i] != null : a.getClass() != classes[i]) return false;
        }
        Object receiver = arguments[0];
        return !(receiver instanceof GroovyObject go) || go.getMetaClass() == selectionMetaClass;
    }

    /**
     * Attempts a reflective cold-tier selection for a plain method call.
     * "Plain" means: real receiver (non-null, not a {@code Class}, not
     * {@code GroovyInterceptable}), no spread/safe-null semantics, cacheable
     * selection resolving to a public, non-static, non-category
     * {@link CachedMethod} on a public class that is not caller-sensitive.
     * Anything else returns {@code null} and takes the full handle path.
     *
     * @return the reflective wrapper, or {@code null} if unsupported
     */
    static MethodHandleWrapper tryBuild(Selector selector, CacheableCallSite callSite, Class<?> sender,
            String methodName, int callID, Boolean safeNavigation, Boolean thisCall, Boolean spreadCall,
            Object[] arguments) {
        MetaMethod selected = selector.selectForColdReflection();
        if (selected == null || selected instanceof GroovyCategorySupport.CategoryMethod) return null;
        MetaClass mc = selector.getSelectionMetaClass();
        if (mc == null || !selector.cache) return null;
        MetaMethod transformed = VMPluginFactory.getPlugin().transformMetaMethod(mc, selected, sender);
        if (!(transformed instanceof CachedMethod cm)) return null;
        int modifiers = cm.getModifiers();
        if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) return null;
        if (!Modifier.isPublic(cm.getDeclaringClass().getModifiers())) return null;
        if (isCallerSensitive(cm, arguments[0].getClass())) return null;
        Class<?>[] argClasses = new Class[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            Object a = arguments[i];
            if (a instanceof Wrapper) return null; // cast markers need the full coercion path
            argClasses[i] = (a == null) ? null : a.getClass();
        }
        return new ColdReflectiveMethodHandleWrapper(cm, callSite, sender, methodName, callID,
                safeNavigation, thisCall, spreadCall, mc, IndyInterface.switchPoint, argClasses);
    }

    /**
     * Reflective invocation would report the wrong caller for caller-sensitive
     * methods (and promotion would then change the observed caller mid-run),
     * so such targets stay on the full handle path. The per-method logic —
     * annotation probe plus the declaring-class cases like serialization —
     * lives on {@link CachedMethod#isCallerSensitive()}, computed once per
     * method rather than per call site. The only receiver-dependent concern
     * handled here is virtual dispatch through an <em>abstract</em> selection:
     * the metaclass can select e.g. {@code ObjectInput.readObject()} for a
     * call on an {@code ObjectInputStream}, and the sensitivity then belongs
     * to the receiver's implementation. Non-abstract interface methods
     * (defaults) carry their own annotations, so they are probed directly.
     * When a probe cannot decide, the method is treated as sensitive.
     */
    private static boolean isCallerSensitive(CachedMethod cm, Class<?> receiverClass) {
        if (cm.isCallerSensitive()) return true;
        Method method = cm.getCachedMethod();
        if (Modifier.isAbstract(method.getModifiers())) {
            // the metaclass can select an abstract method (e.g. it picks
            // ObjectInput.readObject() for a call on an ObjectInputStream);
            // virtual dispatch then lands on the receiver's implementation,
            // which may be caller-sensitive even though the abstract method
            // cannot carry the annotation. Non-abstract interface methods
            // (defaults) carry their own annotations and need no resolution.
            try {
                Method implementation = receiverClass.getMethod(method.getName(), method.getParameterTypes());
                CachedMethod implCached = CachedMethod.find(implementation);
                if (implCached != null) return implCached.isCallerSensitive();
            } catch (Throwable ignore) {
            }
            return true; // could not resolve the implementation: stay safe
        }
        return false;
    }
}

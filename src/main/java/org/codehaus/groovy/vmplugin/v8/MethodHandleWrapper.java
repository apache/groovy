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

import groovy.lang.MetaMethod;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrap method handles
 *
 * @since 3.0.0
 */
class MethodHandleWrapper {
    private final MethodHandle cachedMethodHandle;
    private final MethodHandle targetMethodHandle;
    private final MetaMethod method;
    private final boolean canSetTarget;
    private final CallSiteTargetRelinkPolicy callSiteTargetRelinkPolicy;
    private final AtomicLong latestHitCount = new AtomicLong(0);

    /**
     * Creates a wrapper for the cached and relink targets of a meta method.
     *
     * @param cachedMethodHandle the cached invocation handle
     * @param targetMethodHandle the relink target handle
     * @param method the associated meta method
     * @param canSetTarget whether the call site target may be updated to this handle
     */
    public MethodHandleWrapper(MethodHandle cachedMethodHandle, MethodHandle targetMethodHandle, MetaMethod method, boolean canSetTarget) {
        this(cachedMethodHandle, targetMethodHandle, method, canSetTarget, CallSiteTargetRelinkPolicy.NEVER);
    }

    private MethodHandleWrapper(MethodHandle cachedMethodHandle, MethodHandle targetMethodHandle, MetaMethod method, boolean canSetTarget, CallSiteTargetRelinkPolicy callSiteTargetRelinkPolicy) {
        this.cachedMethodHandle = cachedMethodHandle;
        this.targetMethodHandle = targetMethodHandle;
        this.method = method;
        this.canSetTarget = canSetTarget;
        this.callSiteTargetRelinkPolicy = callSiteTargetRelinkPolicy;
    }

    /**
     * Creates a wrapper and precomputes the early call-site relink policy for the supplied receiver type.
     *
     * @param cachedMethodHandle the cached invocation handle
     * @param targetMethodHandle the relink target handle
     * @param method the associated meta method
     * @param canSetTarget whether the call site target may be updated to this handle
     * @param receiverType the declared call-site receiver type
     * @return the configured wrapper
     */
    static MethodHandleWrapper create(MethodHandle cachedMethodHandle, MethodHandle targetMethodHandle, MetaMethod method, boolean canSetTarget, Class<?> receiverType) {
        return new MethodHandleWrapper(
            cachedMethodHandle,
            targetMethodHandle,
            method,
            canSetTarget,
            resolveCallSiteTargetRelinkPolicy(method, receiverType)
        );
    }

    /**
     * Returns the cached invocation handle.
     *
     * @return the cached invocation handle
     */
    public MethodHandle getCachedMethodHandle() {
        return cachedMethodHandle;
    }

    /**
     * Returns the handle used when the call site target is updated.
     *
     * @return the relink target handle
     */
    public MethodHandle getTargetMethodHandle() {
        return targetMethodHandle;
    }

    /**
     * Returns the meta method associated with this wrapper.
     *
     * @return the wrapped meta method
     */
    public MetaMethod getMethod() {
        return method;
    }

    /**
     * Indicates whether this wrapper may be installed as the call-site target.
     *
     * @return {@code true} if the target may be updated
     */
    public boolean isCanSetTarget() {
        return canSetTarget;
    }

    /**
     * Indicates whether the call site can be relinked on the first cache hit.
     *
     * @return {@code true} when the target can be installed immediately
     */
    boolean shouldSetCallSiteTargetImmediately() {
        return callSiteTargetRelinkPolicy == CallSiteTargetRelinkPolicy.IMMEDIATE;
    }

    /**
     * Indicates whether the call site can be relinked after observing a repeated exact-final receiver hit.
     *
     * @return {@code true} when a repeated hit may trigger relinking
     */
    boolean shouldSetCallSiteTargetOnRepeatedHit() {
        return callSiteTargetRelinkPolicy == CallSiteTargetRelinkPolicy.AFTER_REPEATED_HIT;
    }

    /**
     * Increments the hit count for the latest inline-cache hit.
     *
     * @return the updated hit count
     */
    public long incrementLatestHitCount() {
        return latestHitCount.incrementAndGet();
    }

    /**
     * Resets the latest-hit counter.
     */
    public void resetLatestHitCount() {
        latestHitCount.set(0);
    }

    /**
     * Returns the latest-hit counter value.
     *
     * @return the current latest-hit counter
     */
    public long getLatestHitCount() {
        return latestHitCount.get();
    }

    /**
     * Returns the sentinel wrapper used when no cacheable handle is available.
     *
     * @return the null sentinel wrapper
     */
    public static MethodHandleWrapper getNullMethodHandleWrapper() {
        return NullMethodHandleWrapper.INSTANCE;
    }

    private static CallSiteTargetRelinkPolicy resolveCallSiteTargetRelinkPolicy(MetaMethod method, Class<?> receiverType) {
        if (method == null) return CallSiteTargetRelinkPolicy.NEVER;

        int modifiers = method.getModifiers();
        if (Modifier.isPrivate(modifiers)) return CallSiteTargetRelinkPolicy.IMMEDIATE;
        if (Modifier.isStatic(modifiers)) {
            return receiverType == Class.class
                ? CallSiteTargetRelinkPolicy.IMMEDIATE
                : CallSiteTargetRelinkPolicy.NEVER;
        }
        if (receiverType == Class.class) return CallSiteTargetRelinkPolicy.NEVER;

        return Modifier.isFinal(receiverType.getModifiers())
            ? CallSiteTargetRelinkPolicy.AFTER_REPEATED_HIT
            : CallSiteTargetRelinkPolicy.NEVER;
    }

    private enum CallSiteTargetRelinkPolicy {
        NEVER,
        IMMEDIATE,
        AFTER_REPEATED_HIT
    }

    private static class NullMethodHandleWrapper extends MethodHandleWrapper {
        /**
         * Shared sentinel wrapper representing the absence of a reusable method handle.
         */
        public static final NullMethodHandleWrapper INSTANCE = new NullMethodHandleWrapper();

        private NullMethodHandleWrapper() {
            super(null, null, null, false);
        }
    }
}

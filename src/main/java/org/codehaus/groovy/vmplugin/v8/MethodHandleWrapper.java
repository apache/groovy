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
    private final AtomicLong latestHitCount = new AtomicLong(0);
    /**
     * The global {@code AotDispatch} invalidation stamp at creation. Read only for sites
     * linked in AOT mode: a mismatch on a PIC hit means the MOP changed since this wrapper
     * was selected and it must be treated as a miss (the AOT replacement for the SwitchPoint
     * guards, which cannot fire under native image). Construction runs at the end of
     * selection, so this stamp only covers invalidations arriving after caching; one landing
     * while selection itself runs is caught at the PIC write instead
     * (see {@code IndyInterface#putSelected}).
     */
    private final long aotStamp = org.apache.groovy.runtime.indy.AotDispatch.stamp();

    /**
     * Creates a wrapper for the cached and relink targets of a meta method.
     *
     * @param cachedMethodHandle the cached invocation handle
     * @param targetMethodHandle the relink target handle
     * @param method the associated meta method
     * @param canSetTarget whether the call site target may be updated to this handle
     */
    public MethodHandleWrapper(MethodHandle cachedMethodHandle, MethodHandle targetMethodHandle, MetaMethod method, boolean canSetTarget) {
        this.cachedMethodHandle = cachedMethodHandle;
        this.targetMethodHandle = targetMethodHandle;
        this.method = method;
        this.canSetTarget = canSetTarget;
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
     * Returns the global {@code AotDispatch} invalidation stamp captured at creation
     * (see the {@code aotStamp} field for how AOT-linked sites use it).
     *
     * @return the creation-time stamp
     */
    long getAotStamp() {
        return aotStamp;
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
     * Returns the PIC sentinel used when a selection must not be stored under a
     * receiver-class cache key ({@code canSetTarget == false}).
     * <p>
     * Renamed from the historical {@code getNullMethodHandleWrapper} name: the
     * value is not “null work”, it is the deliberate “do not class-key-cache
     * this shape” marker consumed by {@code fromCacheHandle} (GROOVY-12191).
     *
     * @return the uncacheable PIC sentinel (identity-comparable singleton)
     */
    public static MethodHandleWrapper getUncacheablePicSentinel() {
        return UncacheablePicSentinel.INSTANCE;
    }

    /**
     * @deprecated use {@link #getUncacheablePicSentinel()}
     */
    @Deprecated
    public static MethodHandleWrapper getNullMethodHandleWrapper() {
        return getUncacheablePicSentinel();
    }

    /**
     * Shared sentinel wrapper: not a real handle, only a PIC marker for
     * uncacheable receiver shapes (per-instance MetaClass, spread-call, …).
     */
    private static final class UncacheablePicSentinel extends MethodHandleWrapper {
        static final UncacheablePicSentinel INSTANCE = new UncacheablePicSentinel();

        private UncacheablePicSentinel() {
            super(null, null, null, false);
        }
    }
}

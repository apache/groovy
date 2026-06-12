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
import java.lang.invoke.SwitchPoint;
import java.util.concurrent.atomic.LongAdder;

/**
 * Wrap method handles
 *
 * @since 3.0.0
 */
class MethodHandleWrapper {
    private final MethodHandle cachedMethodHandle;
    private final MethodHandle targetMethodHandle;
    private final MetaMethod method;
    private final SwitchPoint switchPoint;
    private final boolean canSetTarget;
    private final LongAdder latestHitCount = new LongAdder();

    /**
     * Creates a wrapper for the cached and relink targets of a meta method.
     *
     * @param cachedMethodHandle the cached invocation handle
     * @param targetMethodHandle the relink target handle
     * @param method the associated meta method
     * @param switchPoint the switch point associated with this handle
     * @param canSetTarget whether the call site target may be updated to this handle
     */
    public MethodHandleWrapper(MethodHandle cachedMethodHandle, MethodHandle targetMethodHandle, MetaMethod method, SwitchPoint switchPoint, boolean canSetTarget) {
        this.cachedMethodHandle = cachedMethodHandle;
        this.targetMethodHandle = targetMethodHandle;
        this.method = method;
        this.switchPoint = switchPoint;
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
     * Indicates whether this wrapper may be installed as the call-site target.
     *
     * @return {@code true} if the target may be updated
     */
    public boolean isCanSetTarget() {
        return canSetTarget;
    }

    /**
     * Increments the hit count for the latest inline-cache hit.
     */
    public void incrementLatestHitCount() {
        latestHitCount.increment();
    }

    /**
     * Resets the latest-hit counter.
     */
    public void resetLatestHitCount() {
        latestHitCount.reset();
    }

    /**
     * Adds the specified value to the latest-hit counter.
     *
     * @param value the value to add
     */
    public void addLatestHitCount(long value) {
        latestHitCount.add(value);
    }

    /**
     * Returns the latest-hit counter value.
     *
     * @return the current latest-hit counter
     */
    public long getLatestHitCount() {
        return latestHitCount.sum();
    }

    /**
     * Returns the switch point associated with this wrapper.
     *
     * @return the associated switch point
     */
    public SwitchPoint getSwitchPoint() {
        return switchPoint;
    }

    /**
     * Returns the sentinel wrapper used when no cacheable handle is available.
     *
     * @return the null sentinel wrapper
     */
    public static MethodHandleWrapper getNullMethodHandleWrapper() {
        return NullMethodHandleWrapper.INSTANCE;
    }

    private static class NullMethodHandleWrapper extends MethodHandleWrapper {
        /**
         * Shared sentinel wrapper representing the absence of a reusable method handle.
         */
        public static final NullMethodHandleWrapper INSTANCE = new NullMethodHandleWrapper();

        private NullMethodHandleWrapper() {
            super(null, null, null, null, false);
        }
    }
}

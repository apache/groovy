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
package org.codehaus.groovy.vmplugin.v7;

import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.memoize.LRUCache;
import org.codehaus.groovy.runtime.memoize.MemoizeCache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a cacheable call site, which can reduce the cost of resolving methods
 *
 * @since 3.0.0
 */
public class CacheableCallSite extends MutableCallSite {
    private static final int CACHE_SIZE = SystemUtil.getIntegerSafe("groovy.indy.callsite.cache.size", 16);
    private final MemoizeCache<String, MethodHandleWrapper> cache = new LRUCache<>(CACHE_SIZE);
    private volatile MethodHandleWrapper latestHitMethodHandleWrapper = null;
    private final AtomicLong fallbackCount = new AtomicLong(0);
    private MethodHandle defaultTarget;
    private MethodHandle fallbackTarget;

    public CacheableCallSite(MethodType type) {
        super(type);
    }

    public MethodHandleWrapper getAndPut(String className, MemoizeCache.ValueProvider<? super String, ? extends MethodHandleWrapper> valueProvider) {
        final MethodHandleWrapper result = cache.getAndPut(className, valueProvider);
        final MethodHandleWrapper lhmh = latestHitMethodHandleWrapper;

        if (lhmh == result) {
            result.incrementLatestHitCount();
        } else {
            result.resetLatestHitCount();
            if (null != lhmh) lhmh.resetLatestHitCount();

            latestHitMethodHandleWrapper = result;
        }

        return result;
    }

    public MethodHandleWrapper put(String name, MethodHandleWrapper mhw) {
        return cache.put(name, mhw);
    }

    public long incrementFallbackCount() {
        return fallbackCount.incrementAndGet();
    }

    public void resetFallbackCount() {
        fallbackCount.set(0);
    }

    public MethodHandle getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(MethodHandle defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public MethodHandle getFallbackTarget() {
        return fallbackTarget;
    }

    public void setFallbackTarget(MethodHandle fallbackTarget) {
        this.fallbackTarget = fallbackTarget;
    }
}

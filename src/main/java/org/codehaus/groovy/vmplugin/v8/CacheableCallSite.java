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

import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.memoize.MemoizeCache;

import java.io.Serial;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a cacheable call site, which can reduce the cost of resolving methods
 *
 * @since 3.0.0
 */
public class CacheableCallSite extends MutableCallSite {
    private static final int CACHE_SIZE = SystemUtil.getIntegerSafe("groovy.indy.callsite.cache.size", 8);
    private static final float LOAD_FACTOR = 0.75f;
    private static final int INITIAL_CAPACITY = (int) Math.ceil(CACHE_SIZE / LOAD_FACTOR) + 1;
    private final MethodHandles.Lookup lookup;
    private volatile SoftReference<MethodHandleWrapper> latestHitMethodHandleWrapperSoftReference = null;
    private final AtomicLong fallbackCount = new AtomicLong();
    private final AtomicLong fallbackRound = new AtomicLong();
    private MethodHandle defaultTarget;
    private MethodHandle fallbackTarget;
    private final Map<String, SoftReference<MethodHandleWrapper>> lruCache =
            new LinkedHashMap<String, SoftReference<MethodHandleWrapper>>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
                @Serial private static final long serialVersionUID = 7785958879964294463L;

                /**
                 * Evicts the eldest cached entry when the inline cache grows beyond its limit.
                 *
                 * @param eldest the eldest entry in access order
                 * @return {@code true} when the eldest entry should be removed
                 */
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > CACHE_SIZE;
                }
            };

    /**
     * Creates a cacheable call site for the supplied type and lookup context.
     *
     * @param type the call-site type
     * @param lookup the lookup used to unreflect targets
     */
    public CacheableCallSite(MethodType type, MethodHandles.Lookup lookup) {
        super(type);
        this.lookup = lookup;
    }

    /**
     * Returns a cached method-handle wrapper for the receiver class, computing and storing it if needed.
     *
     * @param className the receiver cache key
     * @param valueProvider the provider used to compute a missing entry
     * @return the cached or newly created wrapper
     */
    public MethodHandleWrapper getAndPut(String className, MemoizeCache.ValueProvider<? super String, ? extends MethodHandleWrapper> valueProvider) {
        MethodHandleWrapper result = null;
        SoftReference<MethodHandleWrapper> resultSoftReference;
        synchronized (lruCache) {
            resultSoftReference = lruCache.get(className);
            if (null != resultSoftReference) {
                result = resultSoftReference.get();
                if (null == result) removeAllStaleEntriesOfLruCache();
            }

            if (null == result) {
                result = valueProvider.provide(className);
                resultSoftReference = new SoftReference<>(result);
                lruCache.put(className, resultSoftReference);
            }
        }
        final SoftReference<MethodHandleWrapper> latestHitReference = latestHitMethodHandleWrapperSoftReference;
        if (latestHitReference == resultSoftReference) {
            result.incrementLatestHitCount();
        } else {
            final MethodHandleWrapper latestHitMethodHandleWrapper = null == latestHitReference ? null : latestHitReference.get();
            if (latestHitMethodHandleWrapper == result) {
                result.incrementLatestHitCount();
                latestHitMethodHandleWrapperSoftReference = resultSoftReference;
            } else {
                result.resetLatestHitCount();
                if (null != latestHitMethodHandleWrapper) latestHitMethodHandleWrapper.resetLatestHitCount();
                latestHitMethodHandleWrapperSoftReference = resultSoftReference;
            }
        }

        return result;
    }

    /**
     * Stores a method-handle wrapper under the supplied cache key.
     *
     * @param name the receiver cache key
     * @param mhw the wrapper to cache
     * @return the previously cached wrapper, or {@code null} if none existed
     */
    public MethodHandleWrapper put(String name, MethodHandleWrapper mhw) {
        synchronized (lruCache) {
            final SoftReference<MethodHandleWrapper> methodHandleWrapperSoftReference;
            methodHandleWrapperSoftReference = lruCache.put(name, new SoftReference<>(mhw));
            if (null == methodHandleWrapperSoftReference) return null;
            final MethodHandleWrapper methodHandleWrapper = methodHandleWrapperSoftReference.get();
            if (null == methodHandleWrapper) removeAllStaleEntriesOfLruCache();
            return methodHandleWrapper;
        }
    }

    private void removeAllStaleEntriesOfLruCache() {
        CACHE_CLEANER_QUEUE.offer(() -> {
            synchronized (lruCache) {
                lruCache.values().removeIf(v -> null == v.get());
            }
        });
    }

    /**
     * Increments the number of fallback executions for this call site.
     *
     * @return the updated fallback count
     */
    public long incrementFallbackCount() {
        return fallbackCount.incrementAndGet();
    }

    /**
     * Resets the fallback count and advances the fallback round marker.
     */
    public void resetFallbackCount() {
        fallbackCount.set(0);
        fallbackRound.incrementAndGet();
    }

    /**
     * Returns the fallback round counter.
     *
     * @return the fallback round counter
     */
    public AtomicLong getFallbackRound() {
        return fallbackRound;
    }

    /**
     * Returns the default target currently installed on this call site.
     *
     * @return the default target
     */
    public MethodHandle getDefaultTarget() {
        return defaultTarget;
    }

    /**
     * Stores the default target for this call site.
     *
     * @param defaultTarget the default target handle
     */
    public void setDefaultTarget(MethodHandle defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    /**
     * Returns the fallback target used when guards fail.
     *
     * @return the fallback target
     */
    public MethodHandle getFallbackTarget() {
        return fallbackTarget;
    }

    /**
     * Stores the fallback target used when guards fail.
     *
     * @param fallbackTarget the fallback target handle
     */
    public void setFallbackTarget(MethodHandle fallbackTarget) {
        this.fallbackTarget = fallbackTarget;
    }

    /**
     * Returns the lookup associated with this call site.
     *
     * @return the call-site lookup
     */
    public MethodHandles.Lookup getLookup() {
        return lookup;
    }

    private static final BlockingQueue<Runnable> CACHE_CLEANER_QUEUE = new LinkedBlockingQueue<>();
    static {
        Thread cacheCleaner = new Thread(() -> {
            while (true) {
                try {
                    CACHE_CLEANER_QUEUE.take().run();
                } catch (Throwable ignore) {
                    Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest(DefaultGroovyMethods.asString(ignore));
                    }
                }
            }
        }, "PIC-Cleaner");
        cacheCleaner.setDaemon(true);
        cacheCleaner.start();
    }
}

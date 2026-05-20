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
 * Represents a cacheable call site, which manages a multi-level caching hierarchy for dynamic method dispatch.
 * <p>
 * To minimize the overhead of dynamic method selection and invocation, this class maintains three levels of caching:
 * <ol>
 *   <li><b>Level 1: Polymorphic Inline Cache (PIC) Chain</b>:
 *       A site-local, bounded chain of guarded method handles (default size 4) stored directly in the {@link #getTarget() target}.
 *       This is the fastest path, allowing the JVM's JIT compiler to inline calls for the hottest receiver shapes.
 *       It is managed via {@link #getPicChain()} and updated by {@code IndyInterface.optimizeCallSite}.
 *   </li>
 *   <li><b>Level 2: Most Recently Used (MRU) Entry</b>:
 *       A {@code volatile} field {@link #mruEntry} that stores a single {@link MethodHandleWrapper} for the most recently successful hit.
 *       Accessed via {@link #get(Object)}, it provides a lock-free path for monomorphic or low-polymorphic call sites
 *       that fall through the PIC chain. It uses identity-based keys to avoid allocations.
 *   </li>
 *   <li><b>Level 3: Least Recently Used (LRU) Cache</b>:
 *       A synchronized {@link LinkedHashMap} {@link #lruCache} (default size 8) that stores {@link SoftReference}s to
 *       {@link MethodHandleWrapper}s. This serves as the megamorphic fallback, preventing full re-selection
 *       for shapes that have been seen before but are not currently in the PIC or MRU.
 *   </li>
 * </ol>
 * <p>
 * <b>Leak-Awareness:</b> To prevent permanent ClassLoader leaks, Level 2 (MRU) uses strong references only when
 * the target class belongs to a safe ClassLoader (same or parent). Level 3 (LRU) always uses {@link SoftReference}s
 * to allow the JVM to reclaim Metaspace under memory pressure.
 *
 * @since 3.0.0
 */
public class CacheableCallSite extends MutableCallSite {
    private static final int CACHE_SIZE = SystemUtil.getIntegerSafe("groovy.indy.callsite.cache.size", 8);
    private static final float LOAD_FACTOR = 0.75f;
    private static final int INITIAL_CAPACITY = (int) Math.ceil(CACHE_SIZE / LOAD_FACTOR) + 1;
    private final MethodHandles.Lookup lookup;
    private volatile MRUEntry mruEntry;
    private final AtomicLong fallbackCount = new AtomicLong();
    private final AtomicLong fallbackRound = new AtomicLong();
    private MethodHandle defaultTarget;
    private MethodHandle fallbackTarget;
    private volatile MethodHandle picChain;
    private Object[] picKeys;
    private int picCount;
    private final Map<Object, SoftReference<MethodHandleWrapper>> lruCache =
            new LinkedHashMap<Object, SoftReference<MethodHandleWrapper>>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
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
     * Returns the cached method-handle wrapper for the receiver key if it is the most recently used.
     *
     * @param key the receiver cache key
     * @return the cached wrapper, or {@code null} if not found or not MRU
     */
    public MethodHandleWrapper get(Object key) {
        MRUEntry entry = mruEntry;
        if (entry != null && entry.key == key) {
            return entry.wrapper;
        }
        return null;
    }

    /**
     * Returns a cached method-handle wrapper for the receiver class, computing and storing it if needed.
     *
     * @param key the receiver cache key
     * @param valueProvider the provider used to compute a missing entry
     * @param sender the caller class
     * @return the cached or newly created wrapper
     */
    public MethodHandleWrapper getAndPut(Object key, MemoizeCache.ValueProvider<? super Object, ? extends MethodHandleWrapper> valueProvider, Class<?> sender) {
        MethodHandleWrapper result = null;
        SoftReference<MethodHandleWrapper> resultSoftReference;
        synchronized (lruCache) {
            resultSoftReference = lruCache.get(key);
            if (null != resultSoftReference) {
                result = resultSoftReference.get();
                if (null == result) removeAllStaleEntriesOfLruCache();
            }

            if (null == result) {
                result = valueProvider.provide(key);
                resultSoftReference = new SoftReference<>(result);
                lruCache.put(key, resultSoftReference);
            }
        }

        updateMRU(key, result, sender);

        return result;
    }

    private void updateMRU(Object key, MethodHandleWrapper result, Class<?> sender) {
        if (result == null || result == MethodHandleWrapper.getNullMethodHandleWrapper()) return;

        // Leak-Awareness: only store strongly if the target loader is safe
        var method = result.getMethod();
        if (method != null) {
            Class<?> declaringClass = method.getDeclaringClass().getTheClass();
            if (isSafeLoader(sender.getClassLoader(), declaringClass.getClassLoader())) {
                mruEntry = new MRUEntry(key, result);
            }
        }
    }

    private static boolean isSafeLoader(ClassLoader callerLoader, ClassLoader targetLoader) {
        if (targetLoader == null) return true; // Bootstrap is always safe
        if (callerLoader == targetLoader) return true;
        ClassLoader cl = callerLoader;
        while (cl != null) {
            if (cl == targetLoader) return true;
            cl = cl.getParent();
        }
        return false;
    }

    /**
     * Stores a method-handle wrapper under the supplied cache key.
     *
     * @param key the receiver cache key
     * @param mhw the wrapper to cache
     * @return the previously cached wrapper, or {@code null} if none existed
     */
    public MethodHandleWrapper put(Object key, MethodHandleWrapper mhw) {
        synchronized (lruCache) {
            final SoftReference<MethodHandleWrapper> methodHandleWrapperSoftReference;
            methodHandleWrapperSoftReference = lruCache.put(key, new SoftReference<>(mhw));
            if (null == methodHandleWrapperSoftReference) return null;
            final MethodHandleWrapper methodHandleWrapper = methodHandleWrapperSoftReference.get();
            if (null == methodHandleWrapper) removeAllStaleEntriesOfLruCache();
            return methodHandleWrapper;
        }
    }

    public void recordInPic(Object key, int maxPicSize) {
        if (picKeys == null) picKeys = new Object[maxPicSize];
        if (picCount < picKeys.length) {
            picKeys[picCount++] = key;
        }
    }

    public boolean picIncludes(Object key) {
        if (picKeys == null) return false;
        for (int i = 0; i < picCount; i++) {
            if (picKeys[i] == key) return true;
        }
        return false;
    }

    public MethodHandle getPicChain() {
        return picChain;
    }

    public void setPicChain(MethodHandle picChain) {
        this.picChain = picChain;
    }

    public int getPicCount() {
        return picCount;
    }

    private static final class MRUEntry {
        final Object key;
        final MethodHandleWrapper wrapper;
        MRUEntry(Object key, MethodHandleWrapper wrapper) {
            this.key = key;
            this.wrapper = wrapper;
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
        picCount = 0;
        picChain = null;
        picKeys = null;
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

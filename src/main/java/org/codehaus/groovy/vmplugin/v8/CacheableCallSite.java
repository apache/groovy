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

import java.io.Serial;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.memoize.MemoizeCache;

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
    private static final Logger LOGGER = Logger.getLogger(CacheableCallSite.class.getName());
    private static final int CACHE_SIZE = SystemUtil.getIntegerSafe("groovy.indy.callsite.cache.size", 8);
    private static final float LOAD_FACTOR = 0.75f;
    private static final int INITIAL_CAPACITY = (int) Math.ceil(CACHE_SIZE / LOAD_FACTOR) + 1;
    private final MethodHandles.Lookup lookup;
    final IndyInterface.CallType callType;
    final boolean safe;
    /**
     * Indicates whether the invocation is a {@code this} call.
     */
    final boolean thisCall;
    final boolean spreadCall;
    /**
     * Stores the most recently accessed entry.
     * <p>
     * <b>Concurrency:</b> Marked as {@code volatile} to ensure thread-safe publication of the entry
     * across different threads, allowing {@link #get(Object)} to remain lock-free.
     */
    private volatile MRUEntry mruEntry;
    private final AtomicLong fallbackCount = new AtomicLong();
    private final AtomicLong fallbackRound = new AtomicLong();
    private MethodHandle defaultTarget;
    private MethodHandle fallbackTarget;
    /**
     * The direct target of the call site before global guards are applied.
     * <p>
     * <b>Concurrency:</b> {@code volatile} ensures that updates to the PIC chain are immediately
     * visible to all threads during high-speed dispatch.
     */
    @SuppressWarnings("java:S3077")
    private volatile MethodHandle picChain;
    @SuppressWarnings("java:S3077")
    private volatile java.lang.invoke.SwitchPoint picSwitchPoint;

    /**
     * Keys corresponding to the handles in the {@link #picChain}.
     * <p>
     * <b>Concurrency:</b> {@code final} for safe visibility during concurrent lookups.
     */
    private final Object[] picKeys;

    /**
     * The number of active entries in the PIC.
     * <p>
     * <b>Concurrency:</b> {@code volatile} for safe visibility. Modifications are further
     * protected by {@code synchronized} blocks to ensure atomicity.
     */
    private volatile int picCount;
    private final Map<Object, SoftReference<MethodHandleWrapper>> lruCache =
            new LinkedHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
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
     * @param type       the call-site type
     * @param lookup     the lookup used to un-reflect targets
     * @param callType
     * @param safe
     * @param thisCall
     * @param spreadCall
     */
    CacheableCallSite(MethodType type, MethodHandles.Lookup lookup, IndyInterface.CallType callType, boolean safe, boolean thisCall, boolean spreadCall) {
        super(type);
        this.callType = callType;
        this.safe = safe;
        this.thisCall = thisCall;
        this.spreadCall = spreadCall;
        this.picKeys = new Object[IndyInterface.INDY_PIC_SIZE];
        this.lookup = lookup;
    }

    /**
     * Returns the cached method-handle wrapper for the receiver key if it is the most recently used.
     *
     * @param key the receiver cache key
     * @return the cached wrapper, or {@code null} if not found or not MRU
     */
    MethodHandleWrapper get(Object key) {
        MRUEntry entry = mruEntry;
        if (entry != null && entry.key == key) {
            MethodHandleWrapper mhw = entry.wrapper;
            if (mhw == MethodHandleWrapper.getNullMethodHandleWrapper() || mhw.getSwitchPoint() == IndyInterface.switchPoint) {
                return mhw;
            }
            mruEntry = null;
        }
        return null;
    }

    /**
     * Returns a cached method-handle wrapper for the receiver class, computing and storing it if needed.
     * <p>
     * <b>Concurrency:</b> Synchronizes on {@link #lruCache} to protect the non-thread-safe
     * {@link LinkedHashMap} and to ensure that a missing entry is only computed once.
     *
     * @param key the receiver cache key
     * @param valueProvider the provider used to compute a missing entry
     * @param sender the caller class
     * @return the cached or newly created wrapper
     */
    MethodHandleWrapper getAndPut(Object key, MemoizeCache.ValueProvider<? super Object, ? extends MethodHandleWrapper> valueProvider, Class<?> sender) {
        MethodHandleWrapper result = null;

        // First check under lock (fast path — already in cache)
        synchronized (lruCache) {
            SoftReference<MethodHandleWrapper> resultSoftReference = lruCache.get(key);
            if (null != resultSoftReference) {
                result = resultSoftReference.get();
                if (null == result || (result != MethodHandleWrapper.getNullMethodHandleWrapper() && result.getSwitchPoint() != IndyInterface.switchPoint)) {
                    lruCache.remove(key);
                    result = null;
                }
            }
        }

        // Compute outside lock if not found (expensive operation — method selection)
        if (null == result) {
            result = valueProvider.provide(key);

            // Second check under lock — another thread may have stored it in the meantime
            synchronized (lruCache) {
                SoftReference<MethodHandleWrapper> existingRef = lruCache.get(key);
                if (existingRef != null) {
                    MethodHandleWrapper existing = existingRef.get();
                    if (existing != null && existing.getSwitchPoint() == IndyInterface.switchPoint) {
                        // Another thread already computed and stored; use theirs
                        result = existing;
                    } else {
                        // Reference was cleared or stale; replace it
                        lruCache.put(key, new SoftReferenceWithKey(key, result, REFERENCE_QUEUE, lruCache));
                    }
                } else {
                    lruCache.put(key, new SoftReferenceWithKey(key, result, REFERENCE_QUEUE, lruCache));
                }
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
     * <p>
     * <b>Concurrency:</b> Synchronizes on {@link #lruCache} for thread-safe access to the underlying map.
     *
     * @param key the receiver cache key
     * @param mhw the wrapper to cache
     * @return the previously cached wrapper, or {@code null} if none existed
     */
    MethodHandleWrapper put(Object key, MethodHandleWrapper mhw) {
        synchronized (lruCache) {
            final SoftReference<MethodHandleWrapper> methodHandleWrapperSoftReference =
                lruCache.put(key, new SoftReferenceWithKey(key, mhw, REFERENCE_QUEUE, lruCache));
            if (null == methodHandleWrapperSoftReference) return null;
            final MethodHandleWrapper methodHandleWrapper = methodHandleWrapperSoftReference.get();
            if (null == methodHandleWrapper) {
                lruCache.remove(key);
            }
            return methodHandleWrapper;
        }
    }

    /**
     * Promotes a new receiver shape to the PIC if it is not already present and space is available.
     * <p>
     * <b>Concurrency:</b> Synchronizes on {@code this} to atomically manage the
     * promotion of receiver shapes into the PIC chain. This prevents multiple threads
     * from corrupting the chain metadata or redundant JIT invalidations.
     *
     * @param key the receiver cache key
     * @param updater the callback used to build the new PIC link
     */
    public void maybeUpdatePic(Object key, java.util.function.UnaryOperator<MethodHandle> updater) {
        synchronized (this) {
            var currentSwitchPoint = IndyInterface.switchPoint;
            if (picSwitchPoint != currentSwitchPoint) {
                clearPic();
                picSwitchPoint = currentSwitchPoint;
            }
            for (int i = 0; i < picCount; i++) {
                if (picKeys[i] == key) return;
            }
            if (picCount < picKeys.length) {
                MethodHandle currentChain = picChain != null ? picChain : defaultTarget;
                MethodHandle newChain = updater.apply(currentChain);
                if (newChain != null) {
                    picChain = newChain;
                    picKeys[picCount++] = key;
                }
            }
        }
    }

    /**
     * Checks if a receiver shape is already present in the PIC.
     * <p>
     * <b>Concurrency:</b> Lock-free read of the PIC metadata. The volatile read of {@link #picCount}
     * ensures visibility of prior writes to {@link #picKeys} by the same or another thread.
     *
     * @param key the receiver cache key
     * @return {@code true} if the key is in the PIC
     */
    public boolean picInsertIfMissing(Object key) {
        if (picSwitchPoint != IndyInterface.switchPoint) {
            return true;
        }
        int count = picCount;
        for (int i = 0; i < count; i++) {
            if (picKeys[i] == key) return false;
        }
        return true;
    }

    public MethodHandle getPicChain() {
        return picChain;
    }

    public void clearPic() {
        synchronized (this) {
            picChain = null;
            picSwitchPoint = null;
            picCount = 0;
            for (int i = 0; i < picKeys.length; i++) {
                picKeys[i] = null;
            }
        }
    }

    public int getPicCount() {
        return picCount;
    }

    @javax.annotation.concurrent.Immutable
    private static final class MRUEntry {
        final Object key;
        final MethodHandleWrapper wrapper;
        MRUEntry(Object key, MethodHandleWrapper wrapper) {
            this.key = key;
            this.wrapper = wrapper;
        }
    }

    private static class SoftReferenceWithKey extends SoftReference<MethodHandleWrapper> {
        private final Object key;
        private final Map<Object, SoftReference<MethodHandleWrapper>> cache;

        SoftReferenceWithKey(Object key, MethodHandleWrapper referent, ReferenceQueue<MethodHandleWrapper> q, Map<Object, SoftReference<MethodHandleWrapper>> cache) {
            super(referent, q);
            this.key = key;
            this.cache = cache;
        }

        void clean() {
            synchronized (cache) {
                if (cache.get(key) == this) {
                    cache.remove(key);
                }
            }
        }
    }

    /**
     * Atomically resets the call site to the default target when the fallback count
     * exceeds the given threshold. This provides a concurrency-safe, double-checked
     * locking reset for mega-morphic call sites.
     * <p>
     * <b>Concurrency:</b> Synchronizes on {@code this} to atomically reset
     * the target and associated PIC metadata, preventing redundant resets
     * when multiple threads detect a high fallback count simultaneously.
     *
     * @param defaultTarget     the default target handle to restore
     * @param threshold         the fallback count threshold that triggers a reset
     * @param fallbackCount     the current fallback count
     * @return {@code true} if the target was reset to the default
     */
    public boolean tryResetToDefaultTarget(MethodHandle defaultTarget, long threshold, long fallbackCount) {
        if (fallbackCount > threshold && getTarget() != defaultTarget) {
            synchronized (this) {
                if (getTarget() != defaultTarget) {
                    setTarget(defaultTarget);
                    resetFallbackCount();
                    return true;
                }
            }
        }
        return false;
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
     * <p>
     * <b>Concurrency:</b> Marked as {@code synchronized} to atomically clear all PIC-related
     * state, ensuring threads see a consistent "empty" state.
     */
    public synchronized void resetFallbackCount() {
        fallbackCount.set(0);
        fallbackRound.incrementAndGet();
        picCount = 0;
        picChain = null;
        Arrays.fill(picKeys, null);
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

    private static final ReferenceQueue<MethodHandleWrapper> REFERENCE_QUEUE = new ReferenceQueue<>();
    static {
        Thread cacheCleaner = new Thread(() -> {
            while (true) {
                try {
                    Reference<? extends MethodHandleWrapper> ref = REFERENCE_QUEUE.remove();
                    if (ref instanceof SoftReferenceWithKey sRef) {
                        sRef.clean();
                    }
                } catch (@SuppressWarnings("java:S1181") Throwable throwable) {
                    if (throwable instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        logger.finest(DefaultGroovyMethods.asString(throwable));
                    }
                }
            }
        }, "PIC-Cleaner");
        cacheCleaner.setDaemon(true);
        cacheCleaner.start();
    }
}

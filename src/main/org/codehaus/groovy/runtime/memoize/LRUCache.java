package org.codehaus.groovy.runtime.memoize;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * A cache backed by a Collections.SynchronizedMap
 *
 * @author Vaclav Pech
 */
public final class LRUCache implements MemoizeCache<Object, Object> {

    private final Map<Object, Object> cache;

    public LRUCache(final int maxCacheSize) {
        cache = Collections.synchronizedMap(new LRUProtectionStorage(maxCacheSize));
    }

    public Object put(final Object key, final Object value) {
        return cache.put(key, value);
    }

    public Object get(final Object key) {
        return cache.get(key);
    }

    /**
     * Replying on the Collections.SynchronizedMap thread-safe iteration implementation the method will remove all entries holding
     * SoftReferences to gc-evicted objects.
     */
    public void cleanUpNullReferences() {
        synchronized (cache) {
            final Iterator<Map.Entry<Object, Object>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<Object, Object> entry = iterator.next();
                if (((SoftReference) entry.getValue()).get() == null) iterator.remove();
            }
        }
    }
}

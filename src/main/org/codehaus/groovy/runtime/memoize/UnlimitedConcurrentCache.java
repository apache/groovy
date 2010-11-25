package org.codehaus.groovy.runtime.memoize;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache backed by a ConcurrentHashMap
 *
 * @author Vaclav Pech
 */
public final class UnlimitedConcurrentCache implements MemoizeCache<Object, Object> {

    private final ConcurrentHashMap<Object, Object> cache = new ConcurrentHashMap<Object, Object>();

    public Object put(final Object key, final Object value) {
        return cache.put(key, value);
    }

    public Object get(final Object key) {
        return cache.get(key);
    }

    /**
     * Replying on the ConcurrentHashMap thread-safe iteration implementation the method will remove all entries holding
     * SoftReferences to gc-evicted objects.
     */
    public void cleanUpNullReferences() {
        final Iterator<Map.Entry<Object, Object>> iterator = cache.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Object, Object> entry = iterator.next();
            Object entryVal = entry.getValue();
            if (entryVal != null && ((SoftReference) entryVal).get() == null) cache.remove(entry.getKey(), entryVal);
        }
    }
}

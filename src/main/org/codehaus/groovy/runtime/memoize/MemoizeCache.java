package org.codehaus.groovy.runtime.memoize;

/**
 * Represents a memoize cache with its essential methods
 * @param <K> type of the keys
 * @param <V> type of the values
 *
 * @author Vaclav Pech
 */
public interface MemoizeCache<K, V> {

    V put(K key, V value);

    V get(K key);

    /**
     * Invoked when some of the held SoftReferences have been evicted by the garbage collector and so should be removed from the cache.
     * The implementation must ensure that concurrent invocations of all methods on the cache may occur from other threads
     * and thus should protect any shared resources.
     */
    void cleanUpNullReferences();
}

package groovy.json.internal;

import groovy.json.internal.Cache;
import groovy.json.internal.CacheType;

import java.util.LinkedHashMap;
import java.util.Map;


/* This supports both LRU and FIFO */
public class SimpleCache<K, V> implements Cache<K, V> {

    Map<K, V> map = new LinkedHashMap();


    private static class InternalCacheLinkedList<K, V> extends LinkedHashMap<K, V> {
        final int limit;

        InternalCacheLinkedList( final int limit, final boolean lru ) {
            super( 16, 0.75f, lru );
            this.limit = limit;
        }

        protected final boolean removeEldestEntry( final Map.Entry<K, V> eldest ) {
            return super.size() > limit;
        }
    }


    public SimpleCache( final int limit, CacheType type ) {

        if ( type.equals( CacheType.LRU ) ) {
            map = new InternalCacheLinkedList<K, V>( limit, true );
        } else {
            map = new InternalCacheLinkedList<K, V>( limit, false );
        }
    }

    public SimpleCache( final int limit ) {

        map = new InternalCacheLinkedList<K, V>( limit, true );

    }

    @Override
    public void put( K key, V value ) {
        map.put( key, value );
    }

    @Override
    public V get( K key ) {
        return map.get( key );
    }

    //For testing only
    @Override
    public V getSilent( K key ) {
        V value = map.get( key );
        if ( value != null ) {
            map.remove( key );
            map.put( key, value );
        }
        return value;
    }

    @Override
    public void remove( K key ) {
        map.remove( key );
    }

    @Override
    public int size() {
        return map.size();
    }

    public String toString() {
        return map.toString();
    }


}

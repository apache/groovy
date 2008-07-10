package org.codehaus.groovy.util;

public class ConcurrentSoftMap<K,V> extends AbstractConcurrentMap<K,V> {
    public ConcurrentSoftMap() {
    }

    protected Segment<K,V> createSegment(int cap) {
        return new ConcurrentSoftMap.Segment<K,V>(cap);
    }

    public static class Segment<K,V> extends AbstractConcurrentMap.Segment<K,V>{
        public Segment(int cap) {
            super(cap);
        }

        protected AbstractConcurrentMap.Entry<K,V> createEntry(K key, int hash, V value) {
            return new EntryWithValue<K,V>(this, key, hash, value);
        }
    }

    public static class Entry<K,V> extends FinalizableRef.SoftRef<K> implements AbstractConcurrentMap.Entry<K,V> {
        private final Segment segment;
        private int hash;

        public Entry(Segment segment, K key, int hash) {
            super(key);
            this.segment = segment;
            this.hash = hash;
        }

        public boolean isValid() {
            return get() != null;
        }

        public boolean isEqual(K key, int hash) {
            return this.hash == hash && get() == key;
        }

        public V getValue() {
            return (V)this;
        }

        public void setValue(V value) {
        }

        public int getHash() {
            return hash;
        }

        public void finalizeRef() {
            super.finalizeRef();
            segment.removeEntry(this);
        }
    }

    public static class EntryWithValue<K,V> extends Entry<K,V> {
        private V value;

        public EntryWithValue(Segment segment, K key, int hash, V value) {
            super(segment, key, hash);
            setValue(value);
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }


        public void finalizeRef() {
            value = null;
            super.finalizeRef();
        }
    }
}
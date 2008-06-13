package org.codehaus.groovy.reflection;

public class ConcurrentSoftMap<K,V> extends AbstractConcurrentMap<K,V> {
    public ConcurrentSoftMap() {
    }

    protected AbstractConcurrentMap.Segment<K,V> createSegment(int cap) {
        return new Segment<K,V>(cap);
    }

    static class Segment<K,V> extends AbstractConcurrentMap.Segment<K,V>{
        public Segment(int cap) {
            super(cap);
        }

        protected AbstractConcurrentMap.Entry<K,V> createEntry(K key, int hash) {
            return new EntryWithValue(key, hash);
        }
    }

    static class Entry<K,V> extends FinalizableRef.SoftRef<K> implements AbstractConcurrentMap.Entry<K,V> {
        private int hash;

        public Entry(K key, int hash) {
            super(key);
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
        }
    }

    private static class EntryWithValue<K,V> extends Entry<K,V> {
        private V value;

        public EntryWithValue(K key, int hash) {
            super(key, hash);
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
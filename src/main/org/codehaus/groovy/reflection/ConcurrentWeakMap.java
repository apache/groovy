package org.codehaus.groovy.reflection;

public class ConcurrentWeakMap<K,V> extends AbstractConcurrentMap<K,V> {
    public ConcurrentWeakMap() {
    }

    protected AbstractConcurrentMap.Segment<K,V> createSegment(int cap) {
        return new Segment<K,V>(cap);
    }

    final static class Segment<K,V> extends AbstractConcurrentMap.Segment<K,V>{
        public Segment(int cap) {
            super(cap);
        }

        protected AbstractConcurrentMap.Entry<K,V> createEntry(K key, int hash) {
            return new EntryWithValue(key, hash);
        }
    }

    private static class Entry<K,V> extends FinalizableRef.WeakRef<K> implements AbstractConcurrentMap.Entry<K,V> {
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
            throw new UnsupportedOperationException();
        }

        public int getHash() {
            return hash;
        }

        public void finalizeRef() {
        }
    }

    public static class EntryWithValue<K,V> extends Entry<K,V> {
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

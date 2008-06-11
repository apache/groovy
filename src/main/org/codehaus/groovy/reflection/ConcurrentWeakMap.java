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

        protected AbstractConcurrentMap.Entry<K, V> createEntry(AbstractConcurrentMap.Entry<K,V> e, AbstractConcurrentMap.Entry<K,V> next) {
            return new Entry (e, next);
        }

        protected AbstractConcurrentMap.Entry<K, V> createEntry(K key, int hash, V value, AbstractConcurrentMap.Entry<K, V> first) {
            return new Entry(key, hash, value, first);
        }

        private class Entry extends FinalizableRef.WeakRef<K> implements AbstractConcurrentMap.Entry<K,V> {
            private AbstractConcurrentMap.Entry<K,V> next;
            private V value;
            private int hash;

            public Entry(K referent) {
                super(referent);
            }

            public Entry(K key, int hash, V value, AbstractConcurrentMap.Entry<K, V> first) {
                super(key);
                this.hash = hash;
                this.value = value;
                this.next = first;
            }

            public Entry(AbstractConcurrentMap.Entry<K,V> e, AbstractConcurrentMap.Entry<K,V> next) {
                super(((Entry)e).get());
                this.hash = e.getHash();
                this.next = next;
                this.value = e.getValue();
            }

            public AbstractConcurrentMap.Entry<K,V> getNext() {
                return next;
            }

            public void setNext(AbstractConcurrentMap.Entry<K,V> ee) {
                next = ee;
            }

            public V getValue() {
                return value;
            }

            public void setValue(V value) {
                this.value = value;
            }

            public boolean isValid() {
                return get() != null;
            }

            public boolean isEqual(K key, int hash) {
                return this.hash == hash && get() == key;
            }

            public int getHash() {
                return hash;
            }

            public void finalizeRef() {
            }
        }
    }
}

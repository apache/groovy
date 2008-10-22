/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.util;

import org.codehaus.groovy.util.ManagedReference.ReferenceBundle;

public class ManagedDoubleKeyMap<K1,K2,V> extends AbstractConcurrentDoubleKeyMap<K1,K2,V> {
    public ManagedDoubleKeyMap(ReferenceBundle bundle) {
        super(bundle);
    }
    
    protected AbstractConcurrentDoubleKeyMap.Segment<K1,K2,V> createSegment(Object segmentInfo, int cap) {
        ReferenceBundle bundle = (ReferenceBundle) segmentInfo;
        return new Segment<K1,K2,V>(bundle, cap);
    }

    static class Segment<K1,K2,V> extends AbstractConcurrentDoubleKeyMap.Segment<K1,K2,V>{
        private ReferenceBundle bundle;
        public Segment(ReferenceBundle bundle, int cap) {
            super(cap);
            this.bundle = bundle;
        }

        protected AbstractConcurrentDoubleKeyMap.Entry<K1,K2,V> createEntry(K1 key1, K2 key2, int hash) {
            return new EntryWithValue(bundle, key1, key2, hash, this);
        }
    }

    static class Ref<K> extends ManagedReference<K> {
        final Entry entry;
        public Ref(ReferenceBundle bundle, K referent, Entry entry) {
            super(bundle, referent);
            this.entry = entry;
        }

        public void finalizeRef() {
            this.entry.clean();
        }
    }

    public static class Entry<K1,K2, V> implements AbstractConcurrentDoubleKeyMap.Entry<K1,K2,V> {
        final private int hash;
        final Ref<K1> ref1;
        final Ref<K2> ref2;
        final Segment segment;

        public Entry(ReferenceBundle bundle, K1 key1, K2 key2, int hash, Segment segment) {
            this.hash = hash;
            this.segment = segment;
            ref1 = new Ref(bundle, key1, this);
            ref2 = new Ref(bundle, key2, this);
        }

        public boolean isValid() {
            return ref1.get() != null && ref2.get () != null;
        }

        public boolean isEqual(K1 key1, K2 key2, int hash) {
            return this.hash == hash && ref1.get() == key1 && ref2.get() == key2;
        }

        public V getValue() {
            return (V)this;
        }

        public void setValue(V value) {
        }

        public int getHash() {
            return hash;
        }

        public void clean() {
            segment.removeEntry(this);
            ref1.clear();
            ref2.clear();
        }
    }

    private static class EntryWithValue<K1,K2,V> extends Entry<K1,K2,V> {
        private V value;

        public EntryWithValue(ReferenceBundle bundle, K1 key1, K2 key2, int hash, Segment segment) {
            super(bundle, key1, key2, hash, segment);
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public void clean() {
            super.clean();
            value = null;
        }
    }
}

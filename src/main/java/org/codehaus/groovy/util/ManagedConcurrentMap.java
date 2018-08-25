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
package org.codehaus.groovy.util;

public class ManagedConcurrentMap<K,V> extends AbstractConcurrentMap<K,V> {
    protected ReferenceBundle bundle;
    public ManagedConcurrentMap(ReferenceBundle bundle) {
        super(bundle);
        this.bundle = bundle;
        if (bundle==null) throw new IllegalArgumentException("bundle must not be null");
    }

    protected Segment<K,V> createSegment(Object segmentInfo, int cap) {
        ReferenceBundle bundle = (ReferenceBundle) segmentInfo;
        if (bundle==null) throw new IllegalArgumentException("bundle must not be null");
        return new ManagedConcurrentMap.Segment<>(bundle, cap);
    }

    public static class Segment<K,V> extends AbstractConcurrentMap.Segment<K,V>{
        private static final long serialVersionUID = 2742952509311037869L;
        protected final ReferenceBundle bundle;
        public Segment(ReferenceBundle bundle, int cap) {
            super(cap);
            this.bundle = bundle;
            if (bundle==null) throw new IllegalArgumentException("bundle must not be null");

        }

        protected AbstractConcurrentMap.Entry<K,V> createEntry(K key, int hash, V value) {
            if (bundle==null) throw new IllegalArgumentException("bundle must not be null");
            return new EntryWithValue<>(bundle, this, key, hash, value);
        }
    }

    public static class Entry<K,V> extends ManagedReference<K> implements AbstractConcurrentMap.Entry<K,V> {
        private final Segment segment;
        private final int hash;

        public Entry(ReferenceBundle bundle, Segment segment, K key, int hash) {
            super(bundle, key);
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

        @Override
        public void finalizeReference() {
            segment.removeEntry(this);
            super.finalizeReference();
        }

        /**
         * @deprecated use finalizeReference
         */
        @Deprecated
        public void finalizeRef() {
            finalizeReference();
        }
    }

    public static class EntryWithValue<K,V> extends Entry<K,V> {
        private V value;

        public EntryWithValue(ReferenceBundle bundle, Segment segment, K key, int hash, V value) {
            super(bundle, segment, key, hash);
            setValue(value);
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public void finalizeReference() {
            value = null;
            super.finalizeReference();
        }
    }
}

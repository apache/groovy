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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;


public enum ReferenceType {
    SOFT {
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new SoftRef(value, handler, queue);
        }
    },
    WEAK {
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new WeakRef(value, handler, queue);
        }
    },
    PHANTOM {
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new PhantomRef(value, handler, queue);
        }            
    },
    HARD {
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new HardRef(value, handler, queue);
        }
    };
    protected abstract <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue);
    
    private static class SoftRef<TT,V  extends Finalizable> extends SoftReference<TT> implements Reference<TT,V> {
        private final V handler;
        public SoftRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            super(referent, q);
            this.handler = handler;
        }
        @Override
        public V getHandler() {
            return handler;
        }        
    }
    
    private static class WeakRef<TT,V  extends Finalizable> extends WeakReference<TT> implements Reference<TT,V> {
        private final V handler;
        public WeakRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            super(referent, q);
            this.handler = handler;
        }
        @Override
        public V getHandler() {
            return handler;
        }            
    }
    
    private static class PhantomRef<TT,V  extends Finalizable> extends PhantomReference<TT> implements Reference<TT,V> {
        private final V handler;
        public PhantomRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            super(referent, q);
            this.handler = handler;
        }
        @Override
        public V getHandler() {
            return handler;
        }            
    }
    
    private static class HardRef<TT,V extends Finalizable> implements Reference<TT,V> {
        private TT ref;
        private final V handler;
        public HardRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            this.ref = referent;
            this.handler = handler;
        }
        @Override
        public V getHandler() {
            return handler;
        }
        @Override
        public TT get() {
            return ref;
        }
        @Override
        public void clear() {
            ref = null;
        }        
    }
    
}

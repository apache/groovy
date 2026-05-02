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


/**
 * Supported reference implementations for managed references.
 */
public enum ReferenceType {
    /**
     * Stores referents as softly reachable values that may be cleared under memory pressure.
     */
    SOFT {
        /** {@inheritDoc} */
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new SoftRef(value, handler, queue);
        }
    },
    /**
     * Stores referents as weakly reachable values that may be cleared once weakly reachable.
     */
    WEAK {
        /** {@inheritDoc} */
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new WeakRef(value, handler, queue);
        }
    },
    /**
     * Stores referents as phantom references for post-mortem cleanup only.
     */
    PHANTOM {
        /** {@inheritDoc} */
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new PhantomRef(value, handler, queue);
        }            
    },
    /**
     * Stores referents strongly until cleared explicitly.
     */
    HARD {
        /** {@inheritDoc} */
        @Override
        protected <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue) {
            return new HardRef(value, handler, queue);
        }
    };

    /**
     * Creates a reference of this type for the supplied referent and handler.
     *
     * @param value the referent to wrap
     * @param handler the cleanup handler associated with the reference
     * @param queue the queue to notify for queue-backed references
     * @param <T> the referent type
     * @param <V> the handler type
     * @return the created reference
     */
    protected abstract <T,V extends Finalizable> Reference<T,V> createReference(T value, V handler, ReferenceQueue queue);

    private static class SoftRef<TT,V  extends Finalizable> extends SoftReference<TT> implements Reference<TT,V> {
        private final V handler;

        /**
         * Creates a soft managed reference.
         *
         * @param referent the referent to wrap
         * @param handler the cleanup handler associated with the reference
         * @param q the queue to receive the reference when cleared
         */
        public SoftRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            super(referent, q);
            this.handler = handler;
        }

        /** {@inheritDoc} */
        @Override
        public V getHandler() {
            return handler;
        }        
    }

    private static class WeakRef<TT,V  extends Finalizable> extends WeakReference<TT> implements Reference<TT,V> {
        private final V handler;

        /**
         * Creates a weak managed reference.
         *
         * @param referent the referent to wrap
         * @param handler the cleanup handler associated with the reference
         * @param q the queue to receive the reference when cleared
         */
        public WeakRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            super(referent, q);
            this.handler = handler;
        }

        /** {@inheritDoc} */
        @Override
        public V getHandler() {
            return handler;
        }            
    }

    private static class PhantomRef<TT,V  extends Finalizable> extends PhantomReference<TT> implements Reference<TT,V> {
        private final V handler;

        /**
         * Creates a phantom managed reference.
         *
         * @param referent the referent to track
         * @param handler the cleanup handler associated with the reference
         * @param q the queue to receive the reference when enqueued
         */
        public PhantomRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            super(referent, q);
            this.handler = handler;
        }

        /** {@inheritDoc} */
        @Override
        public V getHandler() {
            return handler;
        }            
    }

    private static class HardRef<TT,V extends Finalizable> implements Reference<TT,V> {
        private TT ref;
        private final V handler;

        /**
         * Creates a strongly reachable managed reference.
         *
         * @param referent the referent to retain strongly
         * @param handler the cleanup handler associated with the reference
         * @param q ignored because hard references are never enqueued
         */
        public HardRef(TT referent, V handler, ReferenceQueue<? super TT> q) {
            this.ref = referent;
            this.handler = handler;
        }

        /** {@inheritDoc} */
        @Override
        public V getHandler() {
            return handler;
        }

        /** {@inheritDoc} */
        @Override
        public TT get() {
            return ref;
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            ref = null;
        }        
    }

}

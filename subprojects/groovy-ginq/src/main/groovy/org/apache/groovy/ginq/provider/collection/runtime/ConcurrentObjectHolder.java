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
package org.apache.groovy.ginq.provider.collection.runtime;

import org.apache.groovy.internal.util.Supplier;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Hold an object thread-safely
 *
 * @param <T> the type of object
 * @since 4.0.0
 */
class ConcurrentObjectHolder<T> {
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    private volatile T object;

    public ConcurrentObjectHolder() {}

    public ConcurrentObjectHolder(T object) {
        this.object = object;
    }

    public T getObject() {
        readLock.lock();
        try {
            return object;
        } finally {
            readLock.unlock();
        }
    }

    public T getObject(Supplier<? extends T> def) {
        if (null != object) return object;

        writeLock.lock();
        try {
            if (null == object) {
                object = def.get();
            }
            return object;
        } finally {
            writeLock.unlock();
        }
    }

    public void setObject(T object) {
        writeLock.lock();
        try {
            this.object = object;
        } finally {
            writeLock.unlock();
        }
    }
}

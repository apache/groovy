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

/**
 * Hold an object thread-safely
 *
 * @param <T> the type of object
 * @since 4.0.0
 */
class ConcurrentObjectHolder<T> {
    private volatile T object;
    private final Supplier<T> supplier;

    /**
     * Creates a holder that lazily initializes its object from the given supplier.
     *
     * @param supplier the supplier used to create the object
     */
    ConcurrentObjectHolder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Returns the cached object, creating it on first access.
     *
     * @return the cached object
     */
    public T getObject() {
        if (null != object) return object;

        synchronized(this) {
            if (null == object) {
                object = supplier.get();
            }
            return object;
        }
    }
}
